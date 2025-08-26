package com.example.mobilewallet

import android.app.Activity

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.activity.compose.LocalActivity
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

  fun generatePresentation(credential: String): String {
    val presentation = PresentationBuilder().apply {
      did = _prefs.getString("did", "")
      nonce = Uuid.random().toString() //Generate a random string every time to be used as nonce
      addCredential(JsonPrimitive(credential))
    }

    val key = _prefs.getString("key", "") ?: ""
    return presentation.buildAndSignBlocking(JWKKey.importJWKBlocking(key).getOrNull()!!)

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

  val activity = LocalActivity.current


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
              //If this app was called by the Verifier send the token back.
              if (activity?.callingPackage == "com.example.mobileverifier") {

                val result = Intent().apply {
                  //Key "vp_token" as per OpenID for Verifiable Presentations
                  putExtra("vp_token", content)
                }
                activity.setResult(Activity.RESULT_OK, result)
                activity.finish()
              }
              else {
                showDialog = true

              }
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