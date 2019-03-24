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
package de.codesourcery.jsonparser.ast;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ASTNode
{
    public default List<ASTNode> children() 
    {
        return Collections.emptyList();
    }

    public default ASTNode child(int index) {
        return children().get(index);
    }
    
    public default int childCount() {
        return children().size();
    }
    
    public default boolean hasChildren() {
        return childCount() > 0;
    }
    
    public default boolean isLastChild(ASTNode node) {
        return indexOf(node) == childCount()-1;
    }

    public default int indexOf(ASTNode child) {
        return children().indexOf( child );
    }

    public default Object toJavaObject() {
        return toJavaObject( this );
    }

    public static Object toJavaObject(ASTNode node)
    {
        if ( node instanceof StringLiteral ) {
            return ((StringLiteral) node).value;
        }
        if ( node instanceof NullLiteral ) {
            return null;
        }
        if ( node instanceof NumberLiteral ) {
            final NumberLiteral num = (NumberLiteral) node;
            if ( num.value.contains( "." ) )
            {
                return Double.parseDouble( num.value );
            }
            return Integer.parseInt( num.value );
        }
        if ( node instanceof BooleanLiteral ) {
            return ((BooleanLiteral) node).value;
        }
        if ( node instanceof JSONArray ) {
            final JSONArray array = (JSONArray) node;
            final Object[] tmp = new Object[ array.childCount() ];
            Set<Class<?>> componentClass = new HashSet<>();
            final List<ASTNode> children1 = array.children();
            for (int i = 0; i < children1.size(); i++)
            {
                final ASTNode child = children1.get( i );
                final Object obj = child.toJavaObject();
                componentClass.add( obj.getClass() );
                tmp[i] = obj;
            }
            if ( componentClass.size() == 1 ) {
                final Object result = Array.newInstance( componentClass.iterator().next(), tmp.length );
                for ( int i = 0, len = tmp.length ; i < len ; i++ )
                {
                    Array.set( result, i, tmp[i] );
                }
                return result;
            }
            return tmp;
        }
        if ( node instanceof JSONObject )
        {
            final Map<String, Object> tmp = new HashMap<>();
            for (ASTNode child : node.children())
            {
                final KeyValue kv = (KeyValue) child;
                tmp.put( kv.key().value, kv.value().toJavaObject() );
            }
            return tmp;
        }
        throw new RuntimeException("Unhandled node type: "+node);
    }
}