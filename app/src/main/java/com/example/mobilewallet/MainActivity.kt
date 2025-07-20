package com.example.mobilewallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue











import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.navigation.toRoute
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
fun MyHost(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
  NavHost(modifier = modifier, navController = navController, startDestination = Home) {
    composable<Home> {
      MainScreen (onClick = {name ->
        navController.navigate(Wallet(name))},
        onProfileClick = {navController.navigate(Profile)}
      )
    }
    composable<Wallet> { bsEntry ->
      val wallet: Wallet = bsEntry.toRoute()
      WalletScreen(wallet.name, onClick = {jwt ->
        navController.navigate(Credential(jwt))
      })
    }
    composable<Profile> {
      ProfileScreen()
    }
    composable<Credential> {bsEntry ->
      val credential: Credential = bsEntry.toRoute()
      CredentialScreen(credential.credential)
    }
    composable<Login> {}
  }

}

@Composable
fun MainScreen(onClick: (String) -> Unit, onProfileClick: () -> Unit) {
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
        Regeneration()
        ListColumn(onClick = onClick)
        IconButton(onClick = onProfileClick) {
          Icon(
            Icons.Filled.Person, "Profile"
          )
        }
      }
    }
  }
}

class WalletModel(application: Application) : AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!", modifier = modifier, fontSize = TextUnit(38.0f, TextUnitType.Sp)
  )
}

@Composable
fun Subtitle() {
  Text(text = "Your wallets", fontSize = TextUnit(30.0f, TextUnitType.Sp))
}

@Composable
fun AddButton(onClick: () -> Unit) {
  FloatingActionButton(onClick = onClick) {
    Icon(
      Icons.Filled.Add, "Add new wallet or digital credential"
    )
  }

}

@Composable
fun CardWallet(name: String, onClick: (String) -> Unit, delete: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.00.dp)
      .height(100.0.dp)
      .clickable {onClick(name)}
  ) {
    Row() {
      Text(name, Modifier.padding(16.00.dp))
      IconButton(onClick = delete) {
        Icon(
          Icons.Filled.Delete, "Delete selected wallet"
        )
      }
    }
  }
}

@Composable
fun ListColumn(walletModel: WalletModel = viewModel(), onClick: (String) -> Unit) {
  val wallets by walletModel.wallets.collectAsStateWithLifecycle()
  if (wallets.isEmpty()) {
    Text("You don't have any wallets yet.")
  }
  else {
    LazyColumn {
      items(wallets) { name ->
        CardWallet(name, onClick) {
          walletModel.removeWallet(name)
        }
      }
    }
  }
}








@Composable
fun AddWalletDialog(walletModel: WalletModel = viewModel(), onDismissRequest: () -> Unit) {
  var value by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = "Add wallet") },
    dismissButton = {
      TextButton(onClick = onDismissRequest) { Text("Dismiss") }
    },
    confirmButton = {
      TextButton(onClick = {
        walletModel.addWallet(value)
        onDismissRequest()

      }) { Text("Confirm") }
    },
    text = {
      OutlinedTextField(value = value, onValueChange = { v -> value = v }
      )
    },
  )
}

//TODO: Remove
class ButtonModel(application: Application) : AndroidViewModel(application) {
  private val _preferences = application.getSharedPreferences("did", Context.MODE_PRIVATE)

  fun regenerate() {
    viewModelScope.launch {
      val keydid = async {
        generateKeyDid()
      }
      with(_preferences.edit()) {
        val resolved = keydid.await()
        putString("did", resolved.second)
        putString("key", resolved.first.exportJWK())
        apply()
      }
    }
  }
}

@Composable
fun Regeneration(buttonModel: ButtonModel = viewModel()) {
  ElevatedButton(onClick = {
    buttonModel.regenerate()
  }) {Text("Regenerate")}
}