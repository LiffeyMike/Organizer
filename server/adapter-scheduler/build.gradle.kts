plugins {
    id("com.organizer.java-conventions")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":core-config"))
}
