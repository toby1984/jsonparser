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

import java.util.Collections;
import java.util.List;

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
}