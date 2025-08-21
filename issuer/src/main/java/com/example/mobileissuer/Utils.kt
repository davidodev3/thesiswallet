package com.example.mobileissuer

import id.walt.crypto.keys.KeyType

import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.w3c.CredentialBuilder
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder

import kotlin.time.Duration.Companion.days

suspend fun generateCredential(
  type: String,
  map: Map<String, String>,
  issuer: String,
  key: String
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

    subjectDid = "did:key:z6MkmLUYGGZXTCAqq7PtavWYTD93B8mw3dkjL5e1PSqQRPTr" //TODO Hardcoded
    useCredentialSubject(map.toJsonObject())












  }.buildW3C()

  val signed = credential.signJws(

    JWKKey.importJWK(key).getOrNull()!!,  //The JWK was exported as a string to be saved as SharedPreference.
    issuer,
    subjectDid = "did:key:z6MkmLUYGGZXTCAqq7PtavWYTD93B8mw3dkjL5e1PSqQRPTr" //TODO
  )
  return signed
}

//Generate a key using Ed25519 algorithm and a DID using that key.
suspend fun generateKeyDid() : Pair<JWKKey, String> {

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
    this.grants = mutableMapOf("urn:ietf:params:oauth:grant-type:pre-authorized_code" to
      PreAuthorizedFlow(preAuthorizedCode = preAuthorizedCode)
    )
  }

  fun toOfferUrl() : String {
    val format = Json {explicitNulls = false}
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