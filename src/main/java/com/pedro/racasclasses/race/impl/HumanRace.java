package com.pedro.racasclasses.race.impl;

import com.pedro.racasclasses.race.Race;

public class HumanRace implements Race {
    @Override public String getId() { return "humano"; }
    @Override public String getDisplayName() { return "Humano"; }
}