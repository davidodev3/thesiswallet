package com.example.mobileissuer

import id.walt.crypto.keys.KeyType

import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.w3c.CredentialBuilder
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

      validFor(90.days) //TODO: for now add default value of three months which is pretty much the standard for tourist visas
    }
    addType(type)
    issuerDid = issuer
    validFromNow()
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
