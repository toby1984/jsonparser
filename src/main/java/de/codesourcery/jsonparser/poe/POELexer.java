package de.codesourcery.jsonparser.poe;

import de.codesourcery.jsonparser.Token;

import java.util.ArrayList;
import java.util.List;

public class POELexer
{
    private final POEScanner scanner;

    private StringBuilder buffer = new StringBuilder();
    private final List<POEToken> tokens =
            new ArrayList<>();

    public POELexer(POEScanner scanner)
    {
        this.scanner = scanner;
    }

    public POEToken peek() {
        while ( tokens.isEmpty() ) {
            parseTokens();
        }
        return tokens.get(0);
    }

    public POEToken next() {
        while ( tokens.isEmpty() ) {
            parseTokens();
        }
        return tokens.remove(0);
    }

    public int offset() {
        return peek().offset;
    }

    public boolean peek(POEToken.Type t) {
        return peek().hasType( t );
    }

    public void push(POEToken tok) {
        tokens.add(0,tok);
    }

    public boolean eof() {
        while ( tokens.isEmpty() ) {
            parseTokens();
        }
        return tokens.get(0).hasType( POEToken.Type.EOF );
    }

    private void parseTokens() {

        if ( ! tokens.isEmpty() ) {
            return;
        }
        buffer.setLength( 0 );
        while ( ! scanner.eof() && Character.isWhitespace( scanner.peek() ) ) {
            scanner.next();
        }
        int start = scanner.offset();
        while ( ! scanner.eof() )
        {
            char c = scanner.peek();
            if ( Character.isWhitespace( c ) ) {
                break;
            }
            switch(c) {
                case '%':
                    parseBuffer( start );
                    tokens.add( new POEToken(Character.toString( scanner.next() ),scanner.offset(),POEToken.Type.PERCENTAGE));
                    return;
                case '+':
                    scanner.next(); // consume
                    continue;
                case '.':
                    parseBuffer( start );
                    tokens.add( new POEToken(Character.toString( scanner.next() ),scanner.offset(),POEToken.Type.DOT));
                    return;
            }
            buffer.append(scanner.next());
         }
        parseBuffer(start);
        if ( scanner.eof() )
        {
            tokens.add( new POEToken( "", scanner.offset(), POEToken.Type.EOF ) );
        }
    }

    private void parseBuffer(int offset) {

        String s = buffer.toString();
        if ( s.length() == 0 ) {
            return;
        }
        boolean isNumber = true;
        for ( int i = 0 , len = s.length(); i < len ; i++ ) {
            if ( ! Character.isDigit( s.charAt(i) ) ) {
                isNumber = false;
                break;
            }
        }
        if ( isNumber )
        {
            tokens.add( new POEToken( s, offset, POEToken.Type.NUMBER ) );
            return;
        }
        if ( "or".equalsIgnoreCase( s ) ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.OR) );
            return;
        }
        if ( "and".equalsIgnoreCase( s ) ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.AND) );
            return;
        }
        if ( "to".equalsIgnoreCase( s ) ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.TO) );
            return;
        }
        if ( "chance".equalsIgnoreCase( s ) ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.CHANCE) );
            return;
        }
        if ( "while".equalsIgnoreCase( s ) ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.WHILE) );
            return;
        }
        if ( "reduced".equalsIgnoreCase( s ) || "reduce".equalsIgnoreCase( s ) || "reduction".equalsIgnoreCase( s )  ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.REDUCE) );
            return;
        }
        if ( "have".equalsIgnoreCase( s ) ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.HAVE) );
            return;
        }
        if ( "increased".equalsIgnoreCase( s ) ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.INCREASE) );
            return;
        }
        if ( "holding".equalsIgnoreCase( s ) || "wielding".equalsIgnoreCase( s )) {
            tokens.add( new POEToken( s, offset, POEToken.Type.WIELDING) );
            return;
        }
        if ( "second".equalsIgnoreCase( s ) || "seconds".equalsIgnoreCase( s )) {
            tokens.add( new POEToken( s, offset, POEToken.Type.SECOND) );
            return;
        }
        if ( "with".equalsIgnoreCase( s ) || "for".equalsIgnoreCase( s )  ) {
            tokens.add( new POEToken( s, offset, POEToken.Type.WITH) );
            return;
        }
        tokens.add( new POEToken(s, offset, POEToken.Type.TEXT ) );
    }

    @Override
    public String toString()
    {
        while ( tokens.isEmpty() ) {
            parseTokens();
        }
        return tokens.get(0).toString();
     }
}
