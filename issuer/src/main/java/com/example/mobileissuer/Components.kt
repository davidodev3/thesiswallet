package com.example.mobileissuer

import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString


//Dialog shown when generating a credential
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