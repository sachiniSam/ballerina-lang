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
import io.ballerinalang.compiler.syntax.tree.*;

import java.util.ArrayList;
import java.util.List;

import io.ballerinalang.compiler.syntax.tree.TreeModifier;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;

/**
 * Modifies the given tree to format the nodes.
 * Modifier overrides the ModulePartNode, IfElseStatementNode and BlockStatementNode
 *
 */
public class ObservabilityTreeModifier extends TreeModifier {
    private final PackageID packageID;
    private final CompilerInput sourceEntry;
    // variable which keeps track whether the observability module import was checked,
    //once the if-else statements are reached
    private boolean importChecked = false;
    // variable keep tracks if the observability module was imported or not in the user code
    private boolean isObserveImported = false;
    private ModulePartNode modulePartNode;
    private NodeList<ImportDeclarationNode> newImportNodeList;

    ObservabilityTreeModifier(PackageID packageID, CompilerInput sourceEntry, ModulePartNode modulePartNode){
        this.packageID = packageID;
        this.sourceEntry = sourceEntry;
        this.modulePartNode = modulePartNode;
    }

    @Override
    public ModulePartNode transform(ModulePartNode modulePartNode) {

        //NodeList<ImportDeclarationNode> imports = modifyNodeList(modulePartNode.imports());
        newImportNodeList = modifyNodeList(modulePartNode.imports());
        NodeList<ModuleMemberDeclarationNode> members = modifyNodeList(modulePartNode.members());

        return modulePartNode.modify().withImports(newImportNodeList).withMembers(members).apply();
    }


    @Override
    public IfElseStatementNode transform(IfElseStatementNode ifElseStatementNode) {

        //check if the observability import is preset in the user code
        if (!this.importChecked){
            this.importChecked = true;
            List<Node> importNodes = new ArrayList();

            outerLoop:
            for (int i = 0; i < modulePartNode.imports().size();i++) {

                ImportDeclarationNode newNode = (modulePartNode.imports().get(i));
                for (int x = 0; x < newNode.moduleName().size(); x++) {
                    if (newNode.moduleName().get(x).text().equalsIgnoreCase("observe")) {
                        this.isObserveImported = true;
                        break outerLoop;
                    }
                }
                importNodes.add(i, (modulePartNode.imports().get(i)));//current statements
            }

            //If the observability module import is not present in the user code, then Add the import
            if (!isObserveImported){

                //create import token with trailing space
                MinutiaeList leadingMinutiae = NodeFactory.createEmptyMinutiaeList();
                Minutiae spaceAppendedMinutiae = NodeFactory.createWhitespaceMinutiae(" ");
                MinutiaeList trailingMinutiae = NodeFactory.createMinutiaeList(spaceAppendedMinutiae);
                Token importToken = NodeFactory.createToken(SyntaxKind.IMPORT_KEYWORD, leadingMinutiae, trailingMinutiae);

                //ballerina identifier token
                IdentifierToken ballIdentifier = NodeFactory.createIdentifierToken("ballerina");

                //slash token
                Token slashToken = NodeFactory.createToken(SyntaxKind.SLASH_TOKEN);

                // Module organization name token
                ImportOrgNameNode orgNameNode = NodeFactory.createImportOrgNameNode(ballIdentifier, slashToken);

                IdentifierToken observeToken = NodeFactory.createIdentifierToken("observe");
                List<Node> moduleArgs = new ArrayList();
                moduleArgs.add(observeToken);
                SeparatedNodeList importList = NodeFactory.createSeparatedNodeList(moduleArgs);

                Minutiae trailingNewLine = NodeFactory.createEndOfLineMinutiae("\n");
                MinutiaeList semicolonMinutiae = NodeFactory.createMinutiaeList(trailingNewLine);

                Token semiColon = NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN, leadingMinutiae, semicolonMinutiae);

                ImportDeclarationNode observeImport = NodeFactory.createImportDeclarationNode(importToken, orgNameNode, importList, null, null, semiColon);
                importNodes.add(observeImport);
                SeparatedNodeList newImports = NodeFactory.createSeparatedNodeList(importNodes);
                this.importChecked = true;
                //NodeList<ModuleMemberDeclarationNode> members = modifyNodeList(modulePartNode.members());
                newImportNodeList = newImports;


            }
        }


        Token modulePrefix = NodeFactory.createIdentifierToken("observe");

        Token colonToken = NodeFactory.createToken(SyntaxKind.COLON_TOKEN);//:

        IdentifierToken funcToken = NodeFactory.createIdentifierToken("startObservation");// token startObservation

        QualifiedNameReferenceNode qualifiedNameReferenceNode = NodeFactory.createQualifiedNameReferenceNode(modulePrefix,colonToken,funcToken);

        Token openParen = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);// (


        String positionId = generatePositionId(ifElseStatementNode.ifBody().openBraceToken().location(),sourceEntry);
        String packageInfo = generatePackageId(packageID);

