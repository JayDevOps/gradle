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

package org.gradle.ide.xcode.plugins;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.Delete;
import org.gradle.ide.xcode.XcodeProject;
import org.gradle.ide.xcode.XcodeScheme;
import org.gradle.ide.xcode.internal.DefaultXcodeProject;
import org.gradle.ide.xcode.internal.DefaultXcodeScheme;
import org.gradle.ide.xcode.internal.DefaultXcodeTarget;
import org.gradle.ide.xcode.tasks.GenerateSchemeFileTask;
import org.gradle.ide.xcode.tasks.GenerateWorkspaceSettingsFileTask;
import org.gradle.ide.xcode.tasks.GenerateXcodeProjectFileTask;
import org.gradle.language.swift.plugins.SwiftExecutablePlugin;
import org.gradle.plugins.ide.internal.IdePlugin;

public class XcodePlugin extends IdePlugin {
    @Override
    protected String getLifecycleTaskName() {
        return "xcode";
    }

    @Override
    protected void onApply(final Project project) {
        getLifecycleTask().setDescription("Generates XCode project files (pbxproj, xcworkspace, xcscheme)");
        getCleanTask().setDescription("Cleans XCode project files (xcodeproj)");

        configureXcodeCleanTask(project);

//        ideaModel = project.getExtensions().create("idea", IdeaModel.class);

//        configureIdeaWorkspace(project);
//        configureIdeaProject(project);
//        configureIdeaModule(project);
        configureForSwiftPlugin(project);
//        configureForWarPlugin(project);
//        configureForScalaPlugin();
//        registerImlArtifact(project);
//        linkCompositeBuildDependencies((ProjectInternal) project);
    }

    private void configureXcodeCleanTask(Project project) {
        if (isRoot(project)) {
            Delete cleanTask = project.getTasks().create("cleanXcodeProject", Delete.class);
            cleanTask.delete(project.file("app.xcodeproj"));
            getCleanTask().dependsOn(cleanTask);
        }
    }

    private void configureForSwiftPlugin(final Project project) {
        project.getPlugins().withType(SwiftExecutablePlugin.class, new Action<SwiftExecutablePlugin>() {
            @Override
            public void execute(SwiftExecutablePlugin swiftExecutablePlugin) {
                configureXcodeForSwift(project);
            }
        });
    }

    private void configureXcodeForSwift(Project project) {
        if (isRoot(project)) {
            XcodeProject proj = newProject(project);

            GenerateXcodeProjectFileTask projectFileTask = project.getTasks().create("xcodeProject", GenerateXcodeProjectFileTask.class);
            projectFileTask.setProject(proj);
            projectFileTask.setOutputFile(project.file("app.xcodeproj/project.pbxproj"));
            getLifecycleTask().dependsOn(projectFileTask);

            GenerateWorkspaceSettingsFileTask workspaceSettingsFileTask = project.getTasks().create("xcodeWorkspaceSettings", GenerateWorkspaceSettingsFileTask.class);
            workspaceSettingsFileTask.setAutoCreateContextsIfNeeded(false);
            workspaceSettingsFileTask.setOutputFile(project.file("app.xcodeproj/project.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings"));
            getLifecycleTask().dependsOn(workspaceSettingsFileTask);

            GenerateSchemeFileTask schemeFileTask = project.getTasks().create("xcodeScheme", GenerateSchemeFileTask.class);
            schemeFileTask.setScheme(proj.getSchemes().get(0));
            schemeFileTask.setOutputFile(project.file("app.xcodeproj/xcshareddata/xcschemes/target11.xcscheme"));
            getLifecycleTask().dependsOn(schemeFileTask);
        }
    }

    private static XcodeProject newProject(Project project) {
        XcodeProject proj = new DefaultXcodeProject();

        DefaultXcodeTarget target = newTarget(project);

        proj.getTargets().add(target);
        proj.getSchemes().add(newScheme(target));

        return proj;
    }

    private static DefaultXcodeTarget newTarget(Project project) {
        DefaultXcodeTarget target = new DefaultXcodeTarget("target11");
        target.setOutputFile(project.file("build/exe/app"));

        return target;
    }

    private static XcodeScheme newScheme(DefaultXcodeTarget target) {
        XcodeScheme.BuildEntry entry = new DefaultXcodeScheme.DefaultBuildEntry(target, XcodeScheme.BuildEntry.BuildFor.DEFAULT);

        XcodeScheme scheme = new DefaultXcodeScheme("target11");
        scheme.setVisible(true);
        scheme.setParallelizeBuild(true);
        scheme.getBuildEntries().add(entry);
        scheme.setBuildConfiguration("Debug");

        return scheme;
    }

    private static boolean isRoot(Project project) {
        return project.getParent() == null;
    }
}
