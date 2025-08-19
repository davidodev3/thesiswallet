package com.example.mobileissuer

import android.app.Activity

import android.content.Context
import android.app.Application
import android.content.Intent
import androidx.activity.compose.LocalActivity
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
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.oid4vc.OpenID4VC as OpenID

import id.walt.oid4vc.OpenID4VCI as OIDVCI
import id.walt.oid4vc.OpenID4VCIVersion as OIDVCIVersion
import id.walt.oid4vc.data.CredentialOffer
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow











import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

import kotlin.uuid.Uuid

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

@OptIn(ExperimentalUuidApi::class)

@Composable
fun CredentialScreen(credential: String, credentialModel: CredentialModel = viewModel()) {
  var showDialog by remember { mutableStateOf(false) }
  var content by remember { mutableStateOf("") }
  var loading by remember { mutableStateOf(false) }
  val fields: @Composable () -> Unit
  val mapping by credentialModel.mapping.collectAsStateWithLifecycle()
  val coroutineScope = rememberCoroutineScope()
  val activity = LocalActivity.current

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

      var firstName by remember { mutableStateOf("John") }
      var lastName by remember { mutableStateOf("Doe") }
      var gender by remember { mutableStateOf("M") }
      var nationality by remember { mutableStateOf("Canadian") }
      var dateOfBirth by remember { mutableStateOf("1/1/1970") }
      var passportNumber by remember { mutableStateOf("AAAAAA") }
      var visaType by remember { mutableStateOf("Tourism") }
      var entryNumber by remember { mutableStateOf("Single") }
      var duration by remember { mutableStateOf("90") }

      var purposeOfVisit by remember { mutableStateOf("Tourism") }
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
      OutlinedTextField(value = entryNumber, onValueChange = { v: String ->
        mapping["entryNumber"] = v

        entryNumber = v
      }, label = {Text("Entry Number")})

      OutlinedTextField(value = duration, onValueChange = { v: String ->
        mapping["duration"] = v
        duration = v
      }, label = {Text("Duration")})
      OutlinedTextField(value = purposeOfVisit, onValueChange = { v: String ->
        mapping["purposeOfVisit"] = v
        purposeOfVisit = v
      }, label = {Text("Purpose of visit")})
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
        Text(credential)
        fields()
        ElevatedButton(onClick = {
          coroutineScope.launch {
            loading = true

            //TODO: rewrite better

            if (activity?.callingPackage == "com.example.mobilewallet") {
              val ISSUER_BASE_URL = "https://server"
              val preAuthCode = OpenID.generateAuthorizationCodeFor(Uuid.random().toString(), ISSUER_BASE_URL, JWKKey.importJWK(credentialModel.getKey()).getOrNull()!!)
              val requestUrl = CustomCredentialOffer(ISSUER_BASE_URL, mutableListOf(credential), preAuthCode).toOfferUrl()
              val result = Intent().apply {
                putExtra("credential_offer", requestUrl)
              }

              activity.setResult(Activity.RESULT_OK, result)

              activity.finish()
            }

            /*try {
              content = credentialModel.generateCredential(
                credential
              )
            } finally {
              loading = false

            }*/
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