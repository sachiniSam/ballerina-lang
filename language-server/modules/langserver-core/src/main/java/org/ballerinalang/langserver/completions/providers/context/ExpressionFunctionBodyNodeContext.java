/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.List;

/**
 * Completion Provider for {@link ExpressionFunctionBodyNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ExpressionFunctionBodyNodeContext extends AbstractCompletionProvider<ExpressionFunctionBodyNode> {
    public ExpressionFunctionBodyNodeContext() {
        super(ExpressionFunctionBodyNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(CompletionContext ctx, ExpressionFunctionBodyNode node)
            throws LSCompletionException {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (this.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }
        return this.expressionCompletions(ctx);
    }

    @Override
    public boolean onPreValidation(CompletionContext context, ExpressionFunctionBodyNode node) {
        if (node.rightDoubleArrow() == null || node.rightDoubleArrow().isMissing()) {
            return false;
        }
        int cursor = context.getCursorPositionInTree();
        int rightArrowEnd = node.rightDoubleArrow().textRange().endOffset();

        return cursor >= rightArrowEnd;
    }
}
