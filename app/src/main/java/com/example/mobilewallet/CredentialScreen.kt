package com.example.mobilewallet

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.w3c.PresentationBuilder
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CredentialModel(context: Application) : AndroidViewModel(context) {
  private val _prefs = context.getSharedPreferences("did", Context.MODE_PRIVATE)

  @OptIn(ExperimentalUuidApi::class)
  suspend fun generatePresentation(credential: String): String {
    DidService.minimalInit()
    val presentation = PresentationBuilder().apply {
      did = _prefs.getString("did", "")
      nonce = Uuid.random().toString() //Generate a random string every time to be used as nonce
      addCredential(JsonPrimitive(credential))
    }








    val key = _prefs.getString("key", "") ?: ""

    return presentation.buildAndSign(JWKKey.importJWK(key).getOrNull()!!)
  }

  //TODO: Remove, for debug and development purposes only
  fun getDid() : String {
    return _prefs.getString("did", "") ?: ""
  }

}

@Composable
fun CredentialScreen(credential: String, credentialModel: CredentialModel = viewModel()) {
  val coroutineScope = rememberCoroutineScope()

  var showDialog by remember { mutableStateOf(false) }
  var content by remember { mutableStateOf("") }

  //TODO: dialog
  MobileWalletTheme {

    Scaffold { innerPadding ->
      if (showDialog) {
        CredentialDialog(content) {showDialog = false}
      }
      Column(modifier = Modifier
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())) {
        Text(tokenToPayload(credential).toString())
        TextButton(onClick = {
          coroutineScope.launch {
            try {
              content = credentialModel.generatePresentation(credential)
            } finally {
              showDialog = true
            }
          }
        }) { Text("Generate presentation") }
        //TODO: Remove
        val clipboardManager = LocalClipboardManager.current
        TextButton(onClick = {
          clipboardManager.setText(AnnotatedString(credentialModel.getDid()))
        }) {Text("Copy did")}
      }
    }

  }
}

@Composable

fun CredentialDialog(content: String, onDismissRequest: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = "Verifiable presentation:") },
    dismissButton = {
      TextButton(onClick = onDismissRequest) { Text("Dismiss") }
    },








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