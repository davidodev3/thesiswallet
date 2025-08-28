package com.example.mobileverifier

import kotlinx.serialization.SerialName

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

@Serializable
class CustomAuthorizationRequest(
  @SerialName("client_id")
  val clientId: String? = null,
  @SerialName("response_type")

  val responseType: String,
  @SerialName("response_mode")
  private val responseMode: String = "dc_api",
  val nonce: String,
  @SerialName("dcql_query")
  val query: DCQLQuery
) {

  fun toUnsignedJWT() : String{

    val format = Json {explicitNulls = false}
    val header = format.encodeToString(mutableMapOf("alg" to "none", "typ" to "oauth-authz-req+jwt"))
    val payload = format.encodeToString(this)

    //Return request as unsigned JWT
    return (Base64.Default.encode(header.encodeToByteArray()) +
            "." +
            Base64.Default.encode(payload.encodeToByteArray()) + "..")
  }

}

@Serializable
class DCQLQuery(
  val credentials: List<DCQLCredential>











)

@Serializable

class DCQLCredential (
  @SerialName("id")
  val credentialId: String,
  val format: String,
  val meta: DCAPIMeta
)

@Serializable
class DCAPIMeta (

  @SerialName("type_values")

  val typeValues: List<List<String>>
)

@Serializable
class A

@Serializable

class CustomAuthorizationResponse(
  val response: String
) {
  companion object {
    fun fromCredentialMapping(token: Map<String, List<String>>) : CustomAuthorizationResponse {
      return CustomAuthorizationResponse("vp_token=${Json.encodeToString(token)}")
    }
  }
}