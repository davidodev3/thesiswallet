package com.example.mobileverifier

import android.app.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier










import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType


import com.example.mobileverifier.ui.theme.MobileWalletTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    enableEdgeToEdge()
    setContent {
      MobileWalletTheme {
        var vpToken by rememberSaveable { mutableStateOf("") }

        var overallSuccess by rememberSaveable { mutableStateOf(false) }
        var showDialog by rememberSaveable { mutableStateOf(false) }
        val coroutine = rememberCoroutineScope()
        val getToken =

          rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { value ->
            if (value.resultCode == Activity.RESULT_OK) {
              vpToken = value.data?.getStringExtra("vp_token") ?: ""
            }
          }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          if (showDialog) {
            ResultDialog(overallSuccess) { showDialog = false

              vpToken = "" //Resets the vpToken to default
            }
          }
          Column(
            modifier = Modifier
              .padding(innerPadding)

              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween

          ) {
            Heading()
            if (vpToken == "") {
              Text("Paste a JWT below for verification")
              Fields { fieldInput ->
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
              Text("Or request a verifiable presentation from the wallet.")
              Presentation(getToken)

            } else {
              Text("Received token. ${vpToken.take(30)}...") //Add ellipsis for long strings
              ElevatedButton(onClick = {
                try {
                  //Launch a coroutine that verifies the presentation and then show the results in the dialog.
                  coroutine.launch {
                    overallSuccess = coroutine.async {

                      verify(vpToken)

                    }.await()
                  }
                } finally {
                  showDialog = true
                }
              }) { Text("Verify") }
            }
          }
        }

      }
    }
  }

}











@Composable

fun Heading(modifier: Modifier = Modifier) {

  Text(
    text = "Verify", modifier = modifier, fontSize = TextUnit(38.0f, TextUnitType.Sp)
  )
}

@Composable
fun Fields(onSubmit: (String) -> Unit) {
  var fieldInput by rememberSaveable { mutableStateOf("") }
  Column {

    OutlinedTextField(value = fieldInput, onValueChange = { v -> fieldInput = v })
    OutlinedButton(onClick = { onSubmit(fieldInput) }) { Text("Verify") }
  }
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

@Composable

fun Presentation(launcher: ActivityResultLauncher<Intent>) {

  val context = LocalContext.current
  var result by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  Row {
    ElevatedButton(onClick = {
      coroutineScope.launch {
        result = coroutineScope.async {
          credentialRequest(false, context, launcher)

        }.await()
      }
    }) {
      Text("Custom request")
    }

    ElevatedButton(onClick = {
      coroutineScope.launch {
        result = coroutineScope.async {

          credentialRequest(true, context, launcher)
        }.await()
      }
    }) {
      Text("Using DCAPI")
    }
  }
}