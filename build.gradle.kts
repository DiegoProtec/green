import java.util.*

plugins {
    java
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkiverse.helm:quarkus-helm:1.2.1")
    implementation("io.quarkus:quarkus-kind")
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-spring-cloud-config-client")
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-arc")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "org.contecin"
version = "0.0.4"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.register<Exec>("docker-login") {
    group = "Docker"
    description = "Login in docker registry"
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
        commandLine("cmd", "/c", "echo %GITHUB_TOKEN% | docker login %IMAGE_REGISTRY% -u %IMAGE_USER% --password-stdin")
    } else {
        commandLine("bash", "-c", "echo \"\$GITHUB_TOKEN\" | docker login \$IMAGE_REGISTRY -u \$IMAGE_USER --password-stdin")
    }
}

tasks.register("copy-templates") {
    group = "Helm"
    description = "Copia charts do projeto para a pasta de package do chart"
    doLast {
        copy {
            from(layout.projectDirectory.dir("src/main/helm/templates"))
            into(layout.projectDirectory.dir("build/helm/kind/green/templates"))
            include("**/*.yaml")
        }
    }
}

tasks.register<Exec>("package-helm") {
    group = "Helm"
    description = "Empacota charts para arquivo .tar.gz"
    workingDir(layout.projectDirectory)
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
        commandLine("cmd", "/c", "helm package build\\helm\\kind\\green")
    } else {
        commandLine("bash", "-c", "helm package build/helm/kind/green")
    }
}

tasks.register<Exec>("template-helm") {
    group = "Helm"
    description = "Exibe tempalte helm"
    workingDir(layout.projectDirectory)
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
        commandLine("cmd", "/c", "helm template green-0.0.5.tgz --debug")
    } else {
        commandLine("bash", "-c", "helm template green-0.0.5.tgz --debug")
    }
}

tasks.register<Exec>("push-helm") {
    group = "Helm"
    description = "Instala os manifestos no k8s"
    workingDir(layout.projectDirectory)
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
        commandLine("cmd", "/c", "helm cm-push green-0.0.5.tgz chartmuseum")
    } else {
        commandLine("bash", "-c", "helm cm-push green-0.0.5.tgz chartmuseum")
    }
}

tasks.register<Exec>("update-helm") {
    group = "Helm"
    description = "Instala os manifestos no k8s"
    workingDir(layout.projectDirectory.dir("build/helm/kind/green"))
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
        commandLine("cmd", "/c", "helm repo update")
    } else {
        commandLine("bash", "-c", "helm repo update")
    }
}

tasks.register<Exec>("upgrade-helm") {
    group = "Helm"
    description = "Instala os manifestos no k8s"
    workingDir(layout.projectDirectory.dir("build/helm/kind/green"))
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) {
        commandLine("cmd", "/c", "helm upgrade green chartmuseum/green --version=0.0.5 -i -n contecin --create-namespace --cleanup-on-fail --debug --wait --timeout 1m30s -f C:\\Users\\diego\\IdeaProjects\\green\\src\\main\\helm\\values.yaml")
    } else {
        commandLine("bash", "-c", "helm upgrade green chartmuseum/green --version=v0.0.5 -i -n contecin --create-namespace --cleanup-on-fail --debug --wait --timeout 1m30s -f C:/Users/diego/IdeaProjects/green/src/main/helm/values.yaml")
    }
}

//tasks.named("build") { dependsOn("docker-login") }
//tasks.named("build") { finalizedBy("copy-templates") }
//tasks.named("copy-templates") { finalizedBy("package-helm") }
//tasks.named("package-helm") { finalizedBy("template-helm") }
//tasks.named("template-helm") { finalizedBy("push-helm") }
//tasks.named("push-helm") { finalizedBy("update-helm") }