package com.example.mobilewallet

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.credentials.DigitalCredential
import androidx.credentials.ExperimentalDigitalCredentialApi
import androidx.credentials.registry.provider.RegisterCredentialsRequest
import androidx.credentials.registry.provider.RegistryManager











import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


import java.util.Base64

@Composable
fun WalletScreen(name: String, onClick: (String) -> Unit) {
  val context = LocalContext.current
  var showDialog by remember { mutableStateOf(false) }
  val dom : DocumentModel = viewModel(
    factory = DocumentModelFactory(

      context.applicationContext as Application, name
    )
  )
  MobileWalletTheme {
    Scaffold(modifier = Modifier.fillMaxSize(), floatingActionButton = {
      AddButton(onClick = {
        showDialog = true
      })
    }) { innerPadding ->
      if (showDialog) {
        AddDocumentDialog(dom) { showDialog = false }
      }
      Column(modifier = Modifier.padding(innerPadding)) {
        WalletName(name)
        DocumentList(dom, onClick)
      }
    }
  }
}

class DocumentModel(val name: String, private val application: Application) : AndroidViewModel(application),
  SharedPreferences.OnSharedPreferenceChangeListener {
  private val _walletPrefs = (application.getSharedPreferences("wallets", Context.MODE_PRIVATE))

  private val _documents = MutableStateFlow(
    _walletPrefs.getStringSet(name, mutableSetOf<String>())?.toMutableList()
      ?: mutableListOf<String>()
  )
  val documents = _documents.asStateFlow()

  init {
    _walletPrefs.registerOnSharedPreferenceChangeListener(this)
  }

  override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
    _documents.value =
      sharedPreferences.getStringSet(key, mutableSetOf<String>())?.toMutableList()
        ?: mutableListOf<String>()
  }

  fun removeDocument(jwt: String) {
    val doc = _documents.value.toMutableSet()
    //Remove the element from the new list
    doc.remove(jwt)
    with(_walletPrefs.edit()) {
      //Update preferences with new list
      putStringSet(name, doc)

      commit()
    }
  }

  @OptIn(ExperimentalDigitalCredentialApi::class)
  fun addDocument(jwt: String) {
    //Nested map
    val documentId = (tokenToPayload(jwt)["vc"] as JsonObject)["id"].toString()

    val registryManager = RegistryManager.create(application)

    try {
      //This gets executed in a background coroutine with no return value.
      viewModelScope.launch {
        /*Explanation: the API is experimental and does not manage data storage for now.











        So we need to register the credential in the "RegistryManager" and store the actual document somewhere else.
        Also data is treated as "opaque blobs" (binary large objects) so everything has to be binary data.*/
        registryManager.registerCredentials(request = object : RegisterCredentialsRequest(
          DigitalCredential.TYPE_DIGITAL_CREDENTIAL,
          documentId,
          jwt.encodeToByteArray(),
          readBinary("openidvp.wasm", application) //Matcher provided by Google on their repository
        ) {})
      }
    } finally {
      /*The JWT is unique for each issued credential so we can directly save that.
      SQLite was definitely an option to store the actual JSON data (decoded from the issued JWT),
      but probably we need to work with JSON/JWT more so using SharedPreferences to store strings seems more efficient.*/
      val doc = _documents.value.toMutableSet()
      doc.add(jwt)
      with (_walletPrefs.edit()) {
        putStringSet(name, doc)
        commit()
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    _walletPrefs.unregisterOnSharedPreferenceChangeListener(this)
  }
}

//A factory is needed for the viewmodel because extra parameters are needed.
class DocumentModelFactory(private val application: Application, private val name: String) :
  ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
    @Suppress("UNCHECKED_CAST") return DocumentModel(name, application) as T
  }
}

@Composable
fun DocumentList(documentModel: DocumentModel, onClick: (String) -> Unit) {

  val documents by documentModel.documents.collectAsStateWithLifecycle()
  if (documents.isEmpty()) {
    Text("No credentials are stored in this wallet yet.")
  } else {
    LazyColumn {
      items(documents) { jwt ->
        CredentialCard(jwt, onClick) {documentModel.removeDocument(jwt)}
      }
    }

  }
}

@Composable
fun WalletName(name: String) {
  Text(name, fontSize = TextUnit(38.0f,
    TextUnitType.Sp)
  )
}

@Composable
fun AddDocumentDialog(documentModel: DocumentModel, onDismissRequest: () -> Unit) {
  var value by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = "Import credential") },
    dismissButton = {
      TextButton(onClick = onDismissRequest) { Text("Dismiss") }
    },
    confirmButton = {
      TextButton(onClick = {
        documentModel.addDocument(value)
        onDismissRequest()
      }) { Text("Confirm") }
    },
    text = {
      OutlinedTextField(value = value, onValueChange = { v -> value = v }, singleLine = true
      )
    },
  )
}

@Composable
fun CredentialCard(jwt: String, onClick: (String) -> Unit, delete: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()











      .padding(16.00.dp)
      .height(100.0.dp)
      .clickable {onClick(jwt)}

  ) {
    Row() {
      val payload = tokenToPayload(jwt).jsonObject

      Text(((payload["vc"] as JsonObject)["type"] as JsonArray)[1].toString(), Modifier.padding(16.00.dp))
      IconButton(onClick = delete) {
        Icon(
          Icons.Filled.Delete, "Delete selected credential"
        )
      }
    }

  }
}

fun readBinary(filename: String, application: Application) : ByteArray {
  val input = application.assets.open(filename)
  val binary = ByteArray(input.available())
  input.read(binary)
  input.close()
  return binary

}


fun tokenToPayload(jwt: String) : JsonObject {
  val decoder = Base64.getUrlDecoder()
  //The actual content of the encoded JSON is in the second part, the one after the first dot. Decode the JWT and then convert the resulting JSON string into an object.
  return Json.parseToJsonElement(decoder.decode(jwt.split(".")[1]).decodeToString()).jsonObject
}