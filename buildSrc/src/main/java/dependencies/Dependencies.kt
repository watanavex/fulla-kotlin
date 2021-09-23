package dependencies

import org.gradle.api.JavaVersion

object Dep {

    object Kotlin {
        const val version = "1.5.0"
        const val bom = "org.jetbrains.kotlin:kotlin-bom:$version"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib"
    }

    val javaVersion = JavaVersion.VERSION_11
}
