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

import java.util.Iterator;

import de.codesourcery.jsonparser.Identifier;
import de.codesourcery.jsonparser.ast.ASTNode;
import de.codesourcery.jsonparser.ast.BooleanLiteral;
import de.codesourcery.jsonparser.ast.JSONArray;
import de.codesourcery.jsonparser.ast.JSONObject;
import de.codesourcery.jsonparser.ast.KeyValue;
import de.codesourcery.jsonparser.ast.NullLiteral;
import de.codesourcery.jsonparser.ast.NumberLiteral;
import de.codesourcery.jsonparser.ast.PlaceholderExpression;
import de.codesourcery.jsonparser.ast.StringLiteral;

public class ASTPrinter extends ASTVisitor
{
    private StringBuilder buffer = new StringBuilder();

    private boolean prettyPrint = true;
    private int depth = 0;

    public String print(ASTNode node) 
    {
        depth = 0;
        buffer.setLength( 0 );
        super.visit( node );
        return buffer.toString();
    }

    @Override
    protected void visit(BooleanLiteral node)
    {
        buffer.append( node.toString() );
    }

    private void repeat(char c,int times) 
    {
        for ( int i = 0 ; i < times ; i++ ) {
            buffer.append( c );
        }
    }

    private void maybeAppendNewline() {
        if ( prettyPrint ) 
        {
            if ( depth > 0 ) 
            {
                repeat(' ' , depth*2 );
            }
            buffer.append("\n");
        }
    }

    private void maybeIndent() 
    {
        if ( prettyPrint ) 
        {
            if ( depth > 0 ) 
            {
                repeat(' ' , depth*2 );
            }
        }
    }
    
    private void maybeAppendWhitespace() {
        if ( prettyPrint ) {
            buffer.append(" ");
        }
    }    

    @Override
    protected void visit(JSONArray node)
    {
        buffer.append( "[" );
        depth++;
        try 
        {
            if ( node.hasChildren() ) {
                maybeAppendNewline();
            } else {
                maybeAppendWhitespace();
            }        
            for (Iterator<ASTNode> it = node.children().iterator(); it.hasNext();) 
            {
                final ASTNode child = it.next();
                maybeIndent();
                visit( child );
                if ( it.hasNext() ) {
                    buffer.append(",");
                    maybeAppendNewline();                
                }
            }
        } finally {
            depth--;
        }
        maybeAppendNewline();
        buffer.append( "]" );
    }

    @Override
    protected void visit(JSONObject node)
    {
        buffer.append( "{" );
        depth++;
        try {        
            if ( node.hasChildren() ) {
                maybeAppendNewline();
            } else {
                maybeAppendWhitespace();
            }
            for (Iterator<ASTNode> it = node.children().iterator(); it.hasNext();) 
            {
                final ASTNode child = it.next();
                maybeIndent();
                visit( child );
                if ( it.hasNext() ) {
                    buffer.append(",");
                    maybeAppendNewline();                
                }
            }
            maybeAppendNewline();
        } finally {
            depth--;
        }
        maybeIndent();
        buffer.append( "}" );             
    }

    @Override
    protected void visit(KeyValue node)
    {
        visit( node.key() );
        maybeAppendWhitespace();
        buffer.append(":");
        maybeAppendWhitespace();
        visit( node.value() );
    }

    @Override
    protected void visit(NullLiteral node)
    {
        buffer.append("null");
    }

    @Override
    protected void visit(NumberLiteral node)
    {
        buffer.append( node.value );
    }

    @Override
    protected void visit(PlaceholderExpression node)
    {
        String resolved = resolvePlaceholder( node.name );
        if ( resolved == null ) {
            buffer.append("${");
            buffer.append( node.name.name );
            buffer.append("}");
        } else {
            buffer.append( resolved );
        }
    }

    protected String resolvePlaceholder(Identifier identifier) {
        return null;
    }

    @Override
    protected void visit(StringLiteral node)
    {
        buffer.append('"');
        buffer.append( node.getSubstitutedValue( this::resolvePlaceholder ) );
        buffer.append('"');
    }
    
    public void setPrettyPrint(boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
    }
    
    public boolean isPrettyPrint()
    {
        return prettyPrint;
    }
}