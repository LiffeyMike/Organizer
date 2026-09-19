plugins {
    id("com.organizer.java-conventions")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":core-config"))
    implementation(libs.spring.boot.starter.graphql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.boot:spring-boot-starter-graphql-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
