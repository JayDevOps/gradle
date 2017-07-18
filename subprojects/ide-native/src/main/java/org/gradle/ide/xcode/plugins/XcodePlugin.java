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

import com.facebook.buck.apple.xcode.xcodeproj.PBXTarget;
import com.google.common.collect.Sets;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileVar;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.tasks.Delete;
import org.gradle.ide.xcode.XcodeExtension;
import org.gradle.ide.xcode.XcodeIndexingTarget;
import org.gradle.ide.xcode.XcodeScheme;
import org.gradle.ide.xcode.XcodeTarget;
import org.gradle.ide.xcode.internal.DefaultXcodeExtension;
import org.gradle.ide.xcode.internal.DefaultXcodeGradleTarget;
import org.gradle.ide.xcode.internal.DefaultXcodeIndexingTarget;
import org.gradle.ide.xcode.internal.DefaultXcodeScheme;
import org.gradle.ide.xcode.internal.DefaultXcodeTarget;
import org.gradle.ide.xcode.internal.XcodeTargetInternal;
import org.gradle.ide.xcode.tasks.GenerateSchemeFileTask;
import org.gradle.ide.xcode.tasks.GenerateWorkspaceSettingsFileTask;
import org.gradle.ide.xcode.tasks.GenerateXcodeProjectFileTask;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.language.swift.plugins.SwiftExecutablePlugin;
import org.gradle.plugins.ide.internal.IdePlugin;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;

public class XcodePlugin extends IdePlugin {
    private final Instantiator instantiator;
    private final FileResolver fileResolver;
    private DefaultXcodeExtension xcode;

    @Inject
    public XcodePlugin(Instantiator instantiator, FileResolver fileResolver) {
        this.instantiator = instantiator;
        this.fileResolver = fileResolver;
    }

    @Override
    protected String getLifecycleTaskName() {
        return "xcode";
    }

    @Override
    protected void onApply(final Project project) {
        getLifecycleTask().setDescription("Generates XCode project files (pbxproj, xcworkspace, xcscheme)");
        getCleanTask().setDescription("Cleans XCode project files (xcodeproj)");

        xcode = project.getExtensions().create("xcode", DefaultXcodeExtension.class, instantiator, fileResolver);
        xcode.getProject().setLocation(project.file(project.getName() + ".xcodeproj"));

//        configureIdeaWorkspace(project);
        configureXcodeProject(project);
//        configureIdeaModule(project);
        configureForSwiftPlugin(project);
//        configureForWarPlugin(project);
//        configureForScalaPlugin();
//        registerImlArtifact(project);
//        linkCompositeBuildDependencies((ProjectInternal) project);

        configureXcodeCleanTask(project);
    }

    private void configureXcodeCleanTask(Project project) {
        Delete cleanTask = project.getTasks().create("cleanXcodeProject", Delete.class);
        // TODO - Use provider API instead of convention mapping
        cleanTask.setDelete(project.provider(new Callable<Set<Object>>() {
            @Override
            public Set<Object> call() throws Exception {
                return Sets.newHashSet((Object) xcode.getProject().getLocation());
            }
        }));
        getCleanTask().dependsOn(cleanTask);
    }

