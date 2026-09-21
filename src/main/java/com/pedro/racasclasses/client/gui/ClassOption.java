package com.pedro.racasclasses.client.gui;

import java.util.List;

public record ClassOption(
        String id,
        String name,
        String summary,
        List<String> abilities,
        List<SubclassOption> subclasses
) {
    public static final List<ClassOption> ALL = List.of(
            new ClassOption(
                    "rogue",
                    "Ladino",
                    "Especialista em astúcia, posicionamento e truques que confundem seus inimigos.",
                    List.of("Acesso a técnicas de subclasse", "Combate baseado em oportunidade e mobilidade"),
                    List.of(new SubclassOption(
                            "illusionist",
                            "Ilusionista",
                            "Um ladino arcano que luta cercado por imagens falsas e lâminas espectrais.",
                            List.of(
                                    "C: cria três cópias móveis por 15 segundos; qualquer dano desfaz uma cópia",
                                    "Z: invoca três adagas espectrais que permanecem até serem usadas",
                                    "As adagas atacam em sequência, com 1 segundo entre os golpes",
                                    "Recargas: 90s para cópias e 120s para adagas"
                            )))
            )
    );

    public record SubclassOption(String id, String name, String summary, List<String> abilities) {}
}
