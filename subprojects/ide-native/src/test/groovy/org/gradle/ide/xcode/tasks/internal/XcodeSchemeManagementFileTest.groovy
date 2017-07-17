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

package org.gradle.ide.xcode.tasks.internal

import com.dd.plist.NSDictionary
import org.gradle.api.internal.PropertyListTransformer
import org.gradle.ide.xcode.fixtures.SchemeManagementFile
import org.gradle.ide.xcode.tasks.internal.XcodeSchemeManagementFile.SchemeUserState
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification

class XcodeSchemeManagementFileTest extends Specification{
    @Rule
    final TestNameTestDirectoryProvider testDirectoryProvider = new TestNameTestDirectoryProvider()

    def generator = new XcodeSchemeManagementFile(new PropertyListTransformer<NSDictionary>())

    def "setup"() {
        generator.loadDefaults()
    }

    def "empty scheme management file"() {
        expect:
        schemeManagementFile.schemes.empty
    }

    def "set auto create contexts if needed"() {
        when:
        generator.schemeUserStates.put("foo", newSchemeUserState(true, 3))

        then:
        def state = schemeManagementFile.schemeUserStates.get("foo")
        state != null
        state.visible == true
        state.orderHint == 3
    }

    private SchemeManagementFile getSchemeManagementFile() {
        def file = file("schemeManagement.plist")
        generator.store(file)
        return new SchemeManagementFile(file)
    }

    private TestFile file(String name) {
        testDirectoryProvider.testDirectory.file(name)
    }

    private static SchemeUserState newSchemeUserState(boolean visible, int orderHint) {
        def result = new SchemeUserState()
        result.visible = visible
        result.orderHint = orderHint
        return result
    }

}
