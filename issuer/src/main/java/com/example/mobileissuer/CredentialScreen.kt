package com.example.mobileissuer

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileissuer.ui.theme.MobileWalletTheme
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.w3c.CredentialBuilder
import id.walt.w3c.vc.vcs.W3CVC
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch









class CredentialModel(context: Application) : AndroidViewModel(context) {




  /*This is not exposed as state flow because realistically speaking at worst
  this gets updated once per application start. Also any change does not really impact the UI.
  So in short no preference listener.*/
  private val prefs = context.getSharedPreferences("did", Context.MODE_PRIVATE)

  //Mapping of data to be passed to the generated credential
  private val _mapping = MutableStateFlow(mutableStateMapOf<String, String>())
  val mapping = _mapping.asStateFlow()

  fun getDid(): String {
    return prefs.getString("did", "") ?: ""
  }

  fun getKey(): String {
    return prefs.getString("key", "") ?: ""
  }

  fun updateMap(key: String, value: String) {
    _mapping.value[key] = value
  }

  suspend fun generateCredential(type: String): String {
    val jwt = viewModelScope.async {
      generateCredential(type, mapping.value, getDid(), getKey())
    }
    return jwt.await()
  }

}

@Composable
fun CredentialScreen(credential: String, credentialModel: CredentialModel = viewModel()) {
  var showDialog by remember { mutableStateOf(false) }
  var content by remember { mutableStateOf("") }
  var loading by remember { mutableStateOf(false) }
  val fields: @Composable () -> Unit
  val mapping by credentialModel.mapping.collectAsStateWithLifecycle()
  val coroutineScope = rememberCoroutineScope()

  /*Passing a map to generate a credential is the most convenient way
  but since we also need to show text inputs in real time
  we need some support variables to store the data, the map gets populated under the hood*/
  if (credential == "universityDegree") {
    fields = {
      var type by remember { mutableStateOf("") }
      var name by remember { mutableStateOf("") }
      OutlinedTextField(value = type, onValueChange = { v: String ->
        mapping["type"] = v
        type = v
      })
      OutlinedTextField(value = name, onValueChange = { v: String ->
        mapping["name"] = v
        name = v
      })
    }
  } else {
    fields = {}
  }

  if (showDialog) {
    CredentialDialog(content) { showDialog = false }
  }

  MobileWalletTheme {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        Text("University Degree")
        fields()

        ElevatedButton(onClick = {
          coroutineScope.launch {
            loading = true
            try {
              content = credentialModel.generateCredential(
                credential
              )
            } finally {
              loading = false

            }
          }
          showDialog = true
        }) {
          if (loading) {
            CircularProgressIndicator()
          } else {











            Text("Generate")

          }
        }
      }
    }
  }
}

suspend fun generateCredential(
  type: String,

  map: Map<String, String>,
  issuer: String,
  key: String
): String {
  //TODO: Remake
  val credential: W3CVC
  if (type == "universityDegree") {
    credential = CredentialBuilder().apply {
      addContext("https://www.w3.org/2018/credentials/examples/v1")

      addType("UniversityDegree")
      issuerDid = issuer
      validFromNow()
      subjectDid = "did:key:z6MkmLUYGGZXTCAqq7PtavWYTD93B8mw3dkjL5e1PSqQRPTr" //TODO Hardcoded
      useCredentialSubject(map.toJsonObject())
    }.buildW3C()
  } else {
    credential = W3CVC(mutableMapOf())
  }

  val signed = credential.signJws(
    JWKKey.importJWK(key).getOrNull()!!,
    issuer,
    subjectDid = "did:key:z6MkmLUYGGZXTCAqq7PtavWYTD93B8mw3dkjL5e1PSqQRPTr" //TODO
  )
  return signed
}



@Composable
fun CredentialDialog(content: String, onDismissRequest: () -> Unit) {

  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = "Issued credential:") },
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