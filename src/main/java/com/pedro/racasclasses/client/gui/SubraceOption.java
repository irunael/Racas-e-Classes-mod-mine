package com.pedro.racasclasses.client.gui;

import java.util.List;
import java.util.Map;

/** Descrições dos efeitos implementados nas raças, incluindo os traços raciais herdados. */
public record SubraceOption(
        String id,
        String name,
        String summary,
        String attributes,
        List<String> buffs,
        List<String> debuffs,
        List<String> powers
) {
    // Conferir estes perfis ao alterar *Race, RaceEventDispatcher ou BreathWeapon.
    private static final Map<String, List<SubraceOption>> BY_RACE = Map.of(
            "dragonborn", List.of(
                    dragonborn("red", "Vermelho", "Ancestralidade de fogo, agressiva e resistente às próprias chamas."),
                    dragonborn("gold", "Dourado", "Herança de fogo nobre, marcada por disciplina e imponência."),
                    dragonborn("brass", "Latão", "Linhagem de fogo curiosa e expansiva."),
                    dragonborn("white", "Branco", "Ancestralidade gélida, moldada por ambientes extremos."),
                    dragonborn("silver", "Prateado", "Herança de gelo protetora e de espírito elevado."),
                    dragonborn("blue", "Azul", "Ancestralidade elétrica, paciente e calculista."),
                    dragonborn("bronze", "Bronze", "Herança elétrica ligada ao mar e à justiça."),
                    dragonborn("black", "Negro", "Ancestralidade ácida, obstinada e ligada aos pântanos."),
                    dragonborn("copper", "Cobre", "Herança ácida engenhosa e irreverente."),
                    dragonborn("green", "Verde", "Ancestralidade venenosa, persuasiva e estratégica.")),
            "elf", List.of(
                    new SubraceOption("wood", "Elfo da Floresta",
                            "A vegetação fortalece esta linhagem. Longe das folhas, a escuridão se torna uma fraqueza.",
                            "20 HP • velocidade e tamanho normais",
                            List.of("Visão noturna, herdada dos elfos.",
                                    "+25% de dano com flechas, herdado dos elfos.",
                                    "Perto de folhagem: Regeneração I, Velocidade II e Força I; remove Fraqueza e Escuridão.",
                                    "A afinidade exige pelo menos 8 folhas detectadas na área próxima: até 16 blocos na horizontal, 8 abaixo e 16 acima."),
                            List.of("Sem folhagem suficiente e com luz abaixo de 7: Fraqueza I e Escuridão. São removidas ao voltar à luz ou à folhagem."),
                            List.of()),
                    new SubraceOption("drow", "Drow",
                            "Especialista nas profundezas: combate, furtividade e percepção melhoram na escuridão, mas o sol o enfraquece.",
                            "20 HP • velocidade e tamanho normais",
                            List.of("Visão noturna, herdada dos elfos.",
                                    "+25% de dano com flechas, herdado dos elfos.",
                                    "Com luz abaixo de 7: Velocidade II, Força II e Resistência I, renovadas enquanto permanecer no escuro."),
                            List.of("Exposto ao sol durante o dia: Fraqueza I e Escuridão. Após sair do sol, podem persistir por até 2s e 10s, respectivamente."),
                            List.of("Manto das sombras (passivo): agachar com luz abaixo de 7 concede invisibilidade, renovada enquanto a condição durar; pode persistir por até 2s ao sair.",
                                    "Percepção sombria (passiva): no escuro, destaca monstros em uma área de 16 blocos ao redor com Brilho por 3s, renovado enquanto estiverem próximos."))),
            "gnome", List.of(
                    new SubraceOption("forest", "Gnomo da Floresta",
                            "Sua afinidade com criaturas naturais impede que certos animais e aranhas o escolham como alvo.",
                            "20 HP • 60% do tamanho normal",
                            List.of("Visão noturna, herdada dos gnomos.",
                                    "Animais, incluindo lobos, e aranhas não escolhem você como alvo."),
                            List.of("Velocidade base 10% menor, herdada dos gnomos."),
                            List.of()),
                    new SubraceOption("rock", "Gnomo da Rocha",
                            "Explorador compacto que amortece quedas ao percorrer terrenos acidentados.",
                            "20 HP • 60% do tamanho normal",
                            List.of("Visão noturna, herdada dos gnomos.",
                                    "Reduz em 3 blocos a distância usada no cálculo de dano de queda. Quedas de até 3 blocos são anuladas."),
                            List.of("Velocidade base 10% menor, herdada dos gnomos."),
                            List.of())),
            "halfling", List.of(
                    new SubraceOption("lightfoot", "Pé Leve",
                            "Linhagem furtiva que combina a sorte dos halflings com invisibilidade e ataques pelas costas.",
                            "20 HP • 55% do tamanho normal",
                            List.of("Sorte racial: 10% de chance de gerar uma leva extra de drops ao quebrar blocos.",
                                    "Bravura racial: remove Fraqueza e Lentidão a cada 1s."),
                            List.of("Velocidade base 10% menor, herdada dos halflings."),
                            List.of("Furtividade (passiva): agachar concede invisibilidade, renovada enquanto estiver agachado; pode persistir por até 2s ao levantar.",
                                    "Ataque pelas costas (passivo): +20% de dano ao atingir um alvo estando atrás dele.")),
                    new SubraceOption("stout", "Robusto",
                            "Linhagem vigorosa que alia a sorte dos halflings a mais vida e proteção contra ataques físicos.",
                            "26 HP (13 corações) • 55% do tamanho normal",
                            List.of("+6 HP (3 corações) de vida máxima em relação ao halfling base; já incluídos nos atributos acima.",
                                    "20% de redução no dano de ataques de jogadores e criaturas, flechas e tridentes.",
                                    "Sorte racial: 10% de chance de gerar uma leva extra de drops ao quebrar blocos.",
                                    "Bravura racial: remove Fraqueza e Lentidão a cada 1s."),
                            List.of("Velocidade base 10% menor, herdada dos halflings."),
                            List.of())),
            "tiefling", List.of(
                    new SubraceOption("asmodeus", "Asmodeus",
                            "O legado de Asmodeus protege contra chamas e transforma ataques desarmados em golpes incendiários.",
                            "20 HP • velocidade e tamanho normais",
                            List.of("Visão noturna, herdada dos tieflings.",
                                    "Imunidade a fogo, queimadura, lava e blocos quentes. Substitui a resistência racial de 75% nesses danos."),
                            List.of(),
                            List.of("Toque infernal (passivo): ataques com a mão principal vazia incendeiam o alvo por 2s, sem recarga própria.")),
                    new SubraceOption("levistus", "Levistus",
                            "O legado de Levistus impede que a lentidão o prenda e permite desacelerar inimigos com ataques desarmados.",
                            "20 HP • velocidade e tamanho normais",
                            List.of("Visão noturna, herdada dos tieflings.",
                                    "75% de redução no dano de fogo, queimadura, lava e blocos quentes, herdada dos tieflings.",
                                    "Remove Lentidão a cada 1s."),
                            List.of(),
                            List.of("Toque gélido (passivo): ataques com a mão principal vazia aplicam Lentidão I por 3s, sem recarga própria.")),
                    new SubraceOption("zariel", "Zariel",
                            "O legado guerreiro de Zariel reduz o dano de golpes próximos e enfraquece inimigos com ataques desarmados.",
                            "20 HP • velocidade e tamanho normais",
                            List.of("Visão noturna, herdada dos tieflings.",
                                    "75% de redução no dano de fogo, queimadura, lava e blocos quentes, herdada dos tieflings.",
                                    "50% de redução no dano de ataques corpo a corpo de jogadores e criaturas; não inclui flechas nem tridentes arremessados."),
                            List.of(),
                            List.of("Toque debilitante (passivo): ataques com a mão principal vazia aplicam Fraqueza I por 3s, sem recarga própria.")))
    );

    public static List<SubraceOption> forRace(String raceId) {
        return BY_RACE.getOrDefault(raceId, List.of());
    }

    private static SubraceOption dragonborn(String id, String name, String summary) {
        String resistance;
        List<String> powers;
        // As cores de um mesmo elemento compartilham os mesmos efeitos no jogo.
        switch (id) {
            case "red", "gold", "brass" -> {
                resistance = "75% de redução no dano de fogo, queimadura, lava e blocos quentes.";
                powers = List.of("Baforada de fogo: /raca breath. Cone de 6 blocos por 5s; recarga de 30s após terminar.",
                        "Causa 3 pontos de dano por pulso, em intervalos de 1s, e incendeia os alvos por 3s a cada acerto.");
            }
            case "white", "silver" -> {
                resistance = "75% de redução no dano de congelamento.";
                powers = List.of("Baforada de gelo: /raca breath. Cone de 5 blocos por 5s; recarga de 30s após terminar.",
                        "Causa 4 pontos de dano por pulso, em intervalos de 1s, aplica Lentidão II por 10s e acumula congelamento nos alvos.");
            }
            case "blue", "bronze" -> {
                resistance = "75% de redução no dano de raios.";
                powers = List.of("Baforada elétrica: /raca breath. Disparo instantâneo em linha, com alcance de 20 blocos e raio de 1 bloco.",
                        "Causa 10 pontos de dano por alvo atingido. Recarga de 45s.");
            }
            case "black", "copper" -> {
                resistance = "Imunidade ao dano mágico direto, incluindo o dano das baforadas de ácido e veneno; não abrange todas as fontes de magia.";
                powers = List.of("Baforada ácida: /raca breath. Cone de 6 blocos por 5s; recarga de 30s após terminar.",
                        "Causa 2 pontos de dano mágico por pulso, em intervalos de 1s, e aplica Veneno I por 5s a cada acerto.");
            }
            case "green" -> {
                resistance = "Enquanto estiver com Veneno, anula o dano mágico direto, incluindo o dano do próprio veneno. O efeito de Veneno não é removido.";
                powers = List.of("Baforada venenosa: /raca breath. Cone de 4 blocos por 5s; recarga de 30s após terminar.",
                        "Causa 2 pontos de dano mágico por pulso, em intervalos de 1s, e aplica Veneno I por 5s a cada acerto.");
            }
            default -> throw new IllegalArgumentException("Linhagem dracônica desconhecida: " + id);
        }
        return new SubraceOption(id, name, summary,
                "22 HP • +0,2 dano base • +2 armadura • velocidade e tamanho normais",
                List.of("Visão noturna, herdada dos draconatos.", resistance), List.of(), powers);
    }
}