//        String startBraceLinePos = String.valueOf(ifElseStatementNode.ifBody().openBraceToken().location().lineRange().startLine().line());
//        String startBraceColPos = String.valueOf(ifElseStatementNode.ifBody().openBraceToken().location().lineRange().startLine().offset());

        MinutiaeList leadingMinutiae = NodeFactory.createEmptyMinutiaeList();
        MinutiaeList trailingMinutiae = NodeFactory.createEmptyMinutiaeList();

        LiteralValueToken packageLiteral = NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,packageInfo,leadingMinutiae,trailingMinutiae);
        BasicLiteralNode packageBase = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,packageLiteral);
        PositionalArgumentNode packageArg = NodeFactory.createPositionalArgumentNode(packageBase);


        List<Node> funcArgs = new ArrayList();
        funcArgs.add(packageArg);

        Token commaToken = NodeFactory.createToken(SyntaxKind.COMMA_TOKEN);
        funcArgs.add(commaToken);

        LiteralValueToken positionLiteral = NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,positionId,leadingMinutiae,trailingMinutiae);
        BasicLiteralNode positionBase = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,positionLiteral);
        PositionalArgumentNode positionArg = NodeFactory.createPositionalArgumentNode(positionBase);
        funcArgs.add(positionArg);


        SeparatedNodeList argsList = NodeFactory.createSeparatedNodeList(funcArgs);

        Token closeParen = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

        FunctionCallExpressionNode observeFuncCall = NodeFactory.createFunctionCallExpressionNode(qualifiedNameReferenceNode,openParen,argsList,closeParen);



        Token semicolon = NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        ExpressionStatementNode expressionStatementNode = NodeFactory.createExpressionStatementNode(SyntaxKind.CALL_STATEMENT,observeFuncCall,semicolon);


        //observe:endObservation();
        IdentifierToken endFuncToken = NodeFactory.createIdentifierToken("endObservation");// token endObservation

        QualifiedNameReferenceNode qualifiedNameRefEndFunc = NodeFactory.createQualifiedNameReferenceNode(modulePrefix,colonToken,endFuncToken);

        List<Node> funcArgs2 = new ArrayList();
        SeparatedNodeList argsList2 = NodeFactory.createSeparatedNodeList(funcArgs2);

        FunctionCallExpressionNode endObserveFuncCall = NodeFactory.createFunctionCallExpressionNode(qualifiedNameRefEndFunc,openParen,argsList2,closeParen);

        ExpressionStatementNode expressionStatementNodeEnd = NodeFactory.createExpressionStatementNode(SyntaxKind.CALL_STATEMENT,endObserveFuncCall,semicolon);

        boolean endObservationInjected = false;
        List<StatementNode> newList2 = new ArrayList();
        newList2.add(expressionStatementNode);

        for (int i = 0; i < ifElseStatementNode.ifBody().statements().size(); i++) {
            if(ifElseStatementNode.ifBody().statements().get(i).kind()==SyntaxKind.PANIC_STATEMENT ||
                    ifElseStatementNode.ifBody().statements().get(i).kind()==SyntaxKind.RETURN_STATEMENT){
                newList2.add(i+1,expressionStatementNodeEnd);
                endObservationInjected = true;
            }
            if(endObservationInjected){
                newList2.add(i + 2, ifElseStatementNode.ifBody().statements().get(i));//current statements
            }else{
                //nested if
                if (ifElseStatementNode.ifBody().statements().get(i).kind()==SyntaxKind.IF_ELSE_STATEMENT){
                    IfElseStatementNode transformedNode = transform((IfElseStatementNode) ifElseStatementNode.ifBody().statements().get(i));
                    newList2.add(transformedNode);
                }else{
                    newList2.add(i + 1, ifElseStatementNode.ifBody().statements().get(i));//current statements
                }
            }
        }

        if (!endObservationInjected){
            newList2.add(expressionStatementNodeEnd);//appending with endObservation();
        }

        NodeList<StatementNode> newList3 = NodeFactory.createNodeList(newList2);

        BlockStatementNode newBody = ifElseStatementNode.ifBody().modify().withStatements(newList3).apply();

        Node elseBody = this.modifyNode(ifElseStatementNode.elseBody().orElse(null));
        if (elseBody != null) {
            ifElseStatementNode = ifElseStatementNode.modify()
                    .withElseBody(elseBody).apply();
        }

        return ifElseStatementNode.modify().
                withIfBody(newBody).apply();
    }




    @Override
    public BlockStatementNode transform(BlockStatementNode blockStatementNode) {


        SyntaxKind parentKind = blockStatementNode.parent().kind();
        if (blockStatementNode.parent() != null && parentKind == (SyntaxKind.ELSE_BLOCK)) {


            Token modulePrefix = NodeFactory.createIdentifierToken("observe");

            Token colonToken = NodeFactory.createToken(SyntaxKind.COLON_TOKEN);//:

            IdentifierToken funcToken = NodeFactory.createIdentifierToken("startObservation");// token startObservation

            QualifiedNameReferenceNode qualifiedNameReferenceNode = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colonToken, funcToken);

            Token openParen = NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN);// (


            String positionId = generatePositionId(blockStatementNode.openBraceToken().location(),sourceEntry);
            String packageInfo = generatePackageId(packageID);

            MinutiaeList leadingMinutiae = NodeFactory.createEmptyMinutiaeList();
            MinutiaeList trailingMinutiae = NodeFactory.createEmptyMinutiaeList();

            LiteralValueToken packageLiteral = NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,packageInfo,leadingMinutiae,trailingMinutiae);
            BasicLiteralNode packageBase = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,packageLiteral);
            PositionalArgumentNode packageArg = NodeFactory.createPositionalArgumentNode(packageBase);


            List<Node> funcArgs = new ArrayList();
            funcArgs.add(packageArg);

            Token commaToken = NodeFactory.createToken(SyntaxKind.COMMA_TOKEN);//,
            funcArgs.add(commaToken);

            LiteralValueToken positionLiteral = NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,positionId,leadingMinutiae,trailingMinutiae);
            BasicLiteralNode positionBase = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,positionLiteral);
            PositionalArgumentNode positionArg = NodeFactory.createPositionalArgumentNode(positionBase);
            funcArgs.add(positionArg);


            SeparatedNodeList argsList = NodeFactory.createSeparatedNodeList(funcArgs);

            Token closeParen = NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);

            FunctionCallExpressionNode observeFuncCall = NodeFactory.createFunctionCallExpressionNode(qualifiedNameReferenceNode, openParen, argsList, closeParen);

            Token semicolon = NodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

            ExpressionStatementNode expressionStatementNode = NodeFactory.createExpressionStatementNode(SyntaxKind.CALL_STATEMENT, observeFuncCall, semicolon);

            //observe:endObservation();
            IdentifierToken endFuncToken = NodeFactory.createIdentifierToken("endObservation");// token endObservation
            QualifiedNameReferenceNode qualifiedNameRefEndFunc = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colonToken, endFuncToken);
            List<Node> funcArgs2 = new ArrayList();
            SeparatedNodeList argsList2 = NodeFactory.createSeparatedNodeList(funcArgs2);
            FunctionCallExpressionNode endObserveFuncCall = NodeFactory.createFunctionCallExpressionNode(qualifiedNameRefEndFunc, openParen, argsList2, closeParen);
            ExpressionStatementNode expressionStatementNodeEnd = NodeFactory.createExpressionStatementNode(SyntaxKind.CALL_STATEMENT, endObserveFuncCall, semicolon);

            boolean endObservationInjected = false;
            //adding to the list
            List<StatementNode> newList2 = new ArrayList();
            newList2.add(expressionStatementNode);
            for (int i = 0; i < blockStatementNode.statements().size(); i++) {
                if(blockStatementNode.statements().get(i).kind()==SyntaxKind.PANIC_STATEMENT ||
                        blockStatementNode.statements().get(i).kind()==SyntaxKind.RETURN_STATEMENT){
                    newList2.add(i+1,expressionStatementNodeEnd);
                    endObservationInjected = true;
                }
                if(endObservationInjected){
                    newList2.add(i + 2, blockStatementNode.statements().get(i));//current statements
                }else{
                    if (blockStatementNode.statements().get(i).kind()==SyntaxKind.IF_ELSE_STATEMENT){
                        IfElseStatementNode transformedNode = transform((IfElseStatementNode) blockStatementNode.statements().get(i));
                        newList2.add(transformedNode);
                    }else{
                        newList2.add(i + 1, blockStatementNode.statements().get(i));//current statements
                    }

                }
            }


            if (!endObservationInjected){
                newList2.add(expressionStatementNodeEnd);//appending with endObservation();
            }

            NodeList<StatementNode> newList3 = NodeFactory.createNodeList(newList2);
            return blockStatementNode.modify().withStatements(newList3).apply();
        }
        return blockStatementNode;
    }


    /**
     * Generate a ID for a source code position.
     *
     * @param location The position for which the ID should be generated
     * @param sourceEntry source file name details
     * @return The generated ID
     */
    private String generatePositionId(NodeLocation location, CompilerInput sourceEntry) {
        return String.format("\"%s:%d:%d\"",sourceEntry.getEntryName(),location.lineRange().startLine().line(),location.lineRange().startLine().offset());
    }

    /**
     * Generate a ID for package details
     *
     * @param packageID package details
     * @return The generated package ID
     */
    private String generatePackageId(PackageID packageID){
        return String.format("\"%s\"",packageID.toString());
    }


}

