package de.codesourcery.jsonparser.poe;

public class POEToken
{
    public final String value;
    public final int offset;
    public final Type type;

    public POEToken(String value, int offset, Type type)
    {
        this.value = value;
        this.offset = offset;
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "POEToken{" +
                "value='" + value + '\'' +
                ", offset=" + offset +
                ", type=" + type +
                '}';
    }

    public boolean hasType(Type t) {
        return this.type == t;
    }

    public enum Type
    {
        // misc
        NUMBER,
        TEXT,
        // characters
        PERCENTAGE,
        DOT,
        EOF,
        // operators
        INCREASE,
        REDUCE,
        HAVE,
        WITH,
        WHILE,
        WIELDING,
        AND,
        TO,
        CHANCE,
        OR,
        // words
        SECOND
    }
}

