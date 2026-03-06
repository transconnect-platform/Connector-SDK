import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Extension for configuring Maven publishing targets.
 *
 * Usage:
 * ```
 * publishConfig {
 *     publishToNexus.set(true)
 *     publishToMavenCentral.set(false)
 *     generateJavadoc.set(false)  // Disable Javadoc generation
 * }
 * ```
 */
abstract class MavenPublishingExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Whether to publish to Nexus repository.
     * Default: true
     */
    val publishToNexus: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    /**
     * Whether to publish to Maven Central.
     * Default: false (opt-in)
     */
    val publishToMavenCentral: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * Whether to generate Javadoc JAR.
     * Default: true
     */
    val generateJavadoc: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
}