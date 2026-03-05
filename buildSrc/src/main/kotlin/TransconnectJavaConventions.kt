import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import java.nio.charset.StandardCharsets

/**
 * Convention plugin that applies common Java configuration to all subprojects.
 * This includes:
 * - Java toolchain configuration (Java 17)
 * - Testing framework configuration (TestNG/JUnit)
 * - Checkstyle configuration
 * - JaCoCo coverage configuration
 * - Spotless code formatting
 * - Reproducible build settings
 */
class TransconnectJavaConventions : Plugin<Project> {

    override fun apply(project: Project) {
        // Apply common plugins
        project.plugins.apply("java")
        project.plugins.apply("checkstyle")
        project.plugins.apply(JacocoPlugin::class.java)
        project.plugins.apply("io.freefair.lombok")
        project.plugins.apply("com.diffplug.spotless")

        // Configure Java toolchain
        configureJavaToolchain(project)

        // Configure testing after evaluation (to detect TestNG vs JUnit)
        project.afterEvaluate {
            configureTesting(project)
        }

        // Configure Checkstyle
        configureCheckstyle(project)

        // Configure JaCoCo
        configureJacoco(project)

        // Configure reproducible builds
        configureReproducibleBuilds(project)

        // Configure Spotless
        configureSpotless(project)
    }

    private fun configureJavaToolchain(project: Project) {
        project.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }
    }

    private fun configureTesting(project: Project) {
        // Configure test framework based on dependencies
        // TestNG projects: api, war-connector-bridge, extensions/yaml-descriptor, extensions/proxy-properties
        // JUnit projects: all others
        val hasTestNG = project.configurations.getByName("testImplementation").allDependencies.any {
            it.name == "testng"
        }

        // For JUnit projects, add junit-platform-launcher to fix version mismatch with Gradle's bundled version
        if (!hasTestNG) {
            val versionCatalog = project.extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                .named("libs")
            val junitSuiteVersion = versionCatalog.findVersion("junit-suite")
                .orElseThrow { IllegalStateException("junit-suite version not found in version catalog") }
            project.dependencies.add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher:${junitSuiteVersion.requiredVersion}")
        }

        project.tasks.withType<Test>().configureEach {
            if (hasTestNG) {
                useTestNG {
                    // Prevent suite-level failures
                    preserveOrder = true
                    groupByInstances = true
                }
            } else {
                useJUnitPlatform()
            }
            finalizedBy(project.tasks.named("jacocoTestReport"))
        }
    }

    private fun configureCheckstyle(project: Project) {
        project.tasks.withType<org.gradle.api.plugins.quality.Checkstyle>().configureEach {
            reports {
                xml.required = false
                html.required = true
            }
            include("src/main/java/**/*.java")
            exclude("generated/**/*.java")
        }
    }

    private fun configureJacoco(project: Project) {
        project.tasks.withType<JacocoReport>().configureEach {
            reports {
                xml.required = true
                html.required = true
                csv.required = false
                html.outputLocation = project.layout.buildDirectory.dir("jacocoHtml")
            }
            finalizedBy(project.tasks.named("jacocoTestCoverageVerification"))
        }

        project.tasks.withType<JacocoCoverageVerification>().configureEach {
            violationRules {
                rule {
                    limit {
                        // currently the coverage is very low because of the generated code
                        minimum = "0.0".toBigDecimal()
                    }
                }
            }
        }
    }

    private fun configureReproducibleBuilds(project: Project) {
        // see https://github.com/palantir/gradle-baseline?tab=readme-ov-file#compalantirbaseline-reproducibility
        project.tasks.withType<AbstractArchiveTask>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }

        project.tasks.withType<Jar>().configureEach {
            // Ensure reproducible builds
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
    }

    private fun configureSpotless(project: Project) {
        project.extensions.configure(com.diffplug.gradle.spotless.SpotlessExtension::class.java) {
            encoding = StandardCharsets.UTF_8
            lineEndings = com.diffplug.spotless.LineEnding.UNIX
            java {
                target(project.fileTree("src/main") {
                    include("**/*.java")
                })

                // Get version from version catalog
                val versionCatalog = project.extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                    .named("libs")
                val palantirVersion = versionCatalog.findVersion("palantirJavaFormatter")
                    .orElseThrow { IllegalStateException("palantirJavaFormatter version not found in version catalog") }

                palantirJavaFormat(palantirVersion.requiredVersion)
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
            }
        }

        project.tasks.named("spotlessCheck") {
            inputs.files(project.fileTree("src/main") {
                include("**/*.java")
            })
        }
    }
}