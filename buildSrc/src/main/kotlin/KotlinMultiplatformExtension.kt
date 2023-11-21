import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.allSupportedTargets() {
    jvm()

    iosTargets()
    nativeTargets()
}

private fun KotlinMultiplatformExtension.nativeTargets() {
    mingwX64()
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
}

fun KotlinMultiplatformExtension.iosTargets() {
    ios()
    iosSimulatorArm64()
    watchos()
    tvos()
}
