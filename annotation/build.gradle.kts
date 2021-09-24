import dependencies.Dep

plugins {
    id("kotlin")
    id("maven-publish")
}

dependencies {
    implementation(platform(Dep.Kotlin.bom))
    implementation(Dep.Kotlin.stdlib)
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
                artifactId = "annotation"
                version = "1.0.3"

                from(components["java"])
            }
        }
    }
}
