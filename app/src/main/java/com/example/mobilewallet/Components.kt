package com.example.mobilewallet

import android.app.Application
import android.content.Context
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


//Dialog that pops up when generating a presentation without having been redirected by the verifier.
@Composable
fun CredentialDialog(content: String, onDismissRequest: () -> Unit) {

  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = "Verifiable presentation:") },
    dismissButton = {
      TextButton(onClick = onDismissRequest) { Text("Dismiss") }},
    confirmButton = {
      val clipboardManager = LocalClipboardManager.current
      TextButton(onClick = {
        clipboardManager.setText(AnnotatedString(content))

        onDismissRequest()
      }) { Text("Confirm") }
    },
    text = {
      SelectionContainer {
        Text(content, modifier = Modifier.verticalScroll(rememberScrollState()))
      }
    },
  )

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