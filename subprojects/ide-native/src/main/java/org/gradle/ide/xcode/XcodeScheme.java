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

package org.gradle.ide.xcode;

import org.gradle.api.Incubating;
import org.gradle.api.Named;

import java.util.EnumSet;
import java.util.List;

/**
 * @since 4.2
 */
@Incubating
public interface XcodeScheme extends Named {
    boolean isVisible();
    void setVisible(boolean visible);

    boolean isParallelizeBuild();
    void setParallelizeBuild(boolean parallelizeBuild);

    List<BuildEntry> getBuildEntries();

    String getBuildConfiguration();
    void setBuildConfiguration(String buildConfiguration);

    interface BuildEntry {
        enum BuildFor {
            RUNNING, TESTING, PROFILING, ARCHIVING, ANALYZING;

            public static final EnumSet<BuildFor> DEFAULT = EnumSet.allOf(BuildFor.class);
            public static final EnumSet<BuildFor> INDEXING = EnumSet.of(TESTING, ANALYZING, ARCHIVING);
            public static final EnumSet<BuildFor> TEST_ONLY = EnumSet.of(TESTING, ANALYZING);
        }

        XcodeTarget getTarget();

        EnumSet<BuildFor> getBuildFor();
    }
}
