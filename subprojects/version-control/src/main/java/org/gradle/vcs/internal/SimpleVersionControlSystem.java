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

package org.gradle.vcs.internal;

import org.gradle.api.UncheckedIOException;
import org.gradle.util.GFileUtils;
import org.gradle.vcs.VersionControlSpec;
import org.gradle.vcs.VersionControlSystem;

import java.io.File;
import java.io.IOException;

public class SimpleVersionControlSystem implements VersionControlSystem {
    @Override
    public void populate(File workingDir, VersionControlSpec spec) {
        File sourceDir = ((DirectoryRepository)spec).getSourceDir();
        try {
            GFileUtils.copyDirectory(sourceDir, workingDir);
            new File(workingDir, "checkedout").createNewFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
