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

package org.gradle.ide.xcode.fixtures

import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.PropertyListParser
import com.google.common.base.Preconditions
import org.gradle.ide.xcode.tasks.internal.XcodeWorkspaceSettingsFile
import org.gradle.test.fixtures.file.TestFile

class WorkspaceSettingsFile {
    String name
    File file
    NSDictionary plist

    WorkspaceSettingsFile(TestFile file) {
        assert file.exists()
        this.file = file
        this.name = file.name.replace(".xcsettings", "")
        this.plist = PropertyListParser.parse(file)
    }

    boolean isAutoCreateContextsIfNeeded() {
        def value = Preconditions.checkNotNull(plist.get(XcodeWorkspaceSettingsFile.AUTO_CREATE_CONTEXTS_IF_NEEDED_KEY))
        Preconditions.checkArgument(value instanceof NSNumber && value.isBoolean())

        return ((NSNumber)value).boolValue()
    }

    void assertMissingAutoCreateContextsIfNeeded() {
        assert plist.containsKey(XcodeWorkspaceSettingsFile.AUTO_CREATE_CONTEXTS_IF_NEEDED_KEY)
    }
}
