import dependencies.Dep

plugins {
    id("kotlin")
}

dependencies {
    implementation(platform(Dep.Kotlin.bom))
    implementation(Dep.Kotlin.stdlib)
}

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}
