plugins {
    id("java")
//    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.14.2"
//    id("org.gradle.ivy-publish")
}


group = "com.xxxx"
version = "1.2"

repositories {
//    mavenCentral()
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    localPath.set("D:\\javaaaa\\idea\\IntelliJ IDEA 2023.2")
//    version.set("2022.2.5")
//    type.set("IC") // Target IDE Platform

//    plugins.set(listOf(/* Plugin Dependencies */))
    //引入['java','gradle']
//    plugins.set(listOf("java", "gradle"))
    plugins.add("java")
    plugins.add("gradle")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
//    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = "17"
//    }
    withType<JavaExec> {
        jvmArgs = listOf("-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8")
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    
}

dependencies { 
//    compileOnly("com.intellij:openapi:7.0.3")
    // IntelliJ 2023.2 对应 API 版本通常是 232.x 系列
//    compileOnly("com.intellij.platform:core-impl:232.8660.185") // 核心实现库
//    compileOnly("com.intellij:openapi:232.8660.185")   // 替换原 openapi 依赖


}
