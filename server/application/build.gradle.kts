plugins {
    id("com.organizer.java-conventions")
}

dependencies {
    api(project(":domain"))
    api(project(":core-config"))
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
