plugins {
    // Convention plugins from buildSrc are applied in subprojects
}

// Task to print the version for CI/CD pipelines
// Gets version from :api subproject which has the calculated version (with -SNAPSHOT suffix)
tasks.register("printVersion") {
    doLast {
        val apiProject = project.findProject(":api")
        if (apiProject != null) {
            println(apiProject.version)
        } else {
            println(project.version)
        }
    }
}

subprojects {
    // Apply Java conventions to all subprojects
    apply(plugin = "transconnect.java-conventions")
}