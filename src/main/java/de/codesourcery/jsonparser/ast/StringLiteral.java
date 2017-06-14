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
package de.codesourcery.jsonparser.ast;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import de.codesourcery.jsonparser.Identifier;
import de.codesourcery.jsonparser.util.MyParseException;

public class StringLiteral implements ASTNode
{
    public String value;

    public StringLiteral(String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return '"' + value+'"';
    }
    
    public String getSubstitutedValue(Function<Identifier,String> resolver) 
    {
        Set<Identifier> placeholders = getPlaceholderNames();
        if ( placeholders.isEmpty() ) {
            return this.value;
        }
        String tmp = this.value;
        for ( Identifier id : placeholders ) 
        {
            final String resolved = resolver.apply( id );
            if ( resolved != null ) {
                tmp = tmp.replace( "${"+id.name+"}" , resolved );
            }
        }
        return tmp;
    }
    
    public Set<Identifier> getPlaceholderNames() 
    {
        final Set<Identifier> names = new HashSet<>();
        
        final StringBuilder buffer = new StringBuilder();
        boolean inIdentifier = false;
        boolean escaped = false;
        boolean gotStart = false;
        for (int i = 0 , len = value.length() ; i < len ; i++) 
        {
            char c = value.charAt(i);
            
            if ( ! inIdentifier ) 
            {
                if ( ! escaped && c == '\\' ) {
                    escaped = true;
                    continue;
                }
                if ( escaped ) {
                    escaped = false;
                    continue;
                }
                if ( c == '$' ) {
                    inIdentifier = true;
                    gotStart = false;
                }
                continue;
            }
            
            if ( ! gotStart ) 
            {
                if ( c != '{' ) {
                    throw new MyParseException("Expected '{'",i);
                }
                gotStart = true;
                continue;
            }
            
            if ( c == '}' ) 
            {
                gotStart = false;
                inIdentifier = false;
                if ( buffer.length() == 0 ) {
                    throw new MyParseException("Placeholder name required",i);
                }
                names.add( new Identifier( buffer.toString() ) );
                buffer.setLength( 0 );
                continue;
            }
            
            if ( ! Identifier.isValidIdentifierChar( c ) ) {
                throw new MyParseException("Character '"+c+"' not allowed in identifier",i);
            }
            buffer.append( c );
        }
        if ( inIdentifier ) {
            throw new MyParseException("Unterminated placeholder expression",value.length());
        }
        return names;
    }
}
