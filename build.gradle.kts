
import java.io.PrintStream

plugins {
    // Plugin to do Java (shocking!)
    java

    // Plugin to have a "run" task basically
    application

    // This plugin doesn't seem to useful for now...
    // id("org.javamodularity.moduleplugin") version "1.8.12"

    // Plugin to automatically configure JavaFX dependencies
    id("org.openjfx.javafxplugin") version "0.1.0"

    // Plugin to make installer/standalone executables
    id("org.beryx.jlink") version "3.1.1"
}

group = "fr.connexe"
version = "1.0-SNAPSHOT"

repositories {
    // Download dependencies from Maven Central
    mavenCentral()
}

java {
    toolchain {
        // Require a Java version of 24 for running the app. (Gradle may run on an older version of the JVM)
        languageVersion = JavaLanguageVersion.of(24)
    }

    // Configure the Java compiler to use the latest languages features of Java 24.
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24

    // Make sure the module path is set correctly when launching the app.
    modularity.inferModulePath.set(true)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8" // UTF-8 always
}

application {
    // The main module and class of the "run" task
    mainModule.set("fr.connexe")
    mainClass.set("fr.connexe.ConnexeApp")
}

javafx {
    // Automatically fetch the dependencies for JavaFX 24.0.1, with the controls and fxml modules.
    version = "24.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // Include the "sdl2gdx" library for SDL2 controller support.
    implementation(files(layout.projectDirectory.dir("external").file("sdl2gdx-1.0.5.jar")))

    // Include JetBrains annotations for @Nullable.
    implementation("org.jetbrains:annotations:26.0.2")
}

jlink {
    // Configuration for the standalone executables which bundle a customized JVM with only the necessary modules.
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}

// Task to run the ConsoleApp class.
// ./gradlew runConsole --console=plain
tasks.register<JavaExec>("runConsole") {
    description = "Runs the console application"
    group = "application"

    mainModule.set("fr.connexe")
    mainClass.set("fr.connexe.ConsoleApp")

    classpath = sourceSets.main.get().runtimeClasspath

    // Make sure it uses our java toolchain (Java 24)
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))

    standardInput = System.`in`
    standardOutput = System.out
}

// Configure the "run" task. Must be AFTER the "application" block for some reason?!
tasks.withType<JavaExec> {
    jvmArgs(
        // Remove WARNINGS for javafx.graphics who's using restricted methods and unsafe
        // https://bugs.openjdk.org/browse/JDK-8347744
        "--enable-native-access=javafx.graphics",
        // https://bugs.openjdk.org/browse/JDK-8345121
        "--sun-misc-unsafe-memory-access=allow",
        // Enable JNI for SDL2
        "--enable-native-access=sdl2gdx",
        // Use UTF-8 for stdout (System.out) and stderr (System.err) because, weirdly,
        // Gradle seems to use whatever encoding it wants for the console on Windows,
        // which is literally OVERRIDING what Java 18 has done to make UTF-8 the default on Windows.
        // See https://github.com/gradle/gradle/issues/29356
        "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
        "-ea" // Enable assertions
    )

    systemProperty("file.encoding", "UTF-8")
    systemProperty("console.encoding", "UTF-8")
}

// Configure the Javadoc task to add links to JavaFX documentation and neat syntax highlighting
// (Note: recycled from https://github.com/ChuechTeam/Domotique/blob/main/build.gradle.kts#L84-L100)
tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).apply {
        // Link to the JavaFX Javadoc
        links = listOf("https://openjfx.io/javadoc/24/")

        // Add Highlight.js for code syntax highlighting in documentation
        bottom = """
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/default.min.css">
            <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/languages/go.min.js"></script>
            <script>hljs.highlightAll();</script>
        """.trimIndent().replace("\n", "")

        // No idea why this is needed for "bottom" to work properly. Allow script in COMMENTS?!
        addBooleanOption("-allow-script-in-comments", true)
    }

}