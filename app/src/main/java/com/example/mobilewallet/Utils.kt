package com.example.mobilewallet

import kotlinx.serialization.Contextual

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.net.URLEncoder

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