package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

public class HumanRace implements Race {
    @Override public String getId() { return "humano"; }
    @Override public String getDisplayName() { return "Humano"; }

    // Variante +1 em tudo do PHB; CHA → LUCK.
    @Override public int getRacialStrength() { return 1; }
    @Override public int getRacialDexterity() { return 1; }
    @Override public int getRacialConstitution() { return 1; }
    @Override public int getRacialIntelligence() { return 1; }
    @Override public int getRacialWisdom() { return 1; }
    @Override public int getRacialLuck() { return 1; }
    @Override public int getFreeAttributePoints() { return 0; }
}
