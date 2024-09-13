
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.dokka)
    id("com.osacky.doctor") version "0.9.1"
}

// Generate a mf FAT AHH JAR!
tasks.register<Jar>("fatJar") {
    archiveBaseName.set("provider-stubs")
    archiveClassifier.set("sources")
    destinationDirectory.set(File("build/stubs"))

    subprojects.forEach { project ->
        if (project.subprojects.size == 0) {
            val projectPath = "." + project.path.replace(":", "/")
            from("$projectPath/src/main/kotlin", "$projectPath/src/main/java")
        }
    }
}

/**
 *
 * Generate the stubs jar for the providers-system.
 *
 * Must only be run after the tasks:
 * - assembleRelease; and
 * - bundleReleaseClassesToCompileJar.
 * */
tasks.register<Jar>("generateStubsJar") {
    archiveBaseName.set("classes")
    archiveClassifier.set("")
    destinationDirectory.set(File("build/stubs"))
    dependsOn("fatJar")

    subprojects.forEach { project ->
        if (project.subprojects.size == 0) {
            val projectPath = "." + project.path.replace(":", "/")
            val classesJar = File("${projectPath}/build/intermediates/compile_app_classes_jar/release/classes.jar")

            if (classesJar.exists()) {
                from(zipTree(classesJar)) {
                    duplicatesStrategy = DuplicatesStrategy.INCLUDE
                }
            }
            else {
                from({
                    project.configurations.getByName("archives")
                        .allArtifacts.files
                        .filter { it.name.contains("release") }
                        .map(::zipTree)
                        .map { bundle ->
                            zipTree(bundle.files.first { it.name.endsWith("jar") })
                        }
                }) {
                    duplicatesStrategy = DuplicatesStrategy.INCLUDE
                }
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<DokkaTaskPartial>().configureEach {
        if (subprojects.size == 0) {
            moduleName.set(
                getModuleNameForDokka(project = this@subprojects)
            )
        }

        dokkaSourceSets.configureEach {
            documentedVisibilities.set(
                setOf(
                    DokkaConfiguration.Visibility.PUBLIC,
                    DokkaConfiguration.Visibility.PROTECTED
                )
            )

            sourceLink {
                val repository = "https://github.com/rhenwinch/Flixclusive/tree/master"

                localDirectory.set(rootProject.projectDir)
                remoteUrl.set(URL(repository))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

tasks.dokkaHtmlMultiModule {
    moduleName.set("Flixclusive Documentation")
}

fun getModuleNameForDokka(project: Project): String {
    var parentProject = project.parent
    val parentNames = arrayListOf("")
    while (!parentProject?.name.equals(rootProject.name)) {
        parentNames.add(parentProject?.name ?: "")
        parentProject = parentProject?.parent
    }

    val moduleNamePrefix = parentNames.reversed()
        .joinToString("-")

    return moduleNamePrefix + project.name
}