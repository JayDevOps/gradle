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

import com.facebook.buck.apple.xcode.xcodeproj.PBXTarget;
import org.gradle.ide.xcode.XcodeIndexingTarget;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by daniel on 2017-07-18.
 */
public class DefaultXcodeIndexingTarget extends DefaultXcodeTarget implements XcodeIndexingTarget {
    private final Set<File> sources = new HashSet<File>();

    public DefaultXcodeIndexingTarget(String name) {
        super(name);
    }

    @Override
    public Set<File> getSources() {
        return sources;
    }

    @Override
    public void setSources(Set<File> sources) {
        this.sources.addAll(sources);
    }
}