    private void configureXcodeProject(final Project project) {
        GenerateXcodeProjectFileTask projectFileTask = project.getTasks().create("xcodeProject", GenerateXcodeProjectFileTask.class);
        projectFileTask.setProject(xcode.getProject());
        projectFileTask.setOutputFile(project.provider(new Callable<RegularFile>() {
            @Override
            public RegularFile call() throws Exception {
                RegularFileVar result = project.getLayout().newFileVar();
                result.set(new File(xcode.getProject().getLocation(), "project.pbxproj"));
                return result.get();
            }
        }));
        getLifecycleTask().dependsOn(projectFileTask);

        GenerateWorkspaceSettingsFileTask workspaceSettingsFileTask = project.getTasks().create("xcodeWorkspaceSettings", GenerateWorkspaceSettingsFileTask.class);
        workspaceSettingsFileTask.setAutoCreateContextsIfNeeded(false);
        workspaceSettingsFileTask.setOutputFile(project.provider(new Callable<RegularFile>() {
            @Override
            public RegularFile call() throws Exception {
                RegularFileVar result = project.getLayout().newFileVar();
                result.set(new File(xcode.getProject().getLocation(), "project.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings"));
                return result.get();
            }
        }));
        getLifecycleTask().dependsOn(workspaceSettingsFileTask);

        xcode.getProject().getSchemes().all(new Action<XcodeScheme>() {
            @Override
            public void execute(final XcodeScheme scheme) {
                // TODO - Ensure scheme.getName() give something sensible
                GenerateSchemeFileTask schemeFileTask = project.getTasks().create("xcodeScheme" + scheme.getName(), GenerateSchemeFileTask.class);
                schemeFileTask.setScheme(scheme);
                schemeFileTask.setOutputFile(project.provider(new Callable<RegularFile>() {
                    @Override
                    public RegularFile call() throws Exception {
                        RegularFileVar result = project.getLayout().newFileVar();
                        result.set(new File(xcode.getProject().getLocation(), "xcshareddata/xcschemes/" + scheme.getName() + ".xcscheme"));
                        return result.get();
                    }
                }));
                getLifecycleTask().dependsOn(schemeFileTask);
            }
        });
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
        if (project.getBuildFile().exists()) {
            xcode.getProject().source(project.getBuildFile());
        }

        ConfigurableFileTree sourceTree = project.fileTree("src/main/swift");
        sourceTree.include("**/*.swift");
        xcode.getProject().source(sourceTree);

        xcode.getProject().getTargets().add(newIndexingTarget("[INDEXING ONLY] " + project.getPath() + " Executable", sourceTree.getFiles()));

        DefaultXcodeTarget target = newGradleTarget(project.getPath() + " Executable", toGradleCommand(project.getRootProject()), project.getPath() + "linkMain", project.file("build/exe/app"));
        xcode.getProject().getTargets().add(target);
        xcode.getProject().getSchemes().add(newScheme(target));
    }

    private static String toGradleCommand(Project project) {
        if (project.file("gradlew").exists()) {
            return project.file("gradlew").getAbsolutePath();
        } else {
            // TODO - default to gradle on the path (or should we generate an error if no gradle is in the path?)
            return "/Users/daniel/gradle/gradle-source-build/bin/gradle";
        }
    }

    private static DefaultXcodeTarget newGradleTarget(String name, String gradleCommand, String taskName, File outputFile) {
        DefaultXcodeGradleTarget target = new DefaultXcodeGradleTarget(name);
        target.setOutputFile(outputFile);
        target.setTaskName(taskName);
        target.setGradleCommand(gradleCommand);
        target.setOutputFileType(XcodeTargetInternal.FileType.COMPILED_MACH_O_EXECUTABLE);
        target.setProductType(PBXTarget.ProductType.TOOL);
        target.setProductName(outputFile.getName());

        return target;
    }

    private static XcodeTarget newIndexingTarget(String name, Set<File> sources) {
        DefaultXcodeIndexingTarget target = new DefaultXcodeIndexingTarget(name);
        target.setSources(sources);
        target.setOutputFileType(XcodeTargetInternal.FileType.COMPILED_MACH_O_EXECUTABLE);
        target.setProductType(PBXTarget.ProductType.TOOL);
        target.setProductName(name);
        return target;
    }

    private static XcodeScheme newScheme(DefaultXcodeTarget target) {
        XcodeScheme.BuildEntry entry = new DefaultXcodeScheme.DefaultBuildEntry(target, XcodeScheme.BuildEntry.BuildFor.DEFAULT);

        XcodeScheme scheme = new DefaultXcodeScheme(target.getName());
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
