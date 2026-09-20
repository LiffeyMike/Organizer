
pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "organizer"

include("core-config")
include("domain")
include("application")
include("adapter-graphql")
include("adapter-persistence")
include("app")
