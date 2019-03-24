package de.codesourcery.jsonparser.poe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class Parser
{
    private POELexer lexer;
    private Stack<EffectNode> valueStack = new Stack<>();

    public enum OperatorType
    {
        AND,
        OR,
        TO,
        WHILE,
        INCREASE,
        REDUCTION,
        CHANCE_TO,
        WITH,
        HAVE,
        // prefix
        WIELDING {
            @Override
            public boolean isInfixOperator()
            {
                return false;
            }

            @Override
            public boolean isPrefixOperator()
            {
                return true;
            }
        };

        public boolean isInfixOperator() {
            return true;
        }

        public boolean isPrefixOperator() {
            return false;
        }
    }

    public static class EffectNode
    {
        public final List<EffectNode> children=new ArrayList<>();

        public String toString() {
            StringBuilder result = new StringBuilder();
            toString(result,0);
            return result.toString();
        }

        protected static final String indent(int depth) {
            String s = "----";
            while( depth-- > 0 ) {
                s += "----";
            }
            return s;
        }

        public void toString(StringBuilder result, int depth) {
            result.append( indent(depth)+" AST\n");
            for ( EffectNode child : children ) {
                child.toString( result,depth+1 );
            }
        }
    }

    public static final class SubjectNode extends EffectNode
    {
        public final String value;


        public SubjectNode(String value)
        {
            this.value = value;
        }

        @Override
        public void toString(StringBuilder result, int depth)
        {
            result.append( indent( depth ) ).append( " - [S] " ).append( value );
        }
    }

    public static final class NumberNode extends EffectNode
    {
        public final double value;
        public final boolean isPercentage;

        public NumberNode(double value, boolean isPercentage)
        {
            this.value = value;
            this.isPercentage = isPercentage;
        }

        @Override
        public void toString(StringBuilder result, int depth)
        {
            if ( isPercentage )
            {
                result.append( indent( depth ) ).append( " +" ).append( value ).append( "%" );
            } else {
                result.append( indent( depth ) ).append( " +" ).append( value );
            }
        }
    }

    public static final class StringNode extends EffectNode
    {
        public final String value;

        public StringNode(String value)
        {
            this.value = value;
        }

        @Override
        public void toString(StringBuilder result, int depth)
        {
            result.append( indent( depth ) ).append( " " ).append( value );
        }
    }

    public static final class OperatorNode extends EffectNode
    {
        public OperatorNode(OperatorType type)
        {
            this.type = type;
        }

        public final OperatorType type;

        public void toString(StringBuilder result, int depth) {
            result.append( indent(depth)+" "+type.name()+"\n");
            for (Iterator<EffectNode> iterator = children.iterator(); iterator.hasNext(); )
            {
                EffectNode child = iterator.next();
                child.toString( result, depth + 1 );
                if ( iterator.hasNext() ) {
                    result.append("\n");
                }
            }
        }
    }

    public EffectNode parse(String input)
    {
        lexer = new POELexer( new POEScanner( input ) );
        while ( ! lexer.eof() )
        {
            if ( ! parseStatement() ) {
                break;
            }
        }
        if ( ! lexer.eof() ) {
            throw new IllegalStateException( "Lexer not at EOF after parsing? "+lexer.offset() );
        }
        if ( valueStack.isEmpty() ) {
            return new EffectNode();
        }
        if ( valueStack.size() > 1 ) {
            throw new IllegalStateException( "More than one value on stack?" );
        }
        return valueStack.get(0);
    }

    private boolean parseStatement()
    {
        if ( parseValue() ) {
            parseInfixOperator();
            return true;
        }
        return parseInfixOperator();
    }

    private OperatorType getPrefixOperatorType()
    {
        switch( lexer.peek().type )
        {
            case WIELDING:
                lexer.next();
                return OperatorType.WIELDING;
        }
        return null;
    }

    private OperatorType getInfixOperatorType()
    {
        if ( lexer.peek( POEToken.Type.CHANCE ) ) {
            final POEToken tok = lexer.next();
            if ( lexer.peek(POEToken.Type.TO) )
            {
                lexer.next();
                return OperatorType.CHANCE_TO;
            }
            lexer.push(tok);
            return null;
        }
        switch( lexer.peek().type )
        {
            case HAVE:
                lexer.next();
                return OperatorType.HAVE;
            case REDUCE:
                lexer.next();
                return OperatorType.REDUCTION;
            case TO:
                lexer.next();
                return OperatorType.TO;
            case AND:
                lexer.next();
                return OperatorType.AND;
            case OR:
                lexer.next();
                return OperatorType.OR;
            case WHILE:
                lexer.next();
                return OperatorType.WHILE;
            case WITH:
                lexer.next();
                return OperatorType.WITH;
            case INCREASE:
                lexer.next();
                return OperatorType.INCREASE;
        }
        return null;
    }

    private boolean parseInfixOperator()
    {
        final OperatorType type = getInfixOperatorType();
        if ( type != null )
        {
            if ( ! parseValue() ) {
                throw new IllegalStateException( "Failed to parse 2nd argument of "+type );
            }
            if ( valueStack.size() < 2 ) {
                throw new IllegalStateException( "Expected at least 2 values on value stack but got "+valueStack.size() );
            }
            final OperatorNode node = new OperatorNode( type );
            node.children.add( valueStack.pop() );
            node.children.add( valueStack.pop() );
            Collections.reverse(node.children);
            valueStack.push(node);
            return true;
        }
        return false;
    }

    private boolean parseValue()
    {
        if ( lexer.peek( POEToken.Type.TEXT ) )
        {
            final StringBuilder words = new StringBuilder();
            String lastSubject = null;
            while ( ! lexer.eof() )
            {
                final POEToken tok = lexer.peek();
                if ( FillerWords.isFiller( tok.value ) ) {
                    lexer.next();
                    continue;
                }
                final String appended = words.length() == 0 ? tok.value : words+" "+tok.value;
                if ( Subjects.isSubject( appended) || tok.hasType( POEToken.Type.TEXT ) )
                {
                    if ( words.length() > 0 )
                    {
                        words.append( " " );
                    }
                    words.append( lexer.next().value );
                    if ( Subjects.isSubject( words.toString() ) ) {
                        lastSubject = words.toString();
                    }
                } else {
                    break;
                }
            }
            if ( lastSubject != null ) {
                valueStack.push( new SubjectNode( lastSubject ) );
                words.delete( 0, lastSubject.length() );
            }
            if ( words.length() > 0 )
            {
                valueStack.push( new StringNode( words.toString() ) );
            }
            return true;
        }
        if ( parsePrefixOperator() )
        {
            return true;
        }
        if ( parseNumber() ) {
            return true;
        }
        return false;
    }

    private boolean parsePrefixOperator()
    {
        final OperatorType prefixOp = getPrefixOperatorType();
        if ( prefixOp != null )
        {
            if ( ! parseValue() ) {
                throw new IllegalStateException( "Prefix operator "+prefixOp+" needs a value" );
            }
            final OperatorNode op = new OperatorNode( prefixOp );
            op.children.add( valueStack.pop() );
            valueStack.push(op);
            return true;
        }
        return false;
    }

    private boolean parseNumber()
    {
        if ( lexer.peek(POEToken.Type.NUMBER ) )
        {
            POEToken tok = lexer.next();
            String input = tok.value;
            if ( lexer.peek(POEToken.Type.DOT ) )
            {
                lexer.next();
                if ( !lexer.peek( POEToken.Type.NUMBER ) )
                {
                    throw new RuntimeException( "Expected number after DOT @ " + lexer.offset() );
                }
                input += lexer.next().value;
            }
            boolean percentage = false;
            if ( lexer.peek( POEToken.Type.PERCENTAGE ) ) {
                lexer.next();
                percentage = true;
            }
            valueStack.push( new NumberNode( Double.parseDouble( input ),percentage ) );
            return true;
        }
        return false;
    }

    public static void main(String[] args)
    {
        final String input = "5% reduced Mana Cost of Skills";
        final POELexer lexer = new POELexer( new POEScanner( input ) );
        while ( ! lexer.eof() ) {
            System.out.println("TOKEN: "+lexer.next());
        }
        Parser p = new Parser();
        final EffectNode ast = p.parse( input );
        System.out.println("GOT: \n\n"+ast.toString());
    }

}