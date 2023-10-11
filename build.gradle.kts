plugins {
  kotlin("jvm") version "1.8.10"
  id("io.gatling.gradle") version "3.9.5"
}

repositories { mavenCentral() }

dependencies {
  // use this to add dependencies to gatling
  fun add(s: String) {
    implementation(s)
    gatling(s)
    gatlingImplementation(s)
  }

  add("io.github.serpro69:kotlin-faker:1.10.0")
}
