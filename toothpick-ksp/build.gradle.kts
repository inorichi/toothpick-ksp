plugins {
  kotlin("jvm")
}

dependencies {
  implementation("com.github.stephanenicolas.toothpick:toothpick:3.1.0")
  implementation("com.google.devtools.ksp:symbol-processing-api:1.5.31-1.0.0")
  implementation("com.squareup:kotlinpoet:1.10.1")
  implementation("com.squareup:kotlinpoet-ksp:1.10.1")
  implementation("javax.inject:javax.inject:1")

  testImplementation("com.github.stephanenicolas.toothpick:toothpick-testing-junit5:3.1.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.2")
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.2")
}

kotlin {
  sourceSets.all {
    languageSettings {
      optIn("kotlin.RequiresOptIn")
      optIn("com.google.devtools.ksp.KspExperimental")
      optIn("com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview")
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
