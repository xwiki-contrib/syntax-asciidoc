/*
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
plugins {
    `java-library`
    `maven-publish`
    groovy
    id("com.github.joschi.licenser") version "0.6.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.xwiki.commons.componentApi)
    implementation(libs.xwiki.rendering.api)
    implementation(libs.asciidoctorj)

    runtimeOnly(libs.xwiki.rendering.syntax.plain)
    testImplementation(libs.bundles.test.commonLibraries)
    testImplementation(libs.test.xwiki.commons.tool.testComponent)
}

group = "org.xwiki.contrib"
version = "1.0-SNAPSHOT"
description = "XWiki Macro - Hello World Component"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

publishing {
    repositories {
        maven {
            name = "test"
            url = uri("$buildDir/repo")
        }
    }
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

license {
    header = rootProject.file("HEADER")
    exclude("**/*.adoc")
    exclude("build/**")
    exclude("target/**")
}