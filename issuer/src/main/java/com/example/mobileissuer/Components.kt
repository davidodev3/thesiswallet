package com.example.mobileissuer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp


//Heading displayed in the main screen
@Composable
fun Heading(modifier: Modifier = Modifier) {
  Text(
    text = "Issue",
    modifier = modifier,
    fontSize = TextUnit(38.0f, TextUnitType.Sp)
  )
}

//Card that encloses single credentials
@Composable
fun CredentialCard(credential: String, onClick: () -> Unit) {
  Card(
    colors = CardDefaults.cardColors(),
    modifier = Modifier
      .height(100.0.dp)
      .padding(8.dp)
      .fillMaxWidth()



      .clickable {onClick()}
  ) {
    Text(credential)
  }
}

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