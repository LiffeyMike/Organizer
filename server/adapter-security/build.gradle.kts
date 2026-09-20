plugins {
    id("com.organizer.java-conventions")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":core-config"))
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.assertj.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
