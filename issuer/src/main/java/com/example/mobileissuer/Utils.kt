package com.example.mobileissuer

import android.util.Log
import id.walt.crypto.keys.KeyType

import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.w3c.CredentialBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.Base64
import kotlin.time.Duration.Companion.days


fun generateCredential(
  type: String,
  map: Map<String, String>,
  issuer: String,
  key: String,
  subject: String,
  valid: String? = null,
): String {
  val credential = CredentialBuilder().apply {

    //UniversityDegree is defined in the examples.
    if (type == "UniversityDegree") addContext("https://www.w3.org/2018/credentials/examples/v1")

    if (type == "Visa") {
      addType("VerifiableAttestation")











      validFor(Integer.parseInt(map["duration"] ?: "90").days)
    }
    validFromNow() //Validity starts on issuance date and time for other credentials.

    addType(type)
    issuerDid = issuer
    subjectDid = subject
    useCredentialSubject(map.toJsonObject())
  }.buildW3C()

  val signed = credential.signJwsBlocking(
    JWKKey.importJWKBlocking(key)
      .getOrNull()!!,  //The JWK was exported as a string to be saved as SharedPreference.

    issuer,

    subjectDid = subject
  )
  return signed
}


//Generate a key using Ed25519 algorithm and a DID using that key.

suspend fun generateKeyDid(): Pair<JWKKey, String> {

  DidService.minimalInit()
  val key = JWKKey.generate(KeyType.Ed25519)
  val did = DidService.registerByKey("key", key).did
  return Pair(key, did)
}

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
    this.grants = mutableMapOf(
      "urn:ietf:params:oauth:grant-type:pre-authorized_code" to
              PreAuthorizedFlow(preAuthorizedCode = preAuthorizedCode)

    )

  }

  fun toOfferUrl(): String {
    val format = Json { explicitNulls = false }
    val payload = format.encodeToString(this)

    return "openid-credential-offer://?credential_offer=" + URLEncoder.encode(payload, "utf-8")

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

fun tokenToPayload(jwt: String): JsonObject {












  val decoder = Base64.getUrlDecoder()
  //The actual content of the encoded JSON is in the second part, the one after the first dot. Decode the JWT and then convert the resulting JSON string into an object.
  return Json.parseToJsonElement(decoder.decode(jwt.split(".")[1]).decodeToString()).jsonObject

}

suspend fun addSessionRequest(id: String, serverUrl: String) {
  val client = OkHttpClient()

  val request = Request.Builder()

    .post("session=$id".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
    .url("$serverUrl/session")
    .build()
  withContext(Dispatchers.IO) {
    client.newCall(request).execute()
  }

}