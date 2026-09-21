plugins {
    id("com.organizer.java-conventions")
}

dependencies {
    api(project(":core-config"))

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
