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

public class Token
{
    public static final String EMPTY_STRING = "";
    
    public final String value;
    public final int offset;
    public final TokenType type;
    
    public Token(TokenType type, int offset) {
        this(type,EMPTY_STRING,offset);
    }
    
    public Token(TokenType type, String value, int offset)
    {
        this.value = value;
        this.offset = offset;
        this.type = type;
    }
    
    public boolean is(TokenType t) {
        return t.equals(this.type);
    }
    
    public boolean isNot(TokenType t) {
        return ! t.equals(this.type);
    }    
    
    @Override
    public String toString()
    {
        return "Token[ "+type+" = "+value+" @ "+offset+" ]";
    }

    public static enum TokenType 
    {
        CURLY_BRACE_OPEN, // ok
        CURLY_BRACE_CLOSE, // ok
        DOUBLE_QUOTE,
        DOT, // ok
        DOLLAR, // ok
        WHITESPACE, // ok
        TRUE, // ok
        FALSE, // ok
        NULL, // ok
        COMMA, // ok
        COLON, // ok
        DIGITS, // ok
        TEXT, // ok
        BACKSLASH, // ok
        EOF, // ok
        ANGLE_BRACKETS_OPEN, // ok
        ANGLE_BRACKETS_CLOSE, // ok
    }
}
