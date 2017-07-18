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

import com.facebook.buck.apple.xcode.GidGenerator;
import com.facebook.buck.apple.xcode.xcodeproj.PBXTarget;
import org.gradle.ide.xcode.XcodeTarget;
import org.gradle.internal.id.UUIDGenerator;

import java.io.File;

public abstract class DefaultXcodeTarget implements XcodeTargetInternal {
    private final String name;
    private final String id = new UUIDGenerator().generateId().toString();
    private File outputFile;
    private PBXTarget.ProductType productType;
    private String productName;
    private FileType outputFileType;

    public DefaultXcodeTarget(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public FileType getOutputFileType() {
        return outputFileType;
    }

    @Override
    public void setOutputFileType(FileType outputFileType) {
        this.outputFileType = outputFileType;
    }

    @Override
    public PBXTarget.ProductType getProductType() {
        return productType;
    }

    @Override
    public void setProductType(PBXTarget.ProductType productType) {
        this.productType = productType;
    }

    @Override
    public String getProductName() {
        return productName;
    }

    @Override
    public void setProductName(String productName) {
        this.productName = productName;
    }
}
