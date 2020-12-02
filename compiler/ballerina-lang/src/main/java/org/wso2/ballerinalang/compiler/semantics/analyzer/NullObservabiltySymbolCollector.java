/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.PackageDescriptor;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;

/**
 * Null Object for ObserverbilitySymbolCollector interface.
 *
 * @since 2.0.0
 */
public class NullObservabiltySymbolCollector implements ObservabilitySymbolCollector {
    @Override
    public void init(CompilerContext context) {
        // Do nothing
    }

    @Override
    public void process(PackageDescriptor packageDescriptor, ModuleDescriptor moduleDescriptor,
                        SemanticModel semanticModel, String documentName, SyntaxTree syntaxTree,
                        BLangPackage bLangPackage) {
        // Do nothing
    }

    @Override
    public void writeCollectedSymbols(Path executableFile) {
        // Do nothing
    }
}
