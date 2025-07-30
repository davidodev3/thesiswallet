package com.example.mobilewallet

import android.app.Application

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilewallet.ui.theme.MobileWalletTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


@Composable
fun SelectionScreen(selectionModel: SelectionModel = viewModel()) {
  val loading = selectionModel.loading.collectAsStateWithLifecycle()

  MobileWalletTheme {










    Scaffold { innerPadding ->
      Column {
        Text("Selection", modifier = Modifier.padding(innerPadding))
        if (loading.value) {

          CircularProgressIndicator()
        } else {
          LazyColumn {
            items(selectionModel.getWalletCredentials()) { credential ->
              val payload = tokenToPayload(credential.second).jsonObject

              //Get the actual credential type (the one at position 0 is "VerifiableCredential")
              val type = ((payload["vc"] as JsonObject)["type"] as JsonArray)[1].toString()
              Card(Modifier

                .fillMaxWidth()
                .padding(16.00.dp)
                .height(100.0.dp)
                .clickable(onClick = {})
              ) {
                Text("${credential.first}: $type")
              }
            }
          }

        }
      }
    }
  }
}

class SelectionModel(context: Application) : AndroidViewModel(context) {
  private val _walletPrefs = context.getSharedPreferences("wallets", Context.MODE_PRIVATE)
  private var _loading = MutableStateFlow(true)

  val loading = _loading.asStateFlow()

  fun getWalletCredentials(): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    //This operation could take a while so is delegated to a coroutine.
    try {

      viewModelScope.launch {
        _walletPrefs.all.keys.forEach { key ->

          _walletPrefs.getStringSet(key, mutableSetOf())?.forEach { value ->
            result += Pair(key, value)
          }
        }
      }
    } finally {
      _loading.value = false
    }
    return result

  }
}