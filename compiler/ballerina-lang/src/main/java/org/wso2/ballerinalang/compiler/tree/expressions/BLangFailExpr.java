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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.FailExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.List;

/**
 * {@code FailExpressionNode} represents a expression which forces to return error.
 *
 * @since Swan Lake
 */
public class BLangFailExpr extends BLangExpression implements FailExpressionNode {

    public BLangExpression expr;

    // This list caches types that are equivalent to the error type which are returned by the rhs expression.
    public List<BType> equivalentErrorTypeList;

    @Override
    public ExpressionNode getExpression() {
        return this.expr;
    }

    @Override
    public void setExpression(ExpressionNode expressionNode) {
        this.expr = (BLangExpression) expressionNode;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.FAIL;
    }
}