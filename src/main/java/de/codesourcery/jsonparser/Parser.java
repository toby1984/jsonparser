/**
 * Copyright 2017 Tobias Gierke <tobias.gierke@code-sourcery.de>
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
package de.codesourcery.jsonparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import de.codesourcery.jsonparser.Token.TokenType;
import de.codesourcery.jsonparser.ast.ASTNode;
import de.codesourcery.jsonparser.ast.BooleanLiteral;
import de.codesourcery.jsonparser.ast.JSONArray;
import de.codesourcery.jsonparser.ast.JSONObject;
import de.codesourcery.jsonparser.ast.KeyValue;
import de.codesourcery.jsonparser.ast.NullLiteral;
import de.codesourcery.jsonparser.ast.NumberLiteral;
import de.codesourcery.jsonparser.ast.PlaceholderExpression;
import de.codesourcery.jsonparser.ast.StringLiteral;
import de.codesourcery.jsonparser.util.CharStream;
import de.codesourcery.jsonparser.util.MyParseException;
import de.codesourcery.jsonparser.util.StringScanner;

public class Parser
{
    private ILexer lexer;

    private boolean supportsPlaceholders = true;
    
    private Stack<ASTNode> stack = new Stack<>();

    public ASTNode parse(String s) 
    {
        StringScanner scanner = new StringScanner(s);
        return parse( new Lexer(scanner ) );
    }
    
    public ASTNode parse(ILexer lexer) {

        this.lexer = lexer;
        stack.clear();
        parseObject();
        if ( ! lexer.eof() ) {
            throw new MyParseException("Garbage at end of input",lexer.peek().offset);
        }
        if ( stack.isEmpty() ) {
            throw new MyParseException("Premature end of input",lexer.peek().offset);
        }
        return stack.pop();
    }

    private Token consume(TokenType t) 
    {
        if ( ! lexer.peek().is( t ) ) 
        {
            throw new MyParseException("Expected "+t+" but got "+lexer.peek(),lexer.peek().offset);
        }
        return lexer.next();
    }

    private boolean expect(TokenType t) 
    {
        if ( lexer.peek().is( t ) ) {
            return true;
        }
        return false;
    }

    private static boolean isHexDigit(char c) 
    {
        c = Character.toLowerCase( c ); 
        if ( Character.isDigit( c ) ) {
            return true;
        }
        return c >= 'a' && c <= 'f';
    }
    
    private boolean parseString() 
    {
        if (expect( TokenType.DOUBLE_QUOTE ) ) 
        {
            final StringBuilder buffer = new StringBuilder();
            consume( TokenType.DOUBLE_QUOTE );

            if ( lexer.peek().isNot(TokenType.DOUBLE_QUOTE ) ) 
            {
                lexer.setIgnoreWhitespace( false );
                try 
                {
                    final CharStream stream = new CharStream(lexer);
                    while (  stream.hasNext() ) 
                    {
                        if ( ! stream.escaped && stream.token.is(TokenType.BACKSLASH) ) {
                            stream.next();
                            stream.escaped = true;
                            continue;
                        }
                        char c = stream.next();
                        if ( stream.escaped ) 
                        {
                            stream.escaped = false;
                            switch( c ) {
                                case 'u':
                                    buffer.append( "\\u" );
                                    for ( int i = 0 ; i < 4 ; i++ ) 
                                    {
                                        if ( ! stream.hasNext() ) {
                                            throw new MyParseException("\\u requires 4 hex digits" , stream.offset()-1 );
                                        }
                                        c = stream.next(); 
                                        if ( ! isHexDigit( c ) ) {
                                            throw new MyParseException("\\u requires 4 hex digits" , stream.offset()-1 );
                                        }
                                        buffer.append(c);
                                    }
                                    continue;
                                case '$':
                                    if ( supportsPlaceholders ) {
                                        buffer.append("$");
                                        continue;                                           
                                    }
                                    throw new MyParseException("Invalid escape sequence" , stream.offset()-1 );                                    
                                case '"':
                                case '\\':
                                case '/':
                                case 'b':
                                case 'f':
                                case 'n':
                                case 'r':
                                case 't':
                                    buffer.append("\\");
                                    buffer.append( c );
                                    continue;            
                                default:
                                    throw new MyParseException("Invalid escape sequence" , stream.offset()-1 );
                            }
                        }
                        buffer.append( c  );
                        stream.escaped = false;
                    }
                } finally {
                    lexer.setIgnoreWhitespace( true );
                }
            }

            consume(TokenType.DOUBLE_QUOTE);
            stack.push( new StringLiteral( buffer.toString() ) );
            return true;
        }
        return false;
    }

    private boolean parseArray() 
    {
        if ( lexer.peek().is(TokenType.ANGLE_BRACKETS_OPEN) ) 
        {
            consume(TokenType.ANGLE_BRACKETS_OPEN);
            final List<ASTNode> nodes = new ArrayList<>();
            boolean expectedingMore = false;
            while ( ! lexer.eof() && ! lexer.peek().is(TokenType.ANGLE_BRACKETS_CLOSE) ) 
            {
                // parse value
                if ( ! parseValue() ) {
                    throw new MyParseException("Expected a value",lexer.peek().offset);
                }
                expectedingMore = false;
                nodes.add( stack.pop() );
                if ( lexer.peek().is(TokenType.COMMA) ) 
                {
                    consume(TokenType.COMMA);
                    expectedingMore  = true;
                }
            }
            if ( expectedingMore ) {
                throw new MyParseException("JSON array requires an additional value" , lexer.peek().offset );
            }
            consume(TokenType.ANGLE_BRACKETS_CLOSE);

            final JSONArray array = new JSONArray();
            array.add( nodes );
            stack.push( array );
            return true;
        }
        return false;
    }

    private boolean parseValue() 
    {
        if ( parseString() ) {
            return true;
        }
        
        if ( lexer.peek().is( TokenType.TRUE ) || lexer.peek().is( TokenType.FALSE ) ) {
            final Token tok = lexer.next();
            stack.push( new BooleanLiteral( tok.is(TokenType.TRUE ) ) );
            return true;
        }           

        if ( lexer.peek().is( TokenType.NULL ) ) {
            lexer.next();
            stack.push( new NullLiteral() );
            return true;
        }
        
        if ( parseNumber() ) {
            return true;
        }         
        
        if ( supportsPlaceholders && parsePlaceholderExpression() ) {
            return true;
        }
        
        if ( parseObject() ) {
            return true;
        }

        if ( parseArray() ) {
            return true;
        }        
       
        return false;
    }

    private boolean parsePlaceholderExpression() 
    {
        if ( lexer.peek().is(TokenType.DOLLAR ) ) 
        {
            consume(TokenType.DOLLAR);
            consume(TokenType.CURLY_BRACE_OPEN);
            final StringBuffer buffer = new StringBuffer();
            while ( ! lexer.eof() && ! lexer.peek().is(TokenType.CURLY_BRACE_CLOSE)) {
                final Token tok = lexer.next();
                if ( ! Identifier.isValid( tok.value ) ) 
                {
                    throw new MyParseException("Invalid characters in identifier name",tok.offset + Identifier.getFirstInvalidIndex( tok.value ) );
                }
                buffer.append( tok.value );
            }
            if ( buffer.length() == 0 ) {
                throw new MyParseException("Missing identifier name",lexer.peek().offset);
            }
            consume(TokenType.CURLY_BRACE_CLOSE);
            stack.push( new PlaceholderExpression( new Identifier( buffer.toString() ) ) );
            return true;
        }
        return false;
    }
    
    private boolean parseNumber() {

        if ( lexer.peek().is(TokenType.DIGITS ) ) 
        {
            String v1 = lexer.next().value;
            String v2 = null;
            if ( lexer.peek().is(TokenType.DOT ) ) 
            {
                consume(TokenType.DOT);
                if ( ! lexer.peek().is(TokenType.DIGITS) ) {
                    throw new MyParseException("Expected digits but got "+lexer.peek(),lexer.peek().offset);
                }
                v2 = lexer.next().value;
            }
            stack.push( new NumberLiteral( v2 == null ? v1 : v1+"."+v2 ) );
            return true;
        }
        return false;
    }

    private boolean parseObject() {

        if ( expect( TokenType.CURLY_BRACE_OPEN ) ) 
        {
            consume(TokenType.CURLY_BRACE_OPEN);

            final JSONObject object = new JSONObject();
            
            boolean needsMore = false;
            while ( ! lexer.eof() && ! lexer.peek().is(TokenType.CURLY_BRACE_CLOSE ) ) 
            {
                if ( parseString() ) 
                {
                    final StringLiteral lit = (StringLiteral) stack.pop();
                    consume(TokenType.COLON);
                    if ( ! parseValue() ) {
                        throw new MyParseException("Found no valid value",lexer.peek().offset);
                    }
                    needsMore = false;
                    final ASTNode value = stack.pop();
                    object.add( new KeyValue( lit , value ) );
                    if ( lexer.peek().is(TokenType.COMMA ) ) {
                        lexer.next();
                        needsMore = true;
                    }
                } else {
                    break;
                }
            }
            if ( needsMore ) {
                throw new MyParseException("Expected another value",lexer.peek().offset);
            }
            stack.push( object );
            consume(TokenType.CURLY_BRACE_CLOSE);
            return true;
        }
        return false;
    }
    
    public void setSupportsPlaceholders(boolean supportsPlaceholders)
    {
        this.supportsPlaceholders = supportsPlaceholders;
    }
    
    public boolean isSupportsPlaceholders()
    {
        return supportsPlaceholders;
    }
}