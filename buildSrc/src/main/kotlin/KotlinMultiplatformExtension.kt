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
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
}
