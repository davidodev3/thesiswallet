# Mobile Wallet
This project was made for the final thesis (3 ECTS) of the Computer Engineering Bachelor's Degree course at the University of Padua. It's a hypothetical mobile implementation of the issuance and presentation flows of Verifiable Credentials following the "OpenID for Verifiable Credentials" protocols.


## Installation
This project is configured by default to work in a localhost environment using an Android emulator.
### Mobile applications
Clone this repository and open it with Android Studio. Run all three app modules with an emulator running the latest version of the Android SDK.
### Server
Refer to [issuerserver](https://github.com/davidodev3/issuerserver).

## Usage


### Issuance 
First of all, open the Wallet and create a new wallet by tapping on the plus (+) button at the bottom right of the screen.

Press the "Request credentials" button in the middle portion of the screen and wait for the Issuer to start.

Select the desired credential type by tapping on the respective sections, fill in the forms with the credential's data and press "Generate".

Press "Accept" in the rendered Wallet view and select the wallet in which you want to store the credential.


The received credential will be displayed.

### Presentation
Open the Verifier and press the button labeled "Custom request": this will open the Wallet and show all credentials of type "UniversityDegree". 

Select a credential among those displayed.

Press verify and wait for the result.


## License
This software uses the [walt.id](https://walt.id/) Community Edition SDKs, okhttp and Nimbus JOSE + JWT, which are all distributed under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).