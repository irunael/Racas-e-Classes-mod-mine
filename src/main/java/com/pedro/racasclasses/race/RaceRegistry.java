package com.pedro.racasclasses.race;

import com.pedro.racasclasses.race.impl.DragonbornRace;
import com.pedro.racasclasses.race.impl.DwarfRace;
import com.pedro.racasclasses.race.impl.ElfRace;
import com.pedro.racasclasses.race.impl.GnomeRace;
import com.pedro.racasclasses.race.impl.HalfElfRace;
import com.pedro.racasclasses.race.impl.HalfOrcRace;
import com.pedro.racasclasses.race.impl.HalflingRace;
import com.pedro.racasclasses.race.impl.HumanRace;
import com.pedro.racasclasses.race.impl.TieflingRace;
import com.pedro.racasclasses.race.impl.AarakocraRace;
import com.pedro.racasclasses.race.impl.AasimarRace;
import com.pedro.racasclasses.race.impl.GoliathRace;
import com.pedro.racasclasses.race.impl.TabaxiRace;
import com.pedro.racasclasses.race.impl.TritonRace;
import com.pedro.racasclasses.race.impl.OrcRace;
import com.pedro.racasclasses.race.impl.GoblinRace;
import com.pedro.racasclasses.race.impl.HobgoblinRace;
import com.pedro.racasclasses.race.impl.LizardfolkRace;
import com.pedro.racasclasses.race.impl.MinotaurRace;
import com.pedro.racasclasses.race.impl.FirbolgRace;
import com.pedro.racasclasses.race.impl.KoboldRace;
import com.pedro.racasclasses.race.impl.LeoninRace;
import com.pedro.racasclasses.race.impl.SatyrRace;
import com.pedro.racasclasses.race.impl.TortleRace;
import com.pedro.racasclasses.race.impl.VerdanRace;
import com.pedro.racasclasses.race.impl.QuachoRace;
import com.pedro.racasclasses.race.impl.GithyankiRace;
import com.pedro.racasclasses.race.impl.KenkuRace;
import com.pedro.racasclasses.race.impl.HarengonRace;
import com.pedro.racasclasses.race.impl.YuanTiRace;
import com.pedro.racasclasses.race.impl.KenderRace;
import com.pedro.racasclasses.race.impl.KhenraRace;
import com.pedro.racasclasses.race.impl.WarforgedRace;
import com.pedro.racasclasses.race.impl.VampireRace;
import com.pedro.racasclasses.race.impl.ChangelingRace;
import com.pedro.racasclasses.race.impl.SirenRace;
import com.pedro.racasclasses.race.impl.HadozeeRace;
import com.pedro.racasclasses.race.impl.GiffRace;
import com.pedro.racasclasses.race.impl.ShifterRace;
import com.pedro.racasclasses.race.impl.VedalkenRace;
import com.pedro.racasclasses.race.impl.HexbloodRace;
import com.pedro.racasclasses.race.impl.RebornRace;
import com.pedro.racasclasses.race.impl.GenasiRace;
import com.pedro.racasclasses.race.impl.PlasmoidRace;
import com.pedro.racasclasses.race.impl.NagaRace;
import com.pedro.racasclasses.race.impl.KalAshtarRace;
import com.pedro.racasclasses.race.impl.SimicHybridRace;
import com.pedro.racasclasses.race.impl.LoxodonRace;
import com.pedro.racasclasses.race.impl.CentaurRace;
import com.pedro.racasclasses.race.impl.FungrilRace;
import com.pedro.racasclasses.race.impl.WukongRace;
import com.pedro.racasclasses.race.impl.DriderRace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RaceRegistry {

    private static final Map<String, Race> RACES = new HashMap<>();

    public static void init() {
        register(new HumanRace());
        register(new DwarfRace());
        register(new HalfElfRace());
        register(new HalfOrcRace());
        register(new DragonbornRace());
        register(new ElfRace());
        register(new GnomeRace());
        register(new HalflingRace());
        register(new TieflingRace());
        register(new AarakocraRace());
        register(new AasimarRace());
        register(new GoliathRace());
        register(new TabaxiRace());
        register(new TritonRace());
        register(new OrcRace());
        register(new GoblinRace());
        register(new HobgoblinRace());
        register(new LizardfolkRace());
        register(new MinotaurRace());
        register(new FirbolgRace());
        register(new KoboldRace());
        register(new LeoninRace());
        register(new SatyrRace());
        register(new TortleRace());
        register(new VerdanRace());
        register(new QuachoRace());
        register(new GithyankiRace());
        register(new KenkuRace());
        register(new HarengonRace());
        register(new YuanTiRace());
        register(new KenderRace());
        register(new KhenraRace());
        register(new WarforgedRace());
        register(new VampireRace());
        register(new ChangelingRace());
        register(new SirenRace());
        register(new HadozeeRace());
        register(new GiffRace());
        register(new ShifterRace());
        register(new VedalkenRace());
        register(new HexbloodRace());
        register(new RebornRace());
        register(new GenasiRace());
        register(new PlasmoidRace());
        register(new NagaRace());
        register(new KalAshtarRace());
        register(new SimicHybridRace());
        register(new LoxodonRace());
        register(new CentaurRace());
        register(new FungrilRace());
        register(new WukongRace());
        register(new DriderRace());
    }

    public static void register(Race race) {
        RACES.put(race.getId().toLowerCase(), race);
    }

    public static Race get(String id) {
        if (id == null) return null;
        return RACES.get(id.toLowerCase().replace(" ", "").replace("_", ""));
    }

    public static Collection<Race> getAll() {
        return RACES.values();
    }

    public static boolean exists(String id) {
        return get(id) != null;
    }
}
