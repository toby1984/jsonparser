package de.codesourcery.jsonparser.poe;

import de.codesourcery.jsonparser.Parser;
import de.codesourcery.jsonparser.ast.ASTNode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SkillTreeLoader
{
    private static final String CLASSPATH = "/skilltree.json";

    public SkillTree load() throws IOException
    {
        final StringBuilder buffer = new StringBuilder();
        try ( InputStream in = getClass().getResourceAsStream( CLASSPATH ) ) {
            if ( in == null ) {
                throw new FileNotFoundException( "Failed to open classpath:"+ CLASSPATH );
            }
            BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
            String line = null;
            while ( (line = reader.readLine() ) != null ) {
                buffer.append(line);
            }
        }
        final ASTNode ast = new Parser().parse( buffer.toString() );
        return new SkillTree( ast );
    }
}