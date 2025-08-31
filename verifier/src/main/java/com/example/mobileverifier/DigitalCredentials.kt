package com.example.mobileverifier

import kotlinx.serialization.Serializable

interface Credential {
  val id: String
  val type: String
}

interface DigitalCredential : Credential {
  val protocol: String
  val data: CustomAuthorizationResponse
}

@Serializable
class DigitalCredentialRequestOptions(val requests: List<DigitalCredentialGetRequest>)

@Serializable
class DigitalCredentialGetRequest(
  private val protocol: String = "openid4vp-v1-unsigned", val data: CustomAuthorizationRequest
)

@Serializable

class CredentialRequestOptions(val digital: DigitalCredentialRequestOptions) {
  companion object {
    fun fromAuthorizationRequests(requests: List<CustomAuthorizationRequest>): CredentialRequestOptions {
      val req = mutableListOf<DigitalCredentialGetRequest>()
      for (request in requests) {
        req.add(DigitalCredentialGetRequest(data = request))
      }
      return CredentialRequestOptions(DigitalCredentialRequestOptions(req))
    }

  }
}

@Serializable
class CustomDigitalCredential(











  override val id: String,
  override val type: String,
  override val protocol: String,

  override val data: CustomAuthorizationResponse
) : DigitalCredential {
  companion object {
    fun userAgentAllowsProtocol(protocol: String): Boolean {
      return protocol == "openid4vp-v1-unsigned"
    }
  }
}