/**
 * Copyright 2015 Tobias Gierke <tobias.gierke@code-sourcery.de>
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
package de.codesourcery.jsonparser.util;

import de.codesourcery.jsonparser.ast.ASTNode;
import de.codesourcery.jsonparser.ast.BooleanLiteral;
import de.codesourcery.jsonparser.ast.JSONArray;
import de.codesourcery.jsonparser.ast.JSONObject;
import de.codesourcery.jsonparser.ast.KeyValue;
import de.codesourcery.jsonparser.ast.NullLiteral;
import de.codesourcery.jsonparser.ast.NumberLiteral;
import de.codesourcery.jsonparser.ast.PlaceholderExpression;
import de.codesourcery.jsonparser.ast.StringLiteral;

public class ASTVisitor
{
    public final void visit(ASTNode node) 
    {
        if ( node instanceof BooleanLiteral) {
            visit( (BooleanLiteral) node);
        } else if ( node instanceof JSONArray) {
            visit( (JSONArray) node);
        } else if ( node instanceof JSONObject) {
            visit( (JSONObject) node);
        } else if ( node instanceof KeyValue) {
            visit( (KeyValue) node);
        } else if ( node instanceof NullLiteral) {
            visit( (NullLiteral) node);
        } else if ( node instanceof NumberLiteral) {
            visit( (NumberLiteral) node);
        } else if ( node instanceof PlaceholderExpression) {
            visit( (PlaceholderExpression) node);
        } else if ( node instanceof StringLiteral) {
            visit( (StringLiteral) node);
        } else {
            throw new RuntimeException("Internal error,unhandled AST node "+node);
        }
    }
    
    protected final void visitChildren(ASTNode node) 
    {
        for ( int i = 0 , len = node.childCount() ; i < len ; i++ ) {
            visit( node.child(i) );
        }
    }
    
    protected void visit(BooleanLiteral node) {
        
    }
    
    protected void visit(JSONArray node) { 
        visitChildren(node);
    }
    
    protected void visit(JSONObject node) { 
        visitChildren(node);
    }
    
    protected void visit(KeyValue node) { 
        visitChildren(node);
    }
    
    protected void visit(NullLiteral node) { }
    
    protected void visit(NumberLiteral node) { }
    
    protected void visit(PlaceholderExpression node) { }
    
    protected void visit(StringLiteral node) { }
}