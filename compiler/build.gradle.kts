import dependencies.Dep

plugins {
    id("kotlin")
    id("kotlin-kapt")
    id("maven-publish")
}

dependencies {
    implementation(project(":annotation"))

    implementation(platform(Dep.Kotlin.bom))
    implementation(Dep.Kotlin.stdlib)
    implementation(Dep.Kotlin.reflect)


    implementation(Dep.Misc.poet)
    implementation(Dep.Misc.auto)
    kapt(Dep.Misc.auto)
}

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.github.watanavex.fulla"
                artifactId = "compiler"
                version = "1.0.3"

                from(components["java"])
            }
        }
    }
}
