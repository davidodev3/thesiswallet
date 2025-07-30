package com.example.mobileissuer

import android.app.Application

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mobileissuer.ui.theme.MobileWalletTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable

class Credential(val type: String)
@Serializable
object Home













class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      MyHost()
    }
  }
}

@Composable
fun MyHost(

  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController()
) {
  NavHost(modifier = modifier, navController = navController, startDestination = Home) {
    composable<Home> {
      MobileWalletTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Column() {
            Heading(

              modifier = Modifier.padding(innerPadding)
            )
            CredentialCard("Visa") { navController.navigate(Credential("Visa")) }
            CredentialCard("University Degree") { navController.navigate(Credential("UniversityDegree")) }
            Regeneration()
          }
        }
      }
    }

    composable<Credential> { bsEntry ->
      val credential: Credential = bsEntry.toRoute()
      CredentialScreen(credential.type)
    }
  }
}

//TODO: Remove
class ButtonModel(application: Application) : AndroidViewModel(application) {

  private val _preferences = application.getSharedPreferences("did", Context.MODE_PRIVATE)

  fun regenerate() {
    viewModelScope.launch {
      val keydid = async {
        generateKeyDid()
      }
      with(_preferences.edit()) {
        val resolved = keydid.await()

        putString("did", resolved.second)
        putString("key", resolved.first.exportJWK())
        apply()
      }
    }

  }
}


@Composable
fun Regeneration(buttonModel: ButtonModel = viewModel()) {
  ElevatedButton(onClick = {
    buttonModel.regenerate()
  }) {Text("Regenerate")}
}