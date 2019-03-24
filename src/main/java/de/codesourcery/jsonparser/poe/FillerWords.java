package de.codesourcery.jsonparser.poe;

import java.util.HashSet;
import java.util.Set;

public class FillerWords
{
    private static final Set<String> fillers = new HashSet<>();

    static {
        add("Extra");
        add("deal");
        add("dealt");
        add("you");
        add("a");
    }

    private static void add(String s) {
        fillers.add( s.toLowerCase() );
    }

    public static boolean isFiller(String s) {
        return fillers.contains( s.toLowerCase() );
    }
}
