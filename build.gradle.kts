// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.spotless)
}

val ktlintOverrides = mapOf(
    "max_line_length" to "off",
    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
    "ktlint_standard_property-naming" to "disabled",
    "ktlint_standard_filename" to "disabled",
    "ktlint_standard_kdoc" to "disabled",
    "ktlint_standard_max-line-length" to "disabled",
)

spotless {
    kotlin {
        target("app/src/**/*.kt", "benchmark/src/**/*.kt")
        targetExclude("**/build/**", "**/generated/**")
        ktlint().editorConfigOverride(ktlintOverrides)
    }
    kotlinGradle {
        target("*.gradle.kts", "app/*.gradle.kts", "benchmark/*.gradle.kts")
        ktlint().editorConfigOverride(ktlintOverrides)
    }
}
