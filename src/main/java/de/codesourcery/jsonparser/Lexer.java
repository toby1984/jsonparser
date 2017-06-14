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
package de.codesourcery.jsonparser;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.jsonparser.Token.TokenType;
import de.codesourcery.jsonparser.util.MyParseException;

public class Lexer implements ILexer
{
    private final IScanner scanner;
    
    private final List<Token> tokens = new ArrayList<>();
    private final StringBuilder buffer = new StringBuilder();

    private boolean ignoreWhitespace = true;
    
    public Lexer(IScanner scanner)
    {
        this.scanner = scanner;
    }

    private void parse() 
    {
        if ( ! tokens.isEmpty() ) {
            return;
        }
        
        int offset = scanner.offset();

        buffer.setLength( 0 );
        
        // parse whitespace
        while ( ! scanner.eof() && Character.isWhitespace( scanner.peek() ) ) 
        {
            final char c = scanner.next();
            if ( ! ignoreWhitespace ) {
                buffer.append( c );
            }
        }
        
        if ( ! ignoreWhitespace && buffer.length() > 0 ) 
        {
            token( TokenType.WHITESPACE , buffer.toString() , offset );
            return;
        }
        
        while ( ! scanner.eof() ) 
        {
            final char c = scanner.peek();
            if ( Character.isWhitespace( c ) ) {
                break;
            }
            switch( c ) 
            {
                case '.':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.DOT , c , offset );
                    return;
                case ',':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.COMMA, c , offset );
                    return;   
                case ':':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.COLON, c , offset );
                    return;                     
                case '\\':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.BACKSLASH, c , offset );
                    return;
                case '{':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.CURLY_BRACE_OPEN, c , offset );
                    return; 
                case '[':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.ANGLE_BRACKETS_OPEN, c , offset );
                    return;  
                case ']':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.ANGLE_BRACKETS_CLOSE, c , offset );
                    return;                      
                case '}':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.CURLY_BRACE_CLOSE, c , offset );
                    return;     
                case '$':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.DOLLAR , c , offset );
                    return;                         
                case '"':
                    parseBuffer(offset);
                    offset = scanner.offset();
                    scanner.next();
                    token( TokenType.DOUBLE_QUOTE , c , offset );
                    return;      
                default:
                    buffer.append( scanner.next() );
            }
        }
        
        parseBuffer(offset);
        
        if ( scanner.eof() ) 
        {
            token(TokenType.EOF,scanner.offset() );
        }        
    }
    
    private void token(TokenType t,int offset) 
    {
        tokens.add( new Token(t,offset ) );
    }
    
    private void token(TokenType t,char value,int offset) 
    {
        tokens.add( new Token(t,Character.toString( value ) , offset ) );
    }
    
    private void token(TokenType t,String value,int offset) 
    {
        tokens.add( new Token(t,value , offset ) );
    }
    
    private void parseBuffer(int offset) {
     
        if ( buffer.length() == 0 ) {
            return;
        }
        boolean allDigits = true;
        for ( int i = 0 ; i < buffer.length() && allDigits ; i++ ) 
        {
            allDigits &= Character.isDigit( buffer.charAt( i ) );
        }
        if ( allDigits ) 
        {
            token(TokenType.DIGITS,buffer.toString(),offset);
            return;
        }
        if ("true".equals( buffer.toString())) {
            token(TokenType.TRUE,buffer.toString(),offset);
            return;
        }
        if ("false".equals( buffer.toString())) {
            token(TokenType.FALSE,buffer.toString(),offset);
            return;
        }        
        if ("null".equals( buffer.toString())) {
            token(TokenType.NULL,buffer.toString(),offset);
            return;
        }        
        token(TokenType.TEXT,buffer.toString(),offset);
    }
    
    public Token peek()
    {
        parse();
        return tokens.get(0);
    }

    public boolean eof()
    {
        parse();
        return tokens.get(0).is( TokenType.EOF );
    }

    public Token next()
    {
        parse();
        return tokens.remove( 0 );
    }

    @Override
    public Token expect(TokenType t)
    {
        if ( ! peek().is( t ) ) {
            throw new MyParseException("Expected "+t+" but got "+peek(),peek().offset);
        }
        return next();
    }

    @Override
    public void setIgnoreWhitespace(boolean nowIgnoreWhitespace)
    {
        if ( ! tokens.isEmpty() ) 
        {
            scanner.setOffset( tokens.get(0).offset );
            tokens.clear();
        }
        this.ignoreWhitespace = nowIgnoreWhitespace;
    }
}