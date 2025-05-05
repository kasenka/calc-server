plugins {
    id("java")
    id("application")
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass.set("org.example.calc_server.CalcServerApplication")
}

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.yaml:snakeyaml:2.2")


    implementation("org.springframework.boot:spring-boot-starter")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")



    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("net.datafaker:datafaker:2.0.1")
    implementation("org.instancio:instancio-junit:3.3.0")
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.10.2")


//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("jakarta.validation:jakarta.validation-api:3.0.0")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.postgresql:postgresql")

    implementation("org.springframework.boot:spring-boot-starter-security")

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}

tasks.test{
    useJUnitPlatform()
}
