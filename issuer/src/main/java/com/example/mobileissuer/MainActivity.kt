package com.example.mobileissuer

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
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
    val prefs = getSharedPreferences("did", Context.MODE_PRIVATE)
    setContent {
      /*Column {
        Text("did: ${prefs.getString("did", "")}")
        Text("key: ${prefs.getString("key", "")}")
      }*/
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
            CredentialCard("University Degree") { navController.navigate(Credential("universityDegree")) }
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

@Composable
fun Heading(modifier: Modifier = Modifier) {
  Text(
    text = "Issue",
    modifier = modifier,
    fontSize = TextUnit(38.0f, TextUnitType.Sp)
  )
}

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