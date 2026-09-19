plugins {
    id("com.organizer.java-conventions")
    alias(libs.plugins.spring.boot)
}

group = "com.organizer"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":application"))

    implementation(libs.spring.boot.starter.graphql)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.webmvc)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.boot:spring-boot-starter-graphql-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework:spring-webflux")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> { useJUnitPlatform() }
