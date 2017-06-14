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
package de.codesourcery.jsonparser.util;

import de.codesourcery.jsonparser.ILexer;
import de.codesourcery.jsonparser.Token;
import de.codesourcery.jsonparser.Token.TokenType;

public class CharStream 
{
    private final ILexer lexer;
    
    public boolean escaped;
    public Token token; 
    private int index;
    
    public CharStream(ILexer lexer) 
    {
        this.lexer = lexer;
        fetch();
    }
    
    protected boolean fetch() 
    {
        index = 0;
        this.token = lexer.peek();
        if ( token.is(TokenType.DOUBLE_QUOTE ) && ! escaped ) 
        {
            token = new Token(TokenType.EOF,lexer.peek().offset);
            return false;
        }
        lexer.next();
        return token.isNot(TokenType.EOF);
    }
    
    public int offset() {
        return token.offset + index;
    }
    
    private int currentLen() {
        return token.value.length();
    }        
    
    public boolean hasNext() 
    {
        if ( index >= currentLen() ) 
        {
            if ( ! fetch() ) {
                return false;
            }
        }
        return token.isNot( TokenType.EOF ) && index < currentLen();
    }
    
    public char next() {
        if ( ! hasNext() ) {
            throw new IllegalStateException("Called past end of token stream");
        }
        return token.value.charAt( index++ );
    }
}