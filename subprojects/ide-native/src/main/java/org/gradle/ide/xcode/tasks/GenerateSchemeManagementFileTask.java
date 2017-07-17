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

package org.gradle.ide.xcode.tasks;

import org.gradle.api.Incubating;
import org.gradle.ide.xcode.XcodeScheme;
import org.gradle.ide.xcode.tasks.internal.XcodeSchemeManagementFile;
import org.gradle.plugins.ide.api.PropertyListGeneratorTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 4.2
 */
@Incubating
public class GenerateSchemeManagementFileTask extends PropertyListGeneratorTask<XcodeSchemeManagementFile> {
    private List<XcodeScheme> xcodeSchemes = new ArrayList<XcodeScheme>();

    public List<XcodeScheme> getXcodeSchemes() {
        return xcodeSchemes;
    }

//    public void setXcodeScheme(XcodeScheme xcodeScheme) {
//        this.xcodeScheme = xcodeScheme;
//    }

    @Override
    protected void configure(XcodeSchemeManagementFile schemeManagementFile) {
        int orderHint = 0;
        for (XcodeScheme scheme : xcodeSchemes) {
            XcodeSchemeManagementFile.SchemeUserState state = new XcodeSchemeManagementFile.SchemeUserState();
            state.setVisible(scheme.isVisible());
            state.setOrderHint(orderHint++);

            schemeManagementFile.getSchemeUserStates().put(scheme.getName(), state);
        }
    }

    @Override
    protected XcodeSchemeManagementFile create() {
        return new XcodeSchemeManagementFile(getPropertyListTransformer());
    }
}
