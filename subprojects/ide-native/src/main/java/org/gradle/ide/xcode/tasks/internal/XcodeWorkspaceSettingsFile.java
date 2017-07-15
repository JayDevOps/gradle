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

package org.gradle.ide.xcode.tasks.internal;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import org.gradle.api.internal.PropertyListTransformer;
import org.gradle.plugins.ide.internal.generator.AbstractPersistableConfigurationObject;
import org.gradle.plugins.ide.internal.generator.PropertyListPersistableConfigurationObject;

import java.io.InputStream;
import java.io.OutputStream;

public class XcodeWorkspaceSettingsFile extends PropertyListPersistableConfigurationObject<NSDictionary> {
    public static final String AUTO_CREATE_CONTEXTS_IF_NEEDED_KEY = "IDEWorkspaceSharedSettings_AutocreateContextsIfNeeded";
    private boolean autoCreateContextsIfNeeded;

    public XcodeWorkspaceSettingsFile(PropertyListTransformer<NSDictionary> transformer) {
        super(NSDictionary.class, transformer);
    }

    public boolean isAutoCreateContextsIfNeeded() {
        return autoCreateContextsIfNeeded;
    }

    public void setAutoCreateContextsIfNeeded(boolean autoCreateContextsIfNeeded) {
        this.autoCreateContextsIfNeeded = autoCreateContextsIfNeeded;
    }

    @Override
    protected NSDictionary newRootObject() {
        return new NSDictionary();
    }

    @Override
    protected void store(NSDictionary rootObject) {
        rootObject.put(AUTO_CREATE_CONTEXTS_IF_NEEDED_KEY, new NSNumber(autoCreateContextsIfNeeded));
    }

    @Override
    protected void load(NSDictionary rootObject) {
        NSObject autoCreateContextsIfNeeded = rootObject.get(AUTO_CREATE_CONTEXTS_IF_NEEDED_KEY);
        if (autoCreateContextsIfNeeded != null && isBoolean(autoCreateContextsIfNeeded)) {
            this.autoCreateContextsIfNeeded = ((NSNumber)autoCreateContextsIfNeeded).boolValue();
        }
    }

    private static boolean isBoolean(NSObject obj) {
        return obj instanceof NSNumber && ((NSNumber) obj).isBoolean();
    }

    @Override
    protected String getDefaultResourceName() {
        return "default.xcsettings";
    }
}
