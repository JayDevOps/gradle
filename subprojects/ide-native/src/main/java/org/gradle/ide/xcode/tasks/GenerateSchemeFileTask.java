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

import org.gradle.api.Action;
import org.gradle.ide.xcode.XcodeScheme;
import org.gradle.ide.xcode.internal.DefaultXcodeScheme;
import org.gradle.ide.xcode.internal.DefaultXcodeTarget;
import org.gradle.ide.xcode.tasks.internal.XcodeSchemeFile;
import org.gradle.plugins.ide.api.XmlGeneratorTask;

public class GenerateSchemeFileTask extends XmlGeneratorTask<XcodeSchemeFile> {
    public DefaultXcodeScheme scheme;

    public XcodeScheme getScheme() {
        return scheme;
    }

    public void setScheme(XcodeScheme scheme) {
        this.scheme = (DefaultXcodeScheme) scheme;
    }

    @Override
    protected void configure(XcodeSchemeFile schemeFile) {
        configureBuildAction(schemeFile.getBuildAction());
        configureTestAction(schemeFile.getTestAction());
        configureLaunchAction(schemeFile.getLaunchAction());
        configureArchiveAction(schemeFile.getArchiveAction());
        configureAnalyzeAction(schemeFile.getAnalyzeAction());
        configureProfileAction(schemeFile.getProfileAction());
    }

    private void configureBuildAction(XcodeSchemeFile.BuildAction action) {
        action.setParallelizeBuild(scheme.isParallelizeBuild());
        for (XcodeScheme.BuildEntry baseEntry : scheme.getBuildEntries()) {
            final DefaultXcodeScheme.DefaultBuildEntry entry = (DefaultXcodeScheme.DefaultBuildEntry) baseEntry;
            action.entry(new Action<XcodeSchemeFile.BuildActionEntry>() {
                @Override
                public void execute(XcodeSchemeFile.BuildActionEntry buildActionEntry) {
                    buildActionEntry.setBuildForAnalysing(entry.getBuildFor().contains(XcodeScheme.BuildEntry.BuildFor.ANALYZING));
                    buildActionEntry.setBuildForArchiving(entry.getBuildFor().contains(XcodeScheme.BuildEntry.BuildFor.ARCHIVING));
                    buildActionEntry.setBuildForProfiling(entry.getBuildFor().contains(XcodeScheme.BuildEntry.BuildFor.PROFILING));
                    buildActionEntry.setBuildForRunning(entry.getBuildFor().contains(XcodeScheme.BuildEntry.BuildFor.RUNNING));
                    buildActionEntry.setBuildForTesting(entry.getBuildFor().contains(XcodeScheme.BuildEntry.BuildFor.TESTING));
                    buildActionEntry.setBuildableReference(toBuildableReference(entry.getTarget()));
                }
            });
        }
    }

    private void configureTestAction(XcodeSchemeFile.TestAction action) {
        action.setBuildConfiguration(scheme.getBuildConfiguration());
    }

    private void configureLaunchAction(XcodeSchemeFile.LaunchAction action) {
        action.setBuildConfiguration(scheme.getBuildConfiguration());
        action.setRunnablePath(scheme.getBuildEntries().get(0).getTarget().getOutputFile().getAbsolutePath());
    }

    private void configureArchiveAction(XcodeSchemeFile.ArchiveAction action) {
        action.setBuildConfiguration(scheme.getBuildConfiguration());
    }

    private void configureProfileAction(XcodeSchemeFile.ProfileAction action) {
        action.setBuildConfiguration(scheme.getBuildConfiguration());
    }

    private void configureAnalyzeAction(XcodeSchemeFile.AnalyzeAction action) {
        action.setBuildConfiguration(scheme.getBuildConfiguration());
    }

    @Override
    protected XcodeSchemeFile create() {
        return new XcodeSchemeFile(getXmlTransformer());
    }

    private XcodeSchemeFile.BuildableReference toBuildableReference(DefaultXcodeTarget target) {
        XcodeSchemeFile.BuildableReference buildableReference = new XcodeSchemeFile.BuildableReference();
        buildableReference.setBuildableIdentifier("primary");
        buildableReference.setBlueprintIdentifier(target.getId());
        buildableReference.setBuildableName(target.getName());
        buildableReference.setBlueprintName(target.getName());
        buildableReference.setContainerRelativePath(getProject().getName() + ".xcodeproj");

        return buildableReference;
    }
}
