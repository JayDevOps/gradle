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
import com.dd.plist.NSObject;
import org.gradle.api.internal.PropertyListTransformer;
import org.gradle.plugins.ide.internal.generator.PropertyListPersistableConfigurationObject;

import java.util.HashMap;
import java.util.Map;

public class XcodeSchemeManagementFile extends PropertyListPersistableConfigurationObject<NSDictionary> {
    private final Map<String, SchemeUserState> schemeUserStates = new HashMap<String, SchemeUserState>();

    public XcodeSchemeManagementFile(PropertyListTransformer transformer) {
        super(NSDictionary.class, transformer);
    }

    public Map<String, SchemeUserState> getSchemeUserStates() {
        return schemeUserStates;
    }

    @Override
    protected NSDictionary newRootObject() {
        return new NSDictionary();
    }


    @Override
    protected void store(NSDictionary rootObject) {
        if (!schemeUserStates.isEmpty()) {
            NSDictionary value = new NSDictionary();
            for (Map.Entry<String, SchemeUserState> entry : schemeUserStates.entrySet()) {
                value.put(entry.getKey(), entry.getValue().toPlist());
            }

            rootObject.put("SchemeUserState", value);
        }
    }

    @Override
    protected void load(NSDictionary rootObject) {
        if (rootObject.containsKey("SchemeUserState")) {
            NSDictionary schemeUserStates = (NSDictionary)rootObject.get("SchemeUserState");
            for (Map.Entry<String, NSObject> entry : schemeUserStates.entrySet()) {
                this.schemeUserStates.put(entry.getKey(), SchemeUserState.fromPlist((NSDictionary)entry.getValue()));
            }
        }
    }

    @Override
    protected String getDefaultResourceName() {
        return "default.plist";
    }

    public static class SchemeUserState {
        private Boolean visible;
        private Integer orderHint;

        public Boolean getVisible() {
            return visible;
        }

        public void setVisible(Boolean visible) {
            this.visible = visible;
        }

        public Integer getOrderHint() {
            return orderHint;
        }

        public void setOrderHint(Integer orderHint) {
            this.orderHint = orderHint;
        }

        public NSDictionary toPlist() {
            NSDictionary result = new NSDictionary();
            if (visible != null) {
                result.put("isShown", visible);
            }

            if (orderHint != null) {
                result.put("orderHint", orderHint);
            }

            return result;
        }

        public static SchemeUserState fromPlist(NSDictionary dict) {
            SchemeUserState result = new SchemeUserState();
            if (dict.containsKey("isShown")) {
                result.visible = dict.get("isShown").toJavaObject(Boolean.class);
            }

            if (dict.containsKey("orderHint")) {
                result.orderHint = dict.get("orderHint").toJavaObject(Integer.class);
            }

            return result;
        }
    }
}
