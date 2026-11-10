plugins {
    `java-library`
    id("eclipse")
    id("io.spring.dependency-management")
    id("com.diffplug.spotless")
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.1")
    }
}

spotless {
    java {
        googleJavaFormat("2.0.0")
    }
}

tasks.withType<Test> { useJUnitPlatform() }
tasks.withType<JavaCompile> { options.compilerArgs.add("-parameters") }
