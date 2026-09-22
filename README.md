# Raças e Classes

Um mod de **raças e classes** para Minecraft inspirado no sistema de D&D 5e. Escolha sua raça, ganhe atributos únicos, habilidades passivas e ativas — e monte sua build do jeito que quiser.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## ✨ Sobre

**Raças e Classes** traz o sistema de raças do Dungeons & Dragons para o Minecraft. Cada raça tem:

- **Atributos próprios** (vida, dano, velocidade, armadura, tamanho)
- **Habilidades passivas** (visão no escuro, resistências, bônus ambientais)
- **Habilidades ativas** (baforada, super pulo, cura, investida, etc)
- **Sub-raças** (ancestralidades, tipos elementais, mutações, etc)
- **Sistema de Atributos** (STR, DEX, CON, INT, WIS, LUCK)
- **Sistema de Fraquezas** (cada raça tem pelo menos 1)

Totalmente **data-driven** e fácil de estender com novas raças.

---

## 🎮 Raças Implementadas (56)

### Raças Base (9)
| Raça | ID | Especial |
|---|---|---|
| 🧑 **Humano** | `humano` | Base, sem bônus |
| 🐉 **Draconato** | `dragonborn` | Baforada elemental + resistência (10 sub-raças) |
| 🪓 **Anão** | `dwarf` | Minério esquentado + resistência poison |
| 🧝 **Elfo** | `elf` | Dano com arco (2 sub-raças) |
| 🧙 **Gnomo** | `gnome` | Invisibilidade (2 sub-raças) |
| 🧝‍♂️ **Meio-Elfo** | `halfelf` | Invisibilidade + XP + nado |
| 🪓 **Meio-Orc** | `halforc` | Crítico + esqueletos fogem |
| 🍀 **Halfling** | `halfling` | Lucky + Backstab (2 sub-raças) |
| 😈 **Tiefling** | `tiefling` | Resistência a fogo (3 sub-raças) |

### Raças Volo's / Mordenkainen's (17)
| Raça | ID | Especial |
|---|---|---|
| 🦅 **Aarakocra** | `aarakocra` | Elytra + Super Jump + garras |
| 🕊️ **Aasimar** | `aasimar` | Cura + elytra + resistências |
| 🏔️ **Goliath** | `goliath` | Tanque (28 HP, escala 1.5) |
| 🐱 **Tabaxi** | `tabaxi` | Velocidade + imune queda |
| 🌊 **Triton** | `triton` | Aquático + bônus submerso |
| 🪓 **Orc** | `orc` | Adrenaline Rush + regeneração |
| 👺 **Goblin** | `goblin` | Furtivo + Nimble Escape |
| ⚔️ **Hobgoblin** | `hobgoblin` | Marcial + Saving Face |
| 🦎 **Lizardfolk** | `lizardfolk` | Mordida + armadura + aquático |
| 🐂 **Minotaur** | `minotaur` | Investida + knockback |
| 🌳 **Firbolg** | `firbolg` | Natureza + doma 100% |
| 🦎 **Kobold** | `kobold` | Pequeno + Pack Tactics |
| 🦁 **Leonin** | `leonin` | Garras + rugido + carnívoro |
| 🎵 **Satyr** | `satyr` | Música + super pulo |
| 🐢 **Tortle** | `tortle` | Tanque + shell defense |
| 🟢 **Verdan** | `verdan` | Black Blood Healing |
| 🐸 **Grung** | `quacho` | Poison skin + imunidades |

### Raças Planescape / Ravnica / Eberron (13)
| Raça | ID | Especial |
|---|---|---|
| 🔮 **Githyanki** | `githyanki` | Psíquico + espada astral |
| 🐦 **Kenku** | `kenku` | Sem fala + cópia de sons |
| 🐰 **Harengon** | `harengon` | Super pulo + imune queda |
| 🐍 **Yuan-Ti** | `yuanti` | Poison spray + resistências |
| 🎲 **Kender** | `kender` | Lucky + furto + imune medo |
| 🐕 **Khenra** | `khenra` | Pack Tactics + corrida |
| 🤖 **Warforged** | `warforged` | Tanque + sem comida |
| 🧛 **Vampire** | `vampire` | Drenagem + fraqueza solar |
| 🎭 **Changeling** | `changeling` | Disfarce + cópia de nomes |
| 🧜 **Siren** | `siren` | Canto hipnótico + aquático |
| 🐒 **Hadozee** | `hadozee` | Planador + escalada |
| 🦛 **Giff** | `giff` | Tanque + super força |

