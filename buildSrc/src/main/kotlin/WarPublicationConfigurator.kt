import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * Configures Maven publication for WAR (Web Application Archive) projects.
 *
 * ## Why This Exists
 *
 * Vanniktech Maven Publish plugin does NOT support WAR files - its `Platform` class is sealed
 * and cannot be extended. Additionally, Gradle's `web` component has a bug (since 2019, still
 * exists in Gradle 8.8) where it doesn't include dependencies in the POM file.
 *
 * This class uses standard Gradle `maven-publish` APIs to:
 * - Publish WAR file via `web` component
 * - Add classes JAR (for compile dependencies) and sources JAR
 * - Manually inject dependencies into POM (workaround for Gradle bug - still required!)
 *
 * ## References
 * - [Vanniktech custom artifacts issue](https://github.com/vanniktech/gradle-maven-publish-plugin/issues/747)
 * - [Gradle WAR dependencies bug from 2019](https://discuss.gradle.org/t/why-do-wars-have-no-dependencies-with-maven-publish/33513)
 */
class WarPublicationConfigurator {

    fun configure(project: Project, generateJavadoc: Boolean) {
        project.pluginManager.apply("maven-publish")

        val javaExtension = project.extensions.getByType<JavaPluginExtension>()
        val mainSourceSet = javaExtension.sourceSets.getByName("main")

        val classesJar = project.tasks.register<Jar>("classesJar") {
            archiveClassifier.set("classes")
            from(mainSourceSet.output)
        }

        val sourcesJar = project.tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(mainSourceSet.allSource)
        }

        project.extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(project.components["web"])
                    artifact(classesJar)
                    if (generateJavadoc) artifact(sourcesJar)

                    pom.withXml {
                        val deps = asNode().appendNode("dependencies")

                        project.configurations.getByName("implementation").dependencies.forEach {
                            addDependency(deps, it, "runtime")
                        }

                        project.configurations.findByName("providedCompile")?.dependencies?.forEach {
                            addDependency(deps, it, "provided")
                        }
                    }
                }
            }
        }
    }

    private fun addDependency(parent: groovy.util.Node, dep: org.gradle.api.artifacts.Dependency, scope: String) {
        val node = parent.appendNode("dependency")
        node.appendNode("groupId", dep.group)
        node.appendNode("artifactId", dep.name)
        node.appendNode("version", dep.version)
        node.appendNode("scope", scope)
    }
}