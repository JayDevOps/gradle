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
import org.gradle.ide.xcode.XcodeScheme;
import org.gradle.ide.xcode.internal.DefaultXcodeScheme;
import org.gradle.ide.xcode.tasks.GenerateSchemeManagementFileTask;
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
        getLifecycleTask().setDescription("Generates XCode project files (xcodeproj, xcworkspace, ???)");
        getCleanTask().setDescription("Cleans XCode project files (???, ???)");

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
            GenerateXcodeProjectFileTask task = project.getTasks().create("pbxProject", GenerateXcodeProjectFileTask.class);
            addWorker(task);

            GenerateWorkspaceSettingsFileTask workspaceSettingsFileTask = project.getTasks().create("workspaceSettings", GenerateWorkspaceSettingsFileTask.class);
            workspaceSettingsFileTask.setAutoCreateContextsIfNeeded(false);
            workspaceSettingsFileTask.setOutputFile(project.file("app.xcodeproj/project.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings"));
            addWorker(workspaceSettingsFileTask);

            GenerateSchemeManagementFileTask sharedSchemeManagementFileTask = project.getTasks().create("sharedSchemeManagement", GenerateSchemeManagementFileTask.class);
            sharedSchemeManagementFileTask.getXcodeSchemes().add(newScheme("target11.xcscheme", true));
            sharedSchemeManagementFileTask.getXcodeSchemes().add(newScheme("[indexing] DO NOT BUILD target11.xcscheme", false));
            sharedSchemeManagementFileTask.setOutputFile(project.file("app.xcodeproj/xcshareddata/xcschemes/xcschememanagement.plist"));
            addWorker(sharedSchemeManagementFileTask);

            GenerateSchemeManagementFileTask userSchemeManagementFileTask = project.getTasks().create("userSchemeManagement", GenerateSchemeManagementFileTask.class);
            userSchemeManagementFileTask.getXcodeSchemes().add(newScheme("target11.xcscheme_^#shared#^_", true));
            userSchemeManagementFileTask.getXcodeSchemes().add(newScheme("[indexing] DO NOT BUILD target11.xcscheme_^#shared#^_", false));
            userSchemeManagementFileTask.setOutputFile(project.file("app.xcodeproj/xcuserdata/" + System.getProperty("user.name") + ".xcuserdatad/xcschemes/xcschememanagement.plist"));
            addWorker(userSchemeManagementFileTask);
        }
    }

    private static XcodeScheme newScheme(String name, boolean visible) {
        XcodeScheme result = new DefaultXcodeScheme(name);
        result.setVisible(visible);
        return result;
    }

    private static boolean isRoot(Project project) {
        return project.getParent() == null;
    }
}
