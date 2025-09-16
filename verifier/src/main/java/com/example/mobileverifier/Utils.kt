package com.example.mobileverifier

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.credentials.CredentialManager
import androidx.credentials.DigitalCredential
import androidx.credentials.ExperimentalDigitalCredentialApi
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetDigitalCredentialOption
import androidx.credentials.exceptions.GetCredentialException
import id.walt.did.dids.DidService

import id.walt.policies.Verifier
import id.walt.policies.models.PolicyRequest
import id.walt.policies.policies.ExpirationDatePolicy
import id.walt.policies.policies.JwtSignaturePolicy
import id.walt.policies.policies.vp.HolderBindingPolicy
import id.walt.w3c.utils.VCFormat
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi


import kotlin.uuid.Uuid

suspend fun verify(presentation: String): Boolean {

  DidService.minimalInit()
  val vpPolicies = listOf(PolicyRequest(HolderBindingPolicy()))
  val globalPolicies = listOf(PolicyRequest(JwtSignaturePolicy()))

  val specificPolicies = mutableMapOf("Visa" to listOf(PolicyRequest(ExpirationDatePolicy())))

  val results = Verifier.verifyPresentation(
    VCFormat.jwt,
    presentation,
    vpPolicies,
    globalPolicies,
    specificPolicies,
  )
  Log.i("AAAAAA", results.results.toString())
  return results.overallSuccess()



}


@OptIn(ExperimentalDigitalCredentialApi::class, ExperimentalUuidApi::class)
suspend fun credentialRequest(
  usesAPI: Boolean, context: Context, launcher: ActivityResultLauncher<Intent>
): Boolean {

  val credentialManager = CredentialManager.create(context)

  /*OpenID for Verifiable Presentation has finally reached a final specification.
    For compatibility with the Android API manually generate the request JSON.
  */
  val nonce = Uuid.random().toString()

  //Building request
  val options = CredentialRequestOptions.fromAuthorizationRequests(

    listOf(CustomAuthorizationRequest(
      nonce = Uuid.random().toString(),
      responseType = "vp_token",
      query = DCQLQuery(
        listOf(DCQLCredential(
          "credential1",
          "jwt_vc_json",
          DCAPIMeta(listOf(listOf("UniversityDegree")))
        ))

      )
    ))

  )

  val request = Json.encodeToString(options)
  //This uses the experimental Digital Credentials API.
  if (usesAPI) {
    val digitalCredentialOptions = listOf(GetDigitalCredentialOption(request))

    val getCredentialRequest = GetCredentialRequest(digitalCredentialOptions)
    try {
      val credResult = credentialManager.getCredential(
        context, getCredentialRequest
      )
      val credential = credResult.credential
      if (credential is DigitalCredential) {
        return verify(credential.credentialJson) //TODO jwt
      }

    } catch (e: GetCredentialException) {
      Log.e("Verifier", "Error", e) //TODO fine grained exception handling method
    }
  }

  //Send the request mimicking the DCAPI
  val sendIntent = Intent().apply {
    action = Intent.ACTION_SEND
    //Our request is a JSON string compliant with Digital Credentials API.

    putExtra(Intent.EXTRA_TEXT, request)
    type = "application/json"
    setPackage("com.example.mobilewallet") //Only targets our wallet app
  }

  launcher.launch(sendIntent)
  return false
}