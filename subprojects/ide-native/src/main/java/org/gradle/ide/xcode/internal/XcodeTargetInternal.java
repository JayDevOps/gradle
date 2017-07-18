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
import org.gradle.ide.xcode.XcodeTarget;

public interface XcodeTargetInternal extends XcodeTarget {
    // TODO - move to PBXTarget, not done yet as it's Buck code
    enum FileType {
        COMPILED_MACH_O_EXECUTABLE("compiled.mach-o.executable");

        public final String identifier;
        FileType(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String toString() {
            return identifier;
        }
    }

    PBXTarget.ProductType getProductType();
    void setProductType(PBXTarget.ProductType productType);

    String getProductName();
    void setProductName(String productName);

    FileType getOutputFileType();
    void setOutputFileType(FileType outputFileType);
}
