/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.composite

import org.gradle.integtests.fixtures.build.BuildTestFile
import org.gradle.util.Matchers
import spock.lang.Ignore
/**
 * Tests for plugin development scenarios within a composite build.
 */
class CompositeBuildPluginDevelopmentIntegrationTest extends AbstractCompositeBuildIntegrationTest {
    BuildTestFile pluginBuild
    BuildTestFile pluginDependencyA

    def setup() {
        pluginDependencyA = singleProjectBuild("pluginDependencyA") {
            buildFile << """
                apply plugin: 'java'
                version "2.0"
"""
        }

        pluginBuild = pluginProjectBuild("pluginBuild")
    }

    def "can co-develop plugin and consumer with plugin as included build"() {
        given:
        applyPlugin(buildA)

        includeBuild pluginBuild

        when:
        execute(buildA, "tasks")

        then:
        executed ":pluginBuild:jar"
        outputContains("taskFromPluginBuild")
    }

    def "can co-develop plugin and consumer with both plugin and consumer as included builds"() {
        given:
        applyPlugin(pluginDependencyA)

        buildA.buildFile << """
            dependencies {
                compile "org.test:pluginDependencyA:1.0"
            }
"""

        includeBuild pluginDependencyA, """
            substitute module("org.test:pluginDependencyA") with project(":")
"""
        includeBuild pluginBuild

        when:
        execute(buildA, "assemble")

        then:
        executed ":pluginBuild:jar", ":pluginDependencyA:jar", ":jar"
    }

    def "can co-develop plugin and consumer where plugin uses previous version of itself to build"() {
        given:
        // Ensure that 'plugin' is published with older version
        mavenRepo.module("org.test", "pluginBuild", "0.1").publish()

        pluginBuild.buildFile << """
            buildscript {
                repositories {
                    repositories {
                        maven { url "${mavenRepo.uri}" }
                    }
                }
                dependencies {
                    classpath 'org.test:pluginBuild:0.1'
                }
            }
"""

        applyPlugin(buildA)

        includeBuild pluginBuild, """
            // Only substitute version 1.0 with project dependency. This allows this project to build with the published dependency.
            substitute module("org.test:pluginBuild:1.0") with project(":")
"""

        when:
        execute(buildA, "tasks")

        then:
        executed ":pluginBuild:jar"
        outputContains("taskFromPluginBuild")
    }

    def "can develop a transitive plugin dependency as included build"() {
        given:
        applyPlugin(buildA)
        dependency(pluginBuild, "org.test:pluginDependencyA:1.0")

        includeBuild pluginBuild
        includeBuild pluginDependencyA

        when:
        execute(buildA, "taskFromPluginBuild")

        then:
        executed ":pluginDependencyA:jar", ":pluginBuild:jar", ":taskFromPluginBuild"
    }


    def "can develop a transitive plugin dependency as included build when plugin itself is not included"() {
        given:
        publishPluginWithDependency()

        buildA.buildFile << """
            buildscript {
                repositories {
                    repositories {
                        maven { url "${mavenRepo.uri}" }
                    }
                }
            }
"""
        applyPlugin(buildA)

        when:
        includeBuild pluginDependencyA
        execute(buildA, "taskFromPluginBuild")

        then:
        executed ":pluginDependencyA:jar", ":taskFromPluginBuild"
        notExecuted ":pluginBuild:jar"
    }

    private void publishPluginWithDependency() {
        dependency pluginBuild, 'org.test:pluginDependencyA:1.0'
        pluginBuild.buildFile << """
publishing {
    repositories {
        maven {
            url '${mavenRepo.uri}'
        }
    }
}
"""
        executer.inDirectory(pluginBuild).withArguments('--include-build', pluginDependencyA.absolutePath).withTasks('publish').run()
    }

    private void publishPlugin() {
        pluginBuild.buildFile << """
publishing {
    repositories {
        maven {
            url '${mavenRepo.uri}'
        }
    }
}
"""
        executer.inDirectory(pluginBuild).withTasks('publish').run()
    }

// TODO:DAZ Fix this: https://builds.gradle.org/viewLog.html?buildId=4295932&buildTypeId=Gradle_Check_NoDaemon_Java8_Oracle_Linux_compositeBuilds
    @Ignore("Cycle check is not parallel safe: test may hang or produce StackOverflowError")
    def "detects dependency cycle between included builds required for buildscript classpath"() {
        given:
        def pluginDependencyB = singleProjectBuild("pluginDependencyB") {
            buildFile << """
                apply plugin: 'java'
                version "2.0"
"""
        }

        dependency pluginBuild, "org.test:pluginDependencyA:1.0"
        dependency pluginDependencyA, "org.test:pluginDependencyB:1.0"
        dependency pluginDependencyB, "org.test:pluginDependencyA:1.0"

        applyPlugin(buildA)

        includeBuild pluginBuild
        includeBuild pluginDependencyA
        includeBuild pluginDependencyB

        when:
        fails(buildA, "tasks")

        then:
        failure
            .assertHasDescription("Could not determine the dependencies of task")
            .assertHasCause("Included build dependency cycle:")
            .assertThatCause(Matchers.containsText("build 'pluginDependencyA' -> build 'pluginDependencyB'"))
            .assertThatCause(Matchers.containsText("build 'pluginDependencyB' -> build 'pluginDependencyA'"))
    }

    def "can co-develop unpublished plugin applied via plugins block"() {
        given:
        addPluginsBlock(buildA, """
            resolutionStrategy.eachPlugin {
                if(requested.id.name == 'pluginBuild') {
                    useModule('org.test:pluginBuild:1.0')
                }
            }
        """)


        when:
        execute(buildA, "tasks", ["--include-build", "../pluginBuild"])

        then:
        executed ":pluginBuild:jar"
        outputContains("taskFromPluginBuild")


        when:
        includeBuild pluginBuild
        execute(buildA, "tasks")

        then:
        executed ":pluginBuild:jar"
        outputContains("taskFromPluginBuild")
    }

    def "can co-develop published plugin applied via plugins block"() {
        given:
        publishPlugin()
        addPluginsBlock(buildA)

        when:
        execute(buildA, "tasks", ["--include-build", "../pluginBuild"])

        then:
        executed ":pluginBuild:jar"
        outputContains("taskFromPluginBuild")


        when:
        includeBuild pluginBuild
        execute(buildA, "tasks")

        then:
        executed ":pluginBuild:jar"
        outputContains("taskFromPluginBuild")
    }

    def addPluginsBlock(BuildTestFile build, String resolutionStrategy = "") {
        build.settingsFile.text = """
            pluginManagement {
                $resolutionStrategy
                repositories {
                    maven { url '${mavenRepo.uri}' }
                }
            }
""" + build.settingsFile.text

        build.buildFile.text = """
            plugins {
                id 'org.test.plugin.pluginBuild' version '1.0'
            }
""" + build.buildFile.text
    }

}
