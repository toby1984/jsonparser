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

public class Identifier
{
    public final String name;
    
    public Identifier(String value) {
        if ( ! isValid(value) ) {
            throw new IllegalArgumentException("Invalid identifier: '"+value+"'");
        }
        this.name = value;
    }
    
    public static boolean isValid(String s) 
    {
        return getFirstInvalidIndex(s) == -1;
    }
    
    public static int getFirstInvalidIndex(String s) 
    {
        if ( s != null && s.length() > 0 ) 
        {
            for (int i = 0, len = s.length() ; i < len ; i++) 
            {
                final char c = Character.toLowerCase( s.charAt(i) );
                if ( ! isValidIdentifierChar( c ) ) {
                    return i;
                }
            }
            return -1;
        }
        return 0;
    }    
    
    @Override
    public String toString()
    {
        return name;
    }
    
    public static boolean isValidIdentifierChar(char c) 
    {
        if ( c != '_' ) 
        {
            if ( c < '0' || c > 'z' ) {
                return false;
            }
            if ( c > '9' && c < 'A' ) {
                return false;
            }
            if ( c > 'Z' && c < 'a' ) {
                return false;
            }
        }
        return true;
    }
}
