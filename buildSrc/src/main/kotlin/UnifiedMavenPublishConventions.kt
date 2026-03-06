import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

/**
 * Unified Maven Publishing Configuration using Vanniktech Maven Publish Plugin.
 *
 * Handles publishing to:
 * 1. Internal Nexus repository (snapshots/releases)
 * 2. Maven Central via Sonatype Central Portal
 *
 * Usage:
 * - Publish to Nexus: ./gradlew publishAllPublicationsToMavenSqlRepository
 * - Publish to Maven Central: ./gradlew publishAllPublicationsToMavenCentralRepository
 */
class UnifiedMavenPublishConventions : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply("com.vanniktech.maven.publish.base")

        // Register the DSL extension
        val extension = project.extensions.create<MavenPublishingExtension>("publishConfig")

        configureVersion(project)

        project.extensions.configure<MavenPublishBaseExtension> {
            val isSnapshot = project.ext<Boolean>("mavenPublishSnapshot")

            configureGpgSigning(project)

            project.afterEvaluate {
                // Check if project uses the war plugin
                val isWarProject = project.pluginManager.hasPlugin("war")

                if (isWarProject) {
                    // WAR projects: Use WarPublicationConfigurator (Vanniktech doesn't support WAR)
                    WarPublicationConfigurator().configure(project, extension.generateJavadoc.get())
                } else {
                    // JAR projects: Use Vanniktech's JavaLibrary platform
                    val javadocJar = if (extension.generateJavadoc.get()) JavadocJar.Javadoc() else JavadocJar.None()
                    configure(JavaLibrary(javadocJar = javadocJar, sourcesJar = true))
                }

                // Configure Maven Central if enabled (works for both JAR and WAR)
                if (extension.publishToMavenCentral.get() && !isSnapshot) {
                    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
                }

                // Configure POM - strict validation when publishing to Maven Central
                configurePom(project, this@configure, extension.publishToMavenCentral.get())

                // Configure Nexus repository if enabled (works for both JAR and WAR)
                if (extension.publishToNexus.get()) {
                    addNexusRepository(project, isSnapshot)
                }
            }
        }
    }

    private fun configureGpgSigning(project: Project) {
        if (project.prop("signing.gnupg.keyName") == null) return

        project.extensions.configure<MavenPublishBaseExtension> {
            signAllPublications()
            project.afterEvaluate {
                extensions.findByType(org.gradle.plugins.signing.SigningExtension::class.java)?.useGpgCmd()
            }
        }
    }

    private fun configureVersion(project: Project) {
        val commitTag = System.getProperty("commit.tag") ?: System.getenv("CI_COMMIT_TAG")
        val commitRefSlug = System.getProperty("commit.ref.slug") ?: System.getenv("CI_COMMIT_REF_SLUG")
        val baseVersion = project.version.toString()

        val version = when {
            !commitTag.isNullOrBlank() && commitTag.matches(SEMANTIC_VERSION_REGEX) -> commitTag.removePrefix("v")
            !commitTag.isNullOrBlank() -> "$baseVersion-$commitRefSlug"
            !commitRefSlug.isNullOrBlank() -> "$baseVersion-$commitRefSlug-SNAPSHOT"
            else -> baseVersion
        }

        project.version = version
        project.ext("mavenPublishSnapshot", version.endsWith("-SNAPSHOT"))
    }

    private fun configurePom(project: Project, extension: MavenPublishBaseExtension, requireStrictPom: Boolean) {
        extension.pom {
            name.set(project.name.takeIf { it.isNotBlank() } ?: project.prop("pom.name") ?: project.name)
            description.set(project.description?.takeIf { it.isNotBlank() } ?: project.prop("pom.description") ?: "")
            url.set(project.pomProp("pom.url", requireStrictPom))

            licenses {
                license {
                    name.set(project.pomProp("pom.license.name", requireStrictPom))
                    url.set(project.pomProp("pom.license.url", requireStrictPom))
                }
            }

            developers {
                developer {
                    id.set(project.pomProp("pom.developer.id", requireStrictPom))
                    name.set(project.pomProp("pom.developer.name", requireStrictPom))
                    email.set(project.pomProp("pom.developer.email", requireStrictPom))
                }
            }

            scm {
                connection.set(project.pomProp("pom.scm.connection", requireStrictPom))
                developerConnection.set(project.pomProp("pom.scm.developerConnection", requireStrictPom))
                url.set(project.pomProp("pom.scm.url", requireStrictPom))
            }
        }
    }

    private fun addNexusRepository(project: Project, isSnapshot: Boolean) {
        (project.extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension).repositories.maven {
            name = "MavenSql"
            url = project.uri(project.requireProp(if (isSnapshot) "nexus.snapshots.url" else "nexus.releases.url"))
            credentials {
                username = project.requireProp("mavenSqlUsername")
                password = project.requireProp("mavenSqlPassword")
            }
        }
    }

    private fun Project.prop(name: String): String? =
        System.getProperty(name) ?: findProperty(name) as? String

    private fun Project.requireProp(name: String): String =
        prop(name) ?: throw IllegalStateException(
            "Required property '$name' is not set. " +
            "Please define it in gradle.properties or pass it as a system property (-D$name=value)"
        )

    private fun Project.pomProp(name: String, required: Boolean): String =
        if (required) requireProp(name) else prop(name) ?: ""

    private fun Project.ext(name: String, value: Any) {
        extensions.extraProperties[name] = value
    }

    private inline fun <reified T> Project.ext(name: String): T =
        extensions.extraProperties[name] as T

    companion object {
        private val SEMANTIC_VERSION_REGEX = Regex(
            """^v?(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$"""
        )
    }
}