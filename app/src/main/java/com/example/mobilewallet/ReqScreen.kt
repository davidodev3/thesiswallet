package com.example.mobilewallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.example.mobilewallet.ui.theme.MobileWalletTheme

@Composable

fun RequestScreen() {
  MobileWalletTheme {
    Scaffold { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        Text("Request credentials", fontSize = TextUnit(38.0f, TextUnitType.Sp))

      }
    }
  }

}