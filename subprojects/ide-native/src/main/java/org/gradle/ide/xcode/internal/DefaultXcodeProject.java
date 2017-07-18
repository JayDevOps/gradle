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

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.ide.xcode.XcodeProject;
import org.gradle.ide.xcode.XcodeScheme;
import org.gradle.ide.xcode.XcodeTarget;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultXcodeProject implements XcodeProject {
    private final List<XcodeTarget> targets = new ArrayList<XcodeTarget>();
    private final ExtensiblePolymorphicDomainObjectContainer<XcodeScheme> schemes;
    private final FileResolver fileResolver;
    private final Set<File> sources = new HashSet<File>();
    private File location;

    @Inject
    public DefaultXcodeProject(final Instantiator instantiator, FileResolver fileResolver) {
        this.schemes = instantiator.newInstance(DefaultPolymorphicDomainObjectContainer.class, XcodeScheme.class, instantiator);
        this.fileResolver = fileResolver;

        schemes.registerFactory(XcodeScheme.class, new NamedDomainObjectFactory<XcodeScheme>() {
            public XcodeScheme create(String name) {
                return instantiator.newInstance(DefaultXcodeScheme.class, name);
            }
        });
    }

    public void source(Object... paths) {
        sources.addAll(fileResolver.resolveFiles(paths).getFiles());
    }

    public Set<File> getSources() {
        return sources;
    }

    @Override
    public File getLocation() {
        return location;
    }

    @Override
    public void setLocation(File location) {
        this.location = location;
    }

    @Override
    public List<XcodeTarget> getTargets() {
        return targets;
    }

    @Override
    public NamedDomainObjectContainer<XcodeScheme> getSchemes() {
        return schemes;
    }
}
