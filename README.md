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

Totalmente **data-driven** e fácil de estender com novas raças.

---

## 🎮 Raças Implementadas (53)

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

### Raças Exóticas (5)
| Raça | ID | Especial |
|---|---|---|
| 🐍 **Naga** | `naga` | Constrict + veneno + aquático |
| 🧠 **Kalashtar** | `kalashtar` | Telepatia + resistência mágica |
| 🧬 **Simic Hybrid** | `simichybrid` | Mutação (3 sub-raças) |
| 🐘 **Loxodon** | `loxodon` | Tanque + Trunk Slam |
| 🐴 **Centaur** | `centaur` | Velocidade + Charge |

**Total: 53 raças | 4 raças com sub-raças | 67 combinações únicas**

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
| **`R`** | Super Jump (Aarakocra, Aasimar, Satyr, Genasi Ar) |
| **`H`** | Habilidade ativa (cada raça tem a sua) |

---

## 🌟 Destaques de Raças

### 🐉 Draconato
**10 ancestralidades elementais**, cada uma com sua baforada única:
- **Fogo** (Red, Gold, Brass) - cone de fogo
- **Gelo** (White, Silver) - cone de gelo + Slowness
- **Raio** (Blue, Bronze) - linha reta elétrica
- **Ácido** (Black, Copper) - cone de ácido
- **Poison** (Green) - nuvem tóxica

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

## 🏗️ Arquitetura

```
com.pedro.racasclasses/
├── RacasClasses.java              # Mod principal
├── capability/
│   ├── PlayerRaceData.java        # Dados do player
│   └── ModAttachments.java        # Attachment do NeoForge
├── race/
│   ├── Race.java                  # Interface comum
│   ├── RaceRegistry.java          # Registro de raças
│   └── impl/                      # 53 implementações
├── network/
│   ├── SuperJumpPayload.java      # Pacote R
│   ├── AbilityPayload.java        # Pacote H
│   └── ServerPayloadHandler.java  # Handler dos pacotes
├── client/
│   ├── ModKeyMappings.java        # Registra R e H
│   └── ClientEvents.java          # Detecta teclas
└── event/
    ├── RaceCommand.java           # Comando /raca
    ├── RaceEventHandler.java      # Atributos base
    ├── RaceTickHandler.java       # Tick global
    ├── RaceEventDispatcher.java   # Eventos
    └── BreathWeapon.java          # Baforada do Draconato
```

---

## 🗺️ Roadmap

- ✅ Sistema de raças com atributos dinâmicos
- ✅ Sub-raças (Draconato, Elfo, Gnomo, Halfling, Tiefling, Genasi, Shifter, Simic Hybrid)
- ✅ Baforada elemental do Draconato
- ✅ 53 raças implementadas
- ✅ Super Jump (R) e Habilidade Ativa (H)
- ✅ Sistema de transformação (Shifter, Vampire)
- ✅ Mecânicas aquáticas (Triton, Naga, Genasi Água)
- ✅ Mecânicas de tanque (Goliath, Loxodon, Warforged)
- ✅ Sistema de investida (Centaur, Minotaur)
- ☐ Sistema de classes (Guerreiro, Mago, Ladino)
- ☐ Sistema de XP e level up
- ☐ GUI de seleção de raça melhorada

---

## 📊 Estatísticas do Mod

| Categoria | Quantidade |
|---|---|
| **Raças totais** | 53 |
| **Raças com sub-raças** | 4 (Draconato, Elfo, Gnomo, Halfling, Tiefling excluindo - agora Genasi, Shifter, Simic Hybrid) |
| **Sub-raças totais** | 22 |
| **Habilidades ativas únicas** | 35+ |
| **Passivas únicas** | 80+ |
| **Linhas de código** | ~15,000 |

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
