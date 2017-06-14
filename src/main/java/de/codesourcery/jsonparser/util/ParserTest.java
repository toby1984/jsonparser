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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.codesourcery.jsonparser.ILexer;
import de.codesourcery.jsonparser.Identifier;
import de.codesourcery.jsonparser.Lexer;
import de.codesourcery.jsonparser.Parser;
import de.codesourcery.jsonparser.ast.ASTNode;
import de.codesourcery.jsonparser.ast.JSONObject;

public class ParserTest extends JFrame
{
    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait( () -> new ParserTest().run() );
    }

    private final MyTreeModel treeModel = new MyTreeModel();
    
    private static final class MyTreeModel implements TreeModel 
    {
        private final List<TreeModelListener> listeners = new ArrayList<>();
        
        private ASTNode root;
        
        public void setRoot(ASTNode node) 
        {
            this.root = node;
        
            final TreeModelEvent ev = new TreeModelEvent(this , new Object[] { this.root } );
            listeners.forEach( l -> l.treeStructureChanged( ev ) );
        }
        
        @Override
        public Object getRoot()
        {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index)
        {
            return ((ASTNode) parent).child(index);
        }

        @Override
        public int getChildCount(Object parent)
        {
            return ((ASTNode) parent).childCount();
        }

        @Override
        public boolean isLeaf(Object node)
        {
            return ! ((ASTNode) node).hasChildren();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue)
        {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child)
        {
            return ((ASTNode) parent).indexOf( (ASTNode) child );
        }

        @Override
        public void addTreeModelListener(TreeModelListener l)
        {
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l)
        {
            listeners.remove(l);
        }};
    
    public ParserTest() 
    {
        super("Parser test");
    }

    private void run()
    {
        final JTextArea input = new JTextArea();
        input.setColumns( 40 );
        input.setRows( 10 );
        input.setText("{\"a\":1,\"b\":{\"c\":true}}");
        
        getContentPane().setLayout( new BorderLayout() );
        
        getContentPane().add( new JScrollPane( input ) , BorderLayout.NORTH );
        
        final JButton button = new JButton("Parse");
        button.addActionListener( ev -> 
        {
            treeModel.setRoot( new JSONObject() );
            if ( input.getText() != null ) 
            {
                final StringScanner scanner = new StringScanner( input.getText() );
                final ILexer lexer = new Lexer( scanner );
                final Parser p = new Parser();
                final ASTNode ast = p.parse( lexer );
                treeModel.setRoot( ast );
                
                final Function<Identifier,String> resolver = name -> 
                {
                    switch(name.name) {
                        case "test1":
                            return "test1";
                        case "test2":
                            return "test2";
                    }
                    return null;                    
                };
                final ASTPrinter printer = new ASTPrinter(); 
                System.out.println( "PRINTED: "+printer.print( ast , resolver ) );
            }
        });
        getContentPane().add(button, BorderLayout.CENTER );
        
        final JTree tree = new JTree();
        tree.setModel( treeModel );
        
        tree.setCellRenderer( new DefaultTreeCellRenderer() 
        {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                    boolean leaf, int row, boolean hasFocus)
            {
                final Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                
                return result;
            }
        });
        tree.setSize( new Dimension(200,200 ) );
        getContentPane().add( new JScrollPane( tree ), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo( null );
        setMinimumSize( new Dimension(640,480) );
        setVisible( true );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }
}
