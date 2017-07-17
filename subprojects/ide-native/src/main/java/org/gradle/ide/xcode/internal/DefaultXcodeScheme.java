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

package org.gradle.ide.xcode.internal;

import org.gradle.ide.xcode.XcodeScheme;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class DefaultXcodeScheme implements XcodeScheme {
    private final String name;
    private boolean visible;
    private boolean parallelizeBuild;
    private List<BuildEntry> buildEntries = new ArrayList<BuildEntry>();
    private String buildConfiguration;

    public DefaultXcodeScheme(String name) {
        this.name = name;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isParallelizeBuild() {
        return parallelizeBuild;
    }

    @Override
    public void setParallelizeBuild(boolean parallelizeBuild) {
        this.parallelizeBuild = parallelizeBuild;
    }

    @Override
    public List<BuildEntry> getBuildEntries() {
        return buildEntries;
    }

    @Override
    public String getBuildConfiguration() {
        return buildConfiguration;
    }

    @Override
    public void setBuildConfiguration(String buildConfiguration) {
        this.buildConfiguration = buildConfiguration;
    }

    @Override
    public String getName() {
        return name;
    }

    public static class DefaultBuildEntry implements BuildEntry {
        private final DefaultXcodeTarget target;
        private final EnumSet<BuildFor> buildFor;

        public DefaultBuildEntry(DefaultXcodeTarget target, EnumSet<BuildFor> buildFor) {
            this.target = target;
            this.buildFor = buildFor;
        }

        @Override
        public DefaultXcodeTarget getTarget() {
            return target;
        }

        @Override
        public EnumSet<BuildFor> getBuildFor() {
            return buildFor;
        }
    }
}
