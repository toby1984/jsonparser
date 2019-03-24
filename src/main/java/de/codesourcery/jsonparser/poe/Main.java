package de.codesourcery.jsonparser.poe;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        final SkillTree tree = new SkillTreeLoader().load();
    }
}
