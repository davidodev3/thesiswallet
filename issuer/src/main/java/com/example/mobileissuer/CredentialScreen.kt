package com.example.mobileissuer

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobileissuer.ui.theme.MobileWalletTheme
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
  if (credential == "UniversityDegree") {
    fields = {
      var type by remember { mutableStateOf("") }
      var name by remember { mutableStateOf("") }
      OutlinedTextField(value = type, onValueChange = { v: String ->
        mapping["type"] = v
        type = v
      }, label = {Text("Type")})
      OutlinedTextField(value = name, onValueChange = { v: String ->
        mapping["name"] = v
        name = v
      }, label = {Text("Name")})
    }
  } else if (credential == "Visa") {
    fields = {
      var firstName by remember { mutableStateOf("") }
      var lastName by remember { mutableStateOf("") }

      var gender by remember { mutableStateOf("") }
      var nationality by remember { mutableStateOf("") }
      var dateOfBirth by remember { mutableStateOf("") }
      var passportNumber by remember { mutableStateOf("") }
      var visaType by remember { mutableStateOf("") }
      var entryNumber by remember { mutableStateOf("") }
      val visaValidity = remember { mutableStateMapOf<String, String>() }
      var purposeOfVisit by remember { mutableStateOf("") }
      OutlinedTextField(value = firstName, onValueChange = { v: String ->

        mapping["firstName"] = v
        firstName = v
      }, label = {Text("First name")})
      OutlinedTextField(value = lastName, onValueChange = { v: String ->
        mapping["lastName"] = v
        lastName = v
      }, label = {Text("Last name")})
      OutlinedTextField(value = gender, onValueChange = { v: String ->
        mapping["gender"] = v

        gender = v
      }, label = {Text("Gender")})
      OutlinedTextField(value = nationality, onValueChange = { v: String ->
        mapping["nationality"] = v
        nationality = v
      }, label = {Text("Nationality")})
      OutlinedTextField(value = dateOfBirth, onValueChange = { v: String ->
        mapping["dateOfBirth"] = v
        dateOfBirth = v

      }, label = {Text("Date of birth")})
      OutlinedTextField(value = passportNumber, onValueChange = { v: String ->
        mapping["passportNumber"] = v
        passportNumber = v
      }, label = {Text("Passport number")})











      OutlinedTextField(value = visaType, onValueChange = { v: String ->
        mapping["visaType"] = v
        visaType = v
      }, label = {Text("Visa type")})
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