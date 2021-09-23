package dependencies

import org.gradle.api.JavaVersion

object Dep {

    object Kotlin {
        const val version = "1.5.0"
        const val bom = "org.jetbrains.kotlin:kotlin-bom:$version"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect"
    }

    object Misc {
        const val poet = "com.squareup:kotlinpoet:1.4.0"
        const val auto = "com.google.auto.service:auto-service:1.0-rc4"
    }

    val javaVersion = JavaVersion.VERSION_1_8
}
