package com.example.mobilewallet

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(profileModel: ProfileModel = viewModel()) {
  Scaffold { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
      Text("key: ${profileModel.getKey().take(28)}")
      Text("did: ${profileModel.getDid().take(28)}")
      Regeneration()

    }

  }
}

class ProfileModel(application: Application) : AndroidViewModel(application) {
  private val _prefs = application.getSharedPreferences("did", Context.MODE_PRIVATE)

  //Not exposed as state flow because these at worst change once per application start.
  fun getKey() : String {
    return _prefs.getString("key", "") ?: ""

  }
  fun getDid() : String {
    return _prefs.getString("did", "") ?: ""
  }
}