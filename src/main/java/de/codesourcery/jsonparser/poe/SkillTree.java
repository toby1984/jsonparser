package de.codesourcery.jsonparser.poe;

import de.codesourcery.jsonparser.ast.ASTNode;
import de.codesourcery.jsonparser.util.ASTPrinter;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SkillTree
{
    public final ASTNode ast;

    public static class SkillNode
    {
        public final Integer id;

        public SkillNode(Integer id)
        {
            this.id = id;
        }
    }

    public static class SkillNodeImpl extends SkillNode
    {
        public final String description;
        public final String[] effects;

        public final List<SkillNode> in = new ArrayList<>(2);
        public final List<SkillNode> out = new ArrayList<>(2);

        public SkillNodeImpl(Integer id,String description,String[] effects)
        {
            super(id);
            this.description = description;
            this.effects = effects;
        }

        public int testParse()
        {
            int failureCount = 0;
            for ( String effect : effects )
            {
                final Parser p = new Parser();
                try
                {
//                    System.out.println("PARSE: '"+effect+"'");
                    p.parse( effect );
//                    System.out.println("SUCCESS: '"+effect+"'");
                } catch(Exception e) {
                    System.out.println("FAIL: '"+effect+"' ("+e.getMessage()+")");
                    failureCount++;
                }
            }
            return failureCount;
        }

        @Override
        public String toString()
        {
            return id+"[ "+description+", "+ ArrayUtils.toString( effects )+" ]";
        }
    }

    public final Map<Integer,SkillNode> nodes = new HashMap<>();

    public SkillTree(ASTNode ast) {
        this.ast = ast;
        parse();
    }

    private void parse()
    {
        final ASTPrinter printer = new ASTPrinter();
        printer.setPrettyPrint( true );
//        FileWriter w = null;
//        try
//        {
//            w = new FileWriter("/home/tobi/tmp/out.json");
//            w.write( printer.print( ast ) );
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
        final Map<String,Object> map = (Map<String, Object>) ast.toJavaObject();
        for ( String key : map.keySet())
        {
            final Object value = map.get(key);
//            System.out.println("GOT: "+key+"="+value);
            if ( "nodes".equals( key ) )
            {
                final Map<String,Object> map2 = (Map<String, Object>) value;
                map2.values().forEach( v -> parseSkillTreeNode( (Map<String,Object>) v ) );
                for ( SkillNode node : nodes.values() ) {
                    if ( !(node instanceof SkillNodeImpl) ) {
                        throw new RuntimeException("Unresolved node: "+node);
                    }
                }
                final Consumer<List<SkillNode>> converter = list -> {
                    for ( int i =0,len = list.size(); i < len ; i++ ) {
                        final SkillNode entry = list.get( i );
                        if ( !(entry instanceof SkillNodeImpl ) ) {
                            final SkillNode newValue = nodes.get( entry.id );
                            if ( !(newValue instanceof SkillNodeImpl) ) {
                                throw new RuntimeException( "Failed to resolve "+entry.id );
                            }
                            list.set(i, newValue );
                        }
                    }
                };
                for ( SkillNode node : nodes.values() )
                {
                    final SkillNodeImpl impl = (SkillNodeImpl) node;
                    converter.accept( impl.in );
                    converter.accept( impl.out );
                }
            }
        }
//        nodes.values().forEach( System.out::println );
        System.out.println("Found "+nodes.size()+" nodes.");

        int failureCount = 0;
        int successCount = 0;
        for (SkillNode sn : nodes.values())
        {
            final int failCnt = ((SkillNodeImpl) sn).testParse();
            successCount += ((SkillNodeImpl) sn).effects.length - failCnt;
            failureCount += failCnt;
        }
        final float ratio = 100*(failureCount/(float) (successCount+failureCount));
        System.out.println("Parsing finished ("+successCount+" success/ "+failureCount+" failures, "+ratio+"% )");
    }

    private void parseSkillTreeNode(Map<String,Object> json)
    {
        final Integer id = ((Number) json.get("id")).intValue();

        SkillNode toAdd = nodes.get( id );
        if ( toAdd instanceof SkillNodeImpl) {
            return;
        }
        final String description = (String) json.get("dn");
        final String[] effects = toStringArray( json.get("sd") );
        final Integer[] in = toIntArray( json.get("in") );
        final Integer[] out = toIntArray( json.get("out") );
        toAdd = new SkillNodeImpl( id, description, effects );
        for ( Integer newId : in ) {
            final SkillNode node = nodes.computeIfAbsent( newId, SkillNode::new );
            ((SkillNodeImpl) toAdd).in.add( node );
        }
        for ( Integer newId : out ) {
            final SkillNode node = nodes.computeIfAbsent( newId, SkillNode::new );
            ((SkillNodeImpl) toAdd).out.add( node );
        }
        nodes.put(id, toAdd );
    }

    private Integer[] toIntArray(Object obj) {
        if ( obj == null ) {
            return new Integer[0];
        }
        if ( ! obj.getClass().isArray() ) {
            throw new IllegalArgumentException( "Not an array: "+obj );
        }
        if ( Array.getLength(obj) == 0 ) {
            return new Integer[0];
        }
        final Class<?> components = obj.getClass().getComponentType();
        if ( components == Integer.class ) {
            return (Integer[]) obj;
        }
        throw new IllegalArgumentException( "Don't know how to convert "+obj+" (elements: "+components.getName()+")");
    }

    private String[] toStringArray(Object obj) {
        if ( obj == null ) {
            return new String[0];
        }
        if ( ! obj.getClass().isArray() ) {
            throw new IllegalArgumentException( "Not an array: "+obj );
        }
        if ( Array.getLength(obj) == 0 ) {
            return new String[0];
        }
        final Class<?> components = obj.getClass().getComponentType();
        if ( components == String.class ) {
            return (String[]) obj;
        }
        throw new IllegalArgumentException( "Don't know how to convert "+obj+" (elements: "+components.getName()+")");
    }
}