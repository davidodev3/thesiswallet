package com.example.mobilewallet

import android.app.Application

import android.content.SharedPreferences
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController










import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

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


@Serializable
object Request

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
  navController: NavHostController = rememberNavController()
) {

  val activity = LocalActivity.current
  NavHost(modifier = modifier, navController = navController, startDestination = Home) {
    composable<Home> {
      MainScreen(onClick = { name ->
        navController.navigate(Wallet(name))
      },
        onProfileClick = { navController.navigate(Profile) },
        onButtonClick = {navController.navigate(Request)}
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


    composable<Request> {
      RequestScreen()
    }
    composable<Selection> {
      SelectionScreen()
    }
  }
}


@Composable
fun MainScreen(onClick: (String) -> Unit, onProfileClick: () -> Unit, onButtonClick: () -> Unit) {
  MobileWalletTheme {
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize(),










      floatingActionButton = {
        AddButton(onClick = {
          showDialog = true
        })

      }) { innerPadding ->
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
fun RequestCredentialsButton( profileModel: ProfileModel = viewModel() ) {
  val activity = LocalActivity.current
  ElevatedButton(onClick = {
    val intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, profileModel.getDid())

      type = "https://schema.org/text"

      setPackage("com.example.mobileissuer") //Only targets our issuer app
    }
    activity?.startActivity(intent)
  }) {
    Text("Request credentials")
  }
}