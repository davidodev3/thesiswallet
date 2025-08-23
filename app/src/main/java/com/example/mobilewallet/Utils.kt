package com.example.mobilewallet

import android.app.Activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import id.walt.oid4vc.OpenID4VCI as OIDVCI
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

//Custom class for compliance to OpenID for Verifiable Credential Issuance Draft 16
@Serializable
class CustomCredentialOffer {

  @SerialName("credential_issuer")

  var credentialIssuer: String
  @SerialName("credential_configuration_ids")
  var credentialConfigurationIds: List<String>
  @Contextual
  var grants: Map<String, PreAuthorizedFlow>? = null











  constructor(issuer: String, credentialConfigs: List<String>, preAuthorizedCode: String) {

    this.credentialIssuer = issuer

    this.credentialConfigurationIds = credentialConfigs
    this.grants = mutableMapOf("urn:ietf:params:oauth:grant-type:pre-authorized_code" to
      PreAuthorizedFlow(preAuthorizedCode = preAuthorizedCode)
    )
  }
}

@Serializable
class PreAuthorizedFlow(

  @SerialName("tx_code")
  var txCode: TxCode? = null,
  @SerialName("pre-authorized_code")
  var preAuthorizedCode: String,
  @SerialName("authorization_server")
  var authorizationServer: String? = null
)

@Serializable

class TxCode(
  @SerialName("input_mode")
  var inputMode: String? = null,
  var length: Int? = null,
  var description: String? = null
)

//Used to handle the HTTP part of the flow to communicate with the OAuth 2.0 authorization server.
suspend fun performRequest(offered: CustomCredentialOffer, context: Activity) {

  val client = OkHttpClient()

  val metadataRequest = Request.Builder()
    .url(OIDVCI.getOpenIdProviderMetadataUrl(offered.credentialIssuer))
    .build()

  withContext(Dispatchers.IO) {
    val response = async {
      client.newCall(metadataRequest).execute()

    }

    val preAuthorizedCode =
      offered.grants?.get("urn:ietf:params:oauth:grant-type:pre-authorized_code")!!.preAuthorizedCode

    val requestParams = "grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code\n" +
            "&pre-authorized_code=$preAuthorizedCode" //TODO
    val requestBody = requestParams.toRequestBody(
      "application/x-www-form-urlencoded".toMediaType()
    )
    val metadata = response.await().body.string()
    Log.i("AAAAAA", metadata)
    val tokenEndpoint = Json.parseToJsonElement(metadata).jsonObject["token_endpoint"].toString()
      .removeSurrounding("\"")
    Log.i("AAAAAA", "$tokenEndpoint: ${tokenEndpoint.toHttpUrlOrNull()}")
    val tokenRequest = Request.Builder().post(requestBody).url(tokenEndpoint).build()
    val tokenResponse = async {
      client.newCall(tokenRequest).execute()
    }

    val accessToken = tokenResponse.await().body.string()

    Log.i("AAAAAA", accessToken)
    var messenger: Messenger?
    var bound = false

    val connection = object : ServiceConnection {
      override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        messenger = Messenger(binder)

        val msg = Message.obtain(null, 1, 0, 0)
        msg.data.putString("token", accessToken)
        messenger?.send(msg)
        bound = true
        //TODO: set replyto
      }
      override fun onServiceDisconnected(name: ComponentName?) {
        messenger = null
        bound = false

      }
    }

    val intent = Intent()
    intent.setClassName(











      "com.example.mobileissuer",
      "com.example.mobileissuer.IssuanceService"
    )

    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
  }
}