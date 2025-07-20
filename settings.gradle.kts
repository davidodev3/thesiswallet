pluginManagement {
   repositories {
      google {
         content {
            includeGroupByRegex("com\\.android.*")
            includeGroupByRegex("com\\.google.*")
            includeGroupByRegex("androidx.*")
         }
      }
      mavenCentral()
      gradlePluginPortal()
   }
}
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      maven { url = uri("https://maven.waltid.dev/releases") }
      maven("https://maven.waltid.dev/snapshots")
   }
}

rootProject.name = "Mobile Wallet"
include(":app")
include(":issuer")
include(":verifier")
