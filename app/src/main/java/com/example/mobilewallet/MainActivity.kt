package com.example.mobilewallet

import android.app.Activity

import android.content.SharedPreferences
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import id.walt.oid4vc.data.CredentialOffer
import kotlinx.coroutines.Dispatchers

import id.walt.oid4vc.OpenID4VCI as OIDVCI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import java.security.Security


@Serializable
class Wallet(val name: String)

@Serializable
object Home

@Serializable
object Profile


@Serializable
class Credential(val credential: String)

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
  issued: Issued = viewModel()
) {
  val offer by issued.credentialOffer.collectAsStateWithLifecycle()

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

        navController.navigate(Credential(jwt))
      })

    }












    composable<Profile> {
      ProfileScreen()
    }

    composable<Credential> { bsEntry ->
      val credential: Credential = bsEntry.toRoute()
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
  issued: Issued = viewModel()
) {
  MobileWalletTheme {
    var showDialog by remember { mutableStateOf(false) }
    val offer by issued.credentialOffer.collectAsStateWithLifecycle()

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      floatingActionButton = {
        AddButton(onClick = {
          showDialog = true
        })
      }) { innerPadding ->
      if (offer != "") {
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
          Text("Credential offer: $offer")

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

  issued: Issued = viewModel()
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


class Issued : ViewModel() {


  private var _offer = MutableStateFlow("")
  var credentialOffer = _offer.asStateFlow()

  fun updateOffer(newCredentialOffer: String) {
    _offer.value = newCredentialOffer
  }

}


@Composable
fun ReceivedScreen(issued: Issued = viewModel()) {

  val offerUrl by issued.credentialOffer.collectAsStateWithLifecycle()
  var offer : CredentialOffer? by remember { mutableStateOf(null) }
  val coroutine = rememberCoroutineScope()

  LaunchedEffect(offerUrl) {
    offer = OIDVCI.parseAndResolveCredentialOfferRequestUrl(offerUrl)

  }
  Column {
    Text("Credential offer:", fontSize = TextUnit(38.0f, TextUnitType.Sp))
    Text("${offer?.draft11?.credentials[0]}")
    ElevatedButton(onClick = { coroutine.launch{performRequest()} }) { Text("Accept") }
  }
}



suspend fun performRequest() {
  val client = OkHttpClient();
  withContext(Dispatchers.IO) {
    withContext(Dispatchers.IO) {
      val request = okhttp3.Request.Builder().url("https://www.example.org/").build()
      val response = client.newCall(request).execute()
      Log.i("AAAAAA", response.body.string())

    }

  }
}