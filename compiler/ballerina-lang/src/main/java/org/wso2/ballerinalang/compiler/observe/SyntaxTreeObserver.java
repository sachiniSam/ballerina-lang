/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.observe;
import io.ballerina.tools.text.LineRange;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that exposes the syntax tree observability API.
 * This would expose the modified syntax tree after instrumenting with observability instructs.
 */
public class SyntaxTreeObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyntaxTreeObserver.class);

    /**
     * Formats the syntax tree by injecting the observability function calls
     *
     * @param syntaxTree The complete SyntaxTree, of which a part is to be modified
     * @param packageID The packageID of the Syntax tree
     * @param sourceEntry source Entry details
     * @return The modified SyntaxTree after injecting observability function calls
     */
    public static SyntaxTree format(SyntaxTree syntaxTree, PackageID packageID, CompilerInput sourceEntry) {
        return modifyTree(syntaxTree,packageID,sourceEntry);

    }

    /**
    * Formats the syntax tree by injecting the observability function calls
     * @param packageID The packageID of the Syntax tree
     * @param sourceEntry source Entry details
     * @return The modified SyntaxTree after injecting observability function calls
    */
    private static SyntaxTree modifyTree(SyntaxTree syntaxTree,PackageID packageID,CompilerInput sourceEntry) {

        ModulePartNode modulePartNode = syntaxTree.rootNode();
        ObservabilityTreeModifier treeModifier = new ObservabilityTreeModifier(packageID,sourceEntry,modulePartNode);

        try {
            // Instrument the root node
            SyntaxTree newSyntaxTree = syntaxTree.modifyWith(treeModifier.transform(modulePartNode));
            return newSyntaxTree;

        } catch (Exception e) {
            LOGGER.error(String.format("Error while formatting the source: %s", e.getMessage()));
            return syntaxTree;
        }
    }
}
