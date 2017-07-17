/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.ide.xcode

import org.gradle.ide.xcode.fixtures.XcodeProjectPackage
import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.nativeplatform.fixtures.app.SwiftHelloWorldApp

class XcodeSingleProjectIntegrationTest extends AbstractIntegrationSpec {

    def "test"() {
        given:
        buildFile << """
apply plugin: 'swift-executable'
apply plugin: 'xcode'
"""

        settingsFile << """
rootProject.name = "app"
"""

        def app = new SwiftHelloWorldApp()
        app.writeSources(file('src/main'))

        when:
        succeeds("xcode")

        then:
        executedAndNotSkipped(":xcodeProject", ":xcodeScheme", ":xcodeWorkspaceSettings", ":xcode")
        xcodeProject("app.xcodeproj").projectFile.mainGroup.children.size() == 5

    }

    private XcodeProjectPackage xcodeProject(String path) {
        new XcodeProjectPackage(file(path))
    }
}
