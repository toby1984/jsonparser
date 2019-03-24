package de.codesourcery.jsonparser.poe;

import java.util.HashSet;
import java.util.Set;

public class Subjects
{
    private static final Set<String> subjects = new HashSet<>();

    static
    {
        // attributes
        add("Non-Chaos Damage");
        add("Extra Chaos Damage");
        add("Critical Strike Chance");
        add("Life");
        add("Trap Damage");
        add("Trap Throwing Speed");
        add("Enemy Stun Threshold");
        add("Attack Damage");
        add("Attack Speed");
        add("Stun Duration");
        add("Endurance Charge");
        add("Mana Cost of Skills");
        add("Mana Regeneration Rate");
        add("Mana");
        add("Damage");
        add("Mana Reserved");
        add("Physical Damage");
        add("Recovery per second");
        add("Endurance Charge Duration");
        add("Life Generated per second");
        add("Cold Resistance");
        add("Damage with Ailments");
        add("Damage with Weapons");
        add("Elemental Damage");
        add("Accuracy Rating");
        add("Cold Resistance");
        add("Fire Resistance");
        add("Lightning Resistance");
        add("Maximum Endurance Charges");
        add("Area Of Effect");

        // states
        add("Dual Wielding");

        // items
        add("Maces");
        add("Claws");
        add("Swords");
        add("Traps");
        add("Minions");
        add("Skeleton");
        add("Skeletons");
        add("Shield");
        add("Remote Mine");
        add("Trap");
        add("Bows");

        // skills
        add("Herald of Ice");
        add("Bow Skills");
        add("Herald of Ash");
        add("Herald of Thunder");
        add("Herald Skills");
        add("Channeling Skills");
        add("Damage Over Time");
        add("Attack Skills");
        add("Attack Skills");
        add("Life Leech");

        // other
        add("Rare Or Unique Enemy");
        add("Enemy");
        add("Enemies");
    }

    private static void add(String s) {
        subjects.add( s.toLowerCase() );
    }

    public static boolean isSubject(String s)
    {
        return subjects.contains( s.toLowerCase() );
    }
}
