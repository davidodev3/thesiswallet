package com.example.mobileverifier

import kotlinx.serialization.Serializable

@Serializable
class DigitalCredentialRequestOptions(val requests: List<DigitalCredentialGetRequest>)

@Serializable
class DigitalCredentialGetRequest(
  private val protocol: String = "openid4vp-v1-unsigned",
  val data: String
)


@Serializable
class CredentialRequestOptions(val digital: DigitalCredentialRequestOptions) {
  companion object {
    fun fromAuthorizationRequests(requests: List<String>) : CredentialRequestOptions {
      val req = mutableListOf<DigitalCredentialGetRequest>()
      for (request in requests) {
        req.add(DigitalCredentialGetRequest(data = request))
      }
      return CredentialRequestOptions(DigitalCredentialRequestOptions(req))

    }
  }
}