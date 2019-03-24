package de.codesourcery.jsonparser.poe;

public class POEScanner
{
    private final String input;
    private int index;

    public POEScanner(String input)
    {
        this.input = input;
    }

    public boolean eof() {
        return index >= input.length();
    }

    public char peek() {
        if ( eof() ) {
            throw new IllegalStateException( "Already at EOF" );
        }
        return input.charAt(index);
    }

    public char next() {
        if ( eof() ) {
            throw new IllegalStateException( "Already at EOF" );
        }
        return input.charAt(index++);
    }

    public int offset() {
        return index;
    }
}
