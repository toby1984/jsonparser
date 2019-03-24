package de.codesourcery.jsonparser.poe;

public enum Attribute
{
    STRENGTH("Strength"),
    DEXTERITY("Dexterity"),
    INTELLIGENCE("Intelligence"),
    MANA("Mana"),
    SPELL_DAMAGE("Spell Damage"),
    PHYSICAL_DAMAGE("Physical Damage"),
    DAMAGE("Damage"),
    CHANCE_BLOCK_SPELL_DAMAGE("Chance to Block Spell Damage"),
    BLOCK_RECOVERY("Block Recovery"),
    CRITICAL_STRIKE_CHANCE("Critical Strike Chance"),
    CRITICAL_STRIKE_MULTIPLIER("Critical Strike Multiplier"),
    ELEMENTAL_RESISTANCES("Elemental Resistances"),
    LIFE("Life");

    public final String name;

    Attribute(String name)
    {
        this.name = name;
    }
}
