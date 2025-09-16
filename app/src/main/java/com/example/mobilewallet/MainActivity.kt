package com.example.mobilewallet

import android.app.Activity

import android.content.SharedPreferences
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.compose.setContent

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit



import androidx.compose.ui.unit.TextUnitType
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLDecoder

@Serializable
class Wallet(val name: String)

@Serializable
object Home

@Serializable
object Profile


@Serializable
class Credentials(val credential: String)

@Serializable
object Login

@Serializable
object Selection


class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyHost()
    }
  }

}


@Composable
fun MyHost(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  issued: IssuedCredentialModel = viewModel()
) {

  NavHost(modifier = modifier, navController = navController, startDestination = Home) {
    composable<Home> {
      MainScreen(
        onClick = { name ->
          navController.navigate(Wallet(name))
        },
        onProfileClick = { navController.navigate(Profile) },
      )
      //If called by the verifier redirect to the selection screen

      /*if (activity != null && activity.callingPackage == "com.example.mobileverifier") {
        navController.navigate(Selection)
      }*/ //TODO uncomment
    }
    composable<Wallet> { bsEntry ->
      val wallet: Wallet = bsEntry.toRoute()
      WalletScreen(wallet.name, onClick = { jwt ->
        issued.updateIssuedCredential("")
        navController.navigate(Credentials(jwt))

      })
    }
    composable<Profile> {
      ProfileScreen()
    }
    composable<Credentials> { bsEntry ->
      val credential: Credentials = bsEntry.toRoute()
      CredentialScreen(credential.credential)
    }

    composable<Selection> {
      SelectionScreen()
    }
  }

}

@Composable
fun MainScreen(



  onClick: (String) -> Unit,


  onProfileClick: () -> Unit,
  issued: IssuedCredentialModel = viewModel()
) {
  MobileWalletTheme {

    var showDialog by remember { mutableStateOf(false) }
    val offer by issued.offer.collectAsStateWithLifecycle()
    val credential by issued.credential.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    Log.i("AAAAAA", "Credenzialis: $credential")
    Scaffold(
      modifier = Modifier.fillMaxSize(),

      floatingActionButton = {

        AddButton(onClick = {
          showDialog = true
        })

      }) { innerPadding ->
      if (activity?.callingPackage == "com.example.mobileverifier") {
        SelectionScreen()
      }
      else {

        if (offer != "" && credential == "") {
          ReceivedScreen()
        } else {
          if (showDialog) {
            AddWalletDialog(onDismissRequest = { showDialog = false })
          }
          Column() {
            Greeting(
              name = "Android", modifier = Modifier.padding(innerPadding)

            )
            Subtitle()
            ListColumn(onClick = onClick)
            IconButton(onClick = onProfileClick) {
              Icon(
                Icons.Filled.Person, "Profile"
              )
            }
            HorizontalDivider()

            RequestCredentialsButton()
          }
        }
      }
    }
  }
}

class WalletModel(application: Application) : AndroidViewModel(application),

  SharedPreferences.OnSharedPreferenceChangeListener {
  private val _walletPrefs = (application.getSharedPreferences("wallets", Context.MODE_PRIVATE))
  private val _wallets = MutableStateFlow(_walletPrefs.all.keys.toMutableList())
  val wallets = _wallets.asStateFlow()

  init {
    _walletPrefs.registerOnSharedPreferenceChangeListener(this)
  }


  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
    _wallets.value = sharedPreferences.all.keys.toMutableList()
  }

  fun addWallet(name: String) {
    with(_walletPrefs.edit()) {
      putStringSet(name, mutableSetOf())
      commit()
    }

  }

  fun removeWallet(name: String) {
    with(_walletPrefs.edit()) {
      remove(name)
      commit()
    }
  }


  override fun onCleared() {
    super.onCleared()
    _walletPrefs.unregisterOnSharedPreferenceChangeListener(this)
  }
}

@Composable
fun RequestCredentialsButton(
  profileModel: ProfileModel = viewModel(),



  issued: IssuedCredentialModel = viewModel()
) {

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { value ->
      if (value.resultCode == Activity.RESULT_OK) {
        issued.updateOffer(value.data?.getStringExtra("credential_offer") ?: "")

      }
    }
  ElevatedButton(onClick = {
    val intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, profileModel.getDid())
      type = "https://schema.org/text"
      setPackage("com.example.mobileissuer") //Only targets our issuer app
    }

    launcher.launch(intent)
  }) {
    Text("Request credentials")
  }
}

class IssuedCredentialModel : ViewModel() {

  private var _offer = MutableStateFlow("")

  private var _credential = MutableStateFlow("")
  var offer = _offer.asStateFlow()
  var credential = _credential.asStateFlow()

  fun updateIssuedCredential(new: String) {
    _credential.value = new
  }

  fun updateOffer(newCredentialOffer: String) {

    _offer.value = newCredentialOffer
  }
}

@Composable
fun ReceivedScreen(issued: IssuedCredentialModel = viewModel()) {
  val url by issued.offer.collectAsStateWithLifecycle()
  var offer: CustomCredentialOffer? by remember { mutableStateOf(null) }
  val activity = LocalActivity.current

  val coroutine = rememberCoroutineScope()
  var clipboard = LocalClipboardManager.current
  val credential by issued.credential.collectAsStateWithLifecycle()
  LaunchedEffect(url) {
    val decoded = URLDecoder.decode(url, "utf-8")
    //Only gets what is after the first =, i.e. the URL encoded credential offer JSON.
    val payload = decoded.substring(decoded.indexOf('=') + 1)
    offer = Json.decodeFromString(payload)
  }

  Column {
    Text("Credential offer:", fontSize = TextUnit(38.0f, TextUnitType.Sp))
    Text(offer?.credentialConfigurationIds.toString())
    ElevatedButton(onClick = {
      clipboard.setText(AnnotatedString(url))
      coroutine.launch { performRequest(offer!!, activity!!, issued) }
    }) { Text("Accept") }
    Text("Issued: $credential")
  }


}