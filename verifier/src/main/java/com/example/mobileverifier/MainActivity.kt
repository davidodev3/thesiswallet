package com.example.mobileverifier

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mobileverifier.ui.theme.MobileWalletTheme
import id.walt.did.dids.DidService
import id.walt.policies.Verifier
import id.walt.policies.models.PolicyRequest
import id.walt.policies.policies.JwtSignaturePolicy
import id.walt.policies.policies.vp.HolderBindingPolicy
import id.walt.w3c.utils.VCFormat
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MobileWalletTheme {
        var overallSuccess by rememberSaveable { mutableStateOf(false) }
        var showDialog by rememberSaveable { mutableStateOf(false) }
        val coroutine = rememberCoroutineScope()





        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          if (showDialog) {
            ResultDialog(overallSuccess) { showDialog = false }
          }

          Column(Modifier.verticalScroll(rememberScrollState())) {
            Greeting(
              name = "Android",
              modifier = Modifier.padding(innerPadding)
            )
            Input() { fieldInput ->
              try {
                //Launch a coroutine that verifies the presentation and then show the results in the dialog.
                coroutine.launch {
                  overallSuccess = coroutine.async {
                    verify(fieldInput)
                  }.await()
                }
              } finally {
                showDialog = true
              }
            }
          }
        }
      }

    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Composable
fun Input(onSubmit: (String) -> Unit) {
  var fieldInput by rememberSaveable { mutableStateOf(" ") }
  Column {
    OutlinedTextField(value = fieldInput, onValueChange = { v -> fieldInput = v })
    OutlinedButton(onClick = { onSubmit(fieldInput) }) { Text("Verify") }
  }
}

suspend fun verify(presentation: String): Boolean {
  DidService.minimalInit()

  val vpPolicies = listOf(PolicyRequest(HolderBindingPolicy()))
  val globalPolicies = listOf(PolicyRequest(JwtSignaturePolicy()))
  return Verifier.verifyPresentation(
    VCFormat.jwt,
    presentation,
    vpPolicies,
    globalPolicies,
    mutableMapOf(), //Leave exceptions empty for now TODO
  ).overallSuccess()
}

@Composable
fun ResultDialog(success: Boolean, onDismissRequest: () -> Unit) {

  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(text = "Verification:") },
    dismissButton = {
      TextButton(onClick = onDismissRequest) { Text("Dismiss") }
    },

    confirmButton = {
      TextButton(onClick = {
        onDismissRequest()
      }) { Text("Confirm") }
    },

    text = {
      Text(if (success) "SUCCESS" else "FAILURE")
    },

    )
}