### Raças Góticas / Místicas (5)
| Raça | ID | Especial |
|---|---|---|
| 🔄 **Shifter** | `shifter` | Transformação (3 sub-raças) |
| 🧠 **Vedalken** | `vedalken` | Arcane Insight + XP |
| 🧙‍♀️ **Hexblood** | `hexblood` | Hex marking + contra-ataque |
| 💀 **Reborn** | `reborn` | Resistências + cura emergencial |
| 🦠 **Plasmoid** | `plasmoid` | Amorfo + invisibilidade |

### Raças Elementais (1 com 4 sub-raças)
| Raça | ID | Sub-raças |
|---|---|---|
| 🌊 **Genasi** | `genasi` | ar, fogo, terra, agua |

### Raças Exóticas (8)
| Raça | ID | Especial |
|---|---|---|
| 🐍 **Naga** | `naga` | Constrict + veneno + aquático |
| 🧠 **Kalashtar** | `kalashtar` | Telepatia + resistência mágica |
| 🧬 **Simic Hybrid** | `simichybrid` | Mutação (3 sub-raças) |
| 🐘 **Loxodon** | `loxodon` | Tanque + Trunk Slam |
| 🐴 **Centaur** | `centaur` | Velocidade + Charge |
| 🐥 **Fungril** | `fungril` | Aura de veneno + esporos |
| 🐵 **Wukong** | `wukong` | Escalada + rugido + hidrofobia |
| 🕷️ **Drider** | `drider` | Teia + afinidade aracnídea |

**Total: 56 raças | 8 raças com sub-raças | 29 sub-raças | 85 combinações únicas**

---

## ⚠️ Sistema de Fraquezas

Cada raça possui fraquezas específicas que equilibram seus poderes. **Só o Humano não tem fraqueza.**

### Fraquezas por Elemento

| Elemento | Raças vulneráveis |
|---|---|
| **Fogo (+50% dano)** | Aasimar, Tortle, Drider, Fungril, Reborn, Triton, Siren, Firbolg, Harengon, Khenra, Wood Elf, Forest Gnome, Genasi Água, Levistus Tiefling |
| **Gelo (+50% dano)** | Plasmoid, Draconato Branco/Prateado, Asmodeus Tiefling |
| **Raio (+30% dano)** | Wukong, Draconato Negro/Cobre, Warforged |
| **Explosão (+30% dano)** | Goliath, Loxodon, Giff, Genasi Ar, Genasi Terra, Rock Gnome |
| **Magia (+30% dano)** | Vedalken, Kalashtar, Simic Hybrid, Tiefling, Hexblood, Vampire, Meio-Orc, Hobgoblin, Verdan, Githyanki, Changeling, Aasimar |
| **Perfurante (+30% dano)** | Minotaur, Centaur, Hadozee, Kenku (projéteis) |
| **Veneno (+25% dano)** | Stout Halfling |
| **Ferro (+30% dano)** | Satyr, Shifter |
| **Mortos-vivos (+30% dano)** | Yuan-Ti, Naga |

### Sistema de Elementos Opostos (Draconato)

| Sub-raça | Elemento oposto |
|---|---|
| Vermelho / Dourado / Latão | Poison |
| Branco / Prateado | Fogo |
| Azul / Bronze | Fogo |
| Negro / Cobre | Raio |
| Verde | Gelo |

### Fraquezas Ambientais

| Condição | Raças |
|---|---|
| **Sol** | Drow, Kobold, Vampire (dano + Weakness + Darkness) |
| **Água** | Anão (impedimento de nado), Tabaxi (1 dano/tick), Wukong (1 dano/tick), Genasi Fogo (na água e chuva) |
| **Queda (+50% dano)** | Aarakocra |

### Fraquezas de Dieta

| Tipo | Raças |
|---|---|
| **Carnívoro (só come carne)** | Leonin, Drider |
| **Fome rápida** | Meio-Elfo (+15%), Lightfoot Halfling (+25%), Kender (+25%), Leonin (+30%) |
| **Fome lenta** | Vampire (só drenagem), Tortle (+30% mais lenta), Drider (+20% mais lenta) |
| **Sem fome** | Warforged |

### Outras Fraquezas

| Fraqueza | Raças |
|---|---|
| **Não pode usar armadura** | Tortle |
| **Não pode trocar com villagers** | Kenku |
| **+30% dano de projéteis** | Kenku |
| **Elemento oposto** | Draconato |

---

## 🎯 Comandos

| Comando | Descrição |
|---|---|
| `/raca listar` | Lista todas as raças disponíveis |
| `/raca escolher <raça> [sub-raça]` | Escolhe sua raça |
| `/raca ver` | Mostra sua raça atual |
| `/raca reset` | Volta ao estado inicial |
| `/raca breath` | Usa a baforada (só Draconato) |

### Exemplos

```
/raca escolher humano
/raca escolher dragonborn red
/raca escolher elf wood
/raca escolher gnome rock
/raca escolher genasi fogo
/raca escolher shifter longtooth
/raca escolher simichybrid manta
```


### Teclas

| Tecla | Ação |
|---|---|
| **`P`** | Abre o menu de atributos (STR, DEX, CON, INT, WIS, LUCK) |
| **`R`** | Super Jump (Aarakocra, Aasimar, Satyr, Genasi Ar, Harengon, Githyanki) |
| **`H`** | Habilidade ativa (cada raça tem a sua) |

---

## 🌟 Destaques de Raças

### 🐉 Draconato
**10 ancestralidades elementais**, cada uma com sua baforada única:
- **Fogo** (Red, Gold, Brass) — cone de fogo
- **Gelo** (White, Silver) — cone de gelo + Slowness
- **Raio** (Blue, Bronze) — linha reta elétrica
- **Ácido** (Black, Copper) — cone de ácido
- **Poison** (Green) — nuvem tóxica

### 🌊 Genasi
**4 sub-raças elementais** com poderes únicos:
- **Ar**: Slow Falling + Super Jump + Gust
- **Fogo**: Imune fogo + Flame Burst
- **Terra**: Tanque (26 HP, armadura 4) + Earth Shield
- **Água**: Aquático + Water Whip

### 🔄 Shifter
**3 formas de transformação**:
- **Longtooth**: Mordida venenosa
- **Razorclaw**: Garras duplas
- **Wildhunt**: Rastreamento + sentidos aguçados

### 🧬 Simic Hybrid
**3 mutações biológicas**:
- **Manta**: Planador (Slow Falling)
- **Climber**: Escalada em paredes
- **Grappling**: Força extra (+2 dano)

### 🐴 Centaur
**Investida épica**: Speed III por 5s, ao colidir causa 4 de dano + knockback massivo

### 🐍 Naga
**Constrict**: Prende o alvo no lugar por 5s + veneno

### 🧛 Vampire
**Drenagem**: SHIFT + click em animal suga vida, recupera fome + HP

### 🤖 Warforged
**Construto resiliente**: Não precisa comer, regeneração quando parado

### 🧠 Kalashtar
**Telepatia**: Mostra players raio 20 ao agachar, resistência mágica

### 🐘 Loxodon
**Tanque elefante**: Tromba devastadora, resistência a explosão

### 🐥 Fungril
**Humanoide fúngico**: Aura de veneno, nuvem de esporos

### 🐵 Wukong
**Guerreiro macaco**: Escalada em paredes, rugido poderoso, hidrofobia

### 🕷️ Drider
**Meio-aracnídeo**: Teia aracnídea, afinidade com aranhas

---

## 🎯 Sistema de Atributos

O mod implementa o sistema de atributos do D&D 5e com 6 stats principais:

### Atributos Disponíveis

| Sigla | Nome | Efeito |
|---|---|---|
| **STR** | Força | +1% dano corpo a corpo por nível |
| **DEX** | Destreza | +1% dano à distância + 0.001 velocidade por nível |
| **CON** | Constituição | +0.25 ❤️ por nível |
| **INT** | Inteligência | +1% duração de efeitos por nível (futuro: mana) |
| **WIS** | Sabedoria | +1% cura por nível (futuro: mana) |
| **LUCK** | Sorte | +0.5% drop de minério por nível |

### Funcionamento

- **Nível máximo de personagem:** 120
- **Nível máximo por atributo:** 50
- **XP necessária:** `nível * 100`
- **Ganho de XP:** matar mob (+10), minerar (+5), trocar com villager (+3), pescar (+2), craftar (+1)
- **Bônus raciais iniciais:** cada raça concede pontos baseados no D&D
- **Pontos livres:** raças sem bônus definido ganham 3 pontos
- **Interface:** pressione **P** para abrir o menu

### Bônus Raciais Iniciais

| Raça | Bônus |
|---|---|
| Humano | +1 em todos |
| Anão | +2 CON |
| Elfo | +2 DEX |
| Halfling | +2 DEX |
| Draconato | +2 STR, +1 LUCK |
| Gnomo | +2 INT |
| Meio-Elfo | +2 LUCK + 2 pontos livres |
| Meio-Orc | +2 STR, +1 CON |
| Tiefling | +1 INT, +2 LUCK |
| **Outras raças** | 3 pontos livres |

---


## 🛠️ Como Instalar

### Para jogar

1. Instale o **NeoForge 21.1.x** para Minecraft **1.21.1**
2. Baixe o `.jar` do mod em Releases
3. Coloque em `.minecraft/mods/`
4. Inicie o Minecraft com o perfil do NeoForge

### Para desenvolver

```bash
git clone https://github.com/SEU-USUARIO/racas-e-classes.git
cd racas-e-classes
./gradlew runClient   # Linux/Mac
gradlew.bat runClient # Windows
```

**Requisitos:**
- Java 21
- IntelliJ IDEA (recomendado)

---

## 🗺️ Roadmap

- ✅ Sistema de raças com atributos dinâmicos
- ✅ Sub-raças (Draconato, Elfo, Gnomo, Halfling, Tiefling, Genasi, Shifter, Simic Hybrid)
- ✅ Baforada elemental do Draconato
- ✅ 56 raças implementadas
- ✅ Super Jump (R) e Habilidade Ativa (H)
- ✅ Sistema de transformação (Shifter, Vampire)
- ✅ Mecânicas aquáticas (Triton, Naga, Genasi Água)
- ✅ Mecânicas de tanque (Goliath, Loxodon, Warforged)
- ✅ Sistema de investida (Centaur, Minotaur)
- ☐ Sistema de classes (Guerreiro, Mago, Ladino)
- ☐ Sistema de XP e level up
- ☐ GUI de seleção de raça melhorada

---

📊 Estatísticas do Mod

| Categoria | Quantidade |
|---|---|
| Raças totais | 56 |
| Raças com sub-raças | 8 (Draconato, Elfo, Gnomo, Halfling, Tiefling, Genasi, Shifter, Simic Hybrid) |
| Sub-raças totais | 29 (10+2+2+2+3+4+3+3) |
| Combinações únicas | 85 (56 raças + 29 sub-raças) |
| Habilidades ativas (H) | 43+ |
| Super Jumps (R) | 6 |
| Atributos | 6 (STR, DEX, CON, INT, WIS, LUCK) |
| Níveis de personagem | 120 |
| Níveis por atributo | 50 |
| Raças aquáticas | 8 |
| Raças tanque (HP ≥ 26) | 6 |
| Raças pequenas (escala < 0.70) | 6 |
| Raças grandes (escala > 1.20) | 5 |
| Linhas de código | ~18.000 |


---

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se livre para:
- Reportar bugs (issues)
- Sugerir raças ou mecânicas
- Enviar pull requests

---

## 📜 Licença

Este projeto está sob a licença **MIT**. Veja LICENSE para mais detalhes.

---

## 🙏 Créditos

- Inspirado no sistema de raças do **Dungeons & Dragons 5e**
- Feito com **NeoForge** para Minecraft **1.21.1**
- Raças adicionais de: Volo's Guide, Mordenkainen's, Eberron, Ravnica, Plane Shift

---

> **"Walking in two worlds but truly belonging to neither."** — Half-Elf, Player's Handbook
