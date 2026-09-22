# Raças Implementadas - Guia Completo

Lista completa de todas as 53 raças do mod, com sub-raças, habilidades, atributos e comandos.

**Última atualização:** 20/09/2026  
**Total de raças:** 53  
**Total de sub-raças:** 21  
**Combinações únicas:** 66

---

## 📖 Índice

1. [Raças Base (9)](#raças-base)
2. [Raças Volo's / Mordenkainen's (17)](#raças-volos--mordenkainens)
3. [Raças Planescape / Ravnica / Eberron (13)](#raças-planescape--ravnica--eberron)
4. [Raças Góticas / Místicas (5)](#raças-góticas--místicas)
5. [Raças Elementais (1 com 4 sub-raças)](#raças-elementais)
6. [Raças Exóticas (5)](#raças-exóticas)
7. [Como Usar](#como-usar)
8. [Estatísticas](#estatísticas)

---

## Raças Base

### 🧑 Humano
- **ID:** `humano`
- **Comando:** `/raca escolher humano`
- **Atributos:** HP 20 | Velocidade 0.10 | Escala 1.00 | Dano 1.0
- **Habilidades:** Base (sem bônus especiais)
- **Fraquezas:** Nenhuma

---

### 🐉 Draconato
- **ID:** `dragonborn`
- **Comando:** `/raca escolher dragonborn <ancestralidade>`
- **Sub-raças (10):** red, gold, brass, white, silver, blue, bronze, black, copper, green
- **Atributos:** HP 22 | Dano 1.2 | Armadura 2.0 | Escala 1.00 | Night Vision
- **Habilidades:**
  - Baforada elemental (/raca breath, cd 30s)
  - 75% resistência elemental ou imunidade (poison/acid)
- **Fraquezas:** Elemento oposto: Fogo ↔ Poison, Gelo ↔ Fogo, Raio ↔ Fogo, Ácido ↔ Raio (+50% dano)

**Ancestralidades:**

| Cor | Elemento | Tipo | Resistência |
|-----|----------|------|-------------|
| 🔴 Red | Fogo | Cone | 75% |
| 🟡 Gold | Fogo | Cone | 75% |
| 🟠 Brass | Fogo | Cone | 75% |
| ⚪ White | Gelo | Cone | 75% |
| ⚪ Silver | Gelo | Cone | 75% |
| 🔵 Blue | Raio | Linha | 75% |
| 🔵 Bronze | Raio | Linha | 75% |
| ⚫ Black | Ácido | Cone | Imunidade |
| 🟤 Copper | Ácido | Cone | Imunidade |
| 🟢 Green | Poison | Nuvem | Imunidade |

---

### 🪓 Anão
- **ID:** `dwarf`
- **Comando:** `/raca escolher dwarf`
- **Atributos:** HP 24 | Velocidade 0.09 | Escala 0.60 | Night Vision
- **Habilidades:**
  - +20% velocidade de mineração
  - 50% resistência a poison
  - 3% chance de minério esquentado (2x drop)
- **Fraquezas:** Impedimento na água: não consegue nadar (aplica Slowness IV)

---

### 🧝 Elfo
- **ID:** `elf`
- **Comando:** `/raca escolher elf <sub-raça>`
- **Sub-raças (2):** wood, drow
- **Atributos Base:** HP 20 | Night Vision
- **Habilidade Base:** +25% dano com arco/besta
- **Fraquezas:** Wood Elf: +50% dano de fogo e fraqueza sem folhagem | Drow: +50% dano no sol + Weakness + Darkness

**Sub-raças:**

| Sub-raça | Especial |
|----------|----------|
| 🌲 **Wood Elf** | Escala 1.10, HP 19, vel 0.11. Em florestas: Regeneration + Speed II + Strength |
| 🕷️ **Drow** | +1.5 armadura. Escuro: Speed II + Strength II + Resistance. Sol: Weakness + Darkness |

---

### 🧙 Gnomo
- **ID:** `gnome`
- **Comando:** `/raca escolher gnome <sub-raça>`
- **Sub-raças (2):** forest, rock
- **Atributos Base:** HP 20 | Velocidade 0.09 | Escala 0.60 | Night Vision
- **Fraquezas:** Forest Gnome: +50% dano de fogo | Rock Gnome: +30% dano de explosão e impedimento na água

**Sub-raças:**

| Sub-raça | Especial |
|----------|----------|
| 🌲 **Forest Gnome** | Invisibilidade ao agachar, mobs pacíficos não atacam, Speed perto de folhagem |
| 🪨 **Rock Gnome** | +2 HP, imunidade queda (3 blocos), XP extra em minérios |

---

### 🧝‍♂️ Meio-Elfo
- **ID:** `halfelf`
- **Comando:** `/raca escolher halfelf`
- **Atributos:** HP 22 | Night Vision
- **Habilidades:**
  - Invisibilidade em florestas
  - +10% XP
  - +15% velocidade de nado
- **Fraquezas:** Fome 15% mais rápida

---

### 🪓 Meio-Orc
- **ID:** `halforc`
- **Comando:** `/raca escolher halforc`
- **Atributos:** HP 24 | Dano 1.2 | Escala 1.10 | Night Vision
- **Habilidades:**
  - +20% dano crítico
  - Esqueletos fogem (Menacing)
- **Fraquezas:** +30% dano de magia

---

### 🍀 Halfling
- **ID:** `halfling`
- **Comando:** `/raca escolher halfling <sub-raça>`
- **Sub-raças (2):** lightfoot, stout
- **Atributos Base:** HP 20 | Velocidade 0.09 | Escala 0.55
- **Habilidades Base:**
  - Lucky (10% drop extra)
  - Brave (imune a Weakness/Slowness)
- **Fraquezas:** Lightfoot: Fome 25% mais rápida | Stout: +25% dano de veneno e impedimento na água

**Sub-raças:**

| Sub-raça | Especial |
|----------|----------|
| 🌲 **Lightfoot** | Invisibilidade ao agachar, Backstab (+20% pelas costas) |
| 🪨 **Stout** | +6 HP, 20% resistência a dano físico |

---

### 😈 Tiefling
- **ID:** `tiefling`
- **Comando:** `/raca escolher tiefling <sub-raça>`
- **Sub-raças (3):** asmodeus, levistus, zariel
- **Atributos Base:** HP 20 | Night Vision
- **Habilidade Base:** 75% resistência a fogo
- **Fraquezas:** +30% dano sagrado (MAGIC) | Asmodeus: fraqueza a gelo | Levistus: fraqueza a fogo | Zariel: fraqueza a magia

**Sub-raças:**

| Sub-raça | Especial |
|----------|----------|
| 🔥 **Asmodeus** | Imunidade a fogo, ataque mão vazia aplica fogo |
| ❄️ **Levistus** | Imune a Slowness, ataque mão vazia aplica lentidão |
| ⚔️ **Zariel** | 50% resistência física, ataque mão vazia aplica fraqueza |

---

## Raças Volo's / Mordenkainen's

### 🦅 Aarakocra
- **ID:** `aarakocra`
- **Comando:** `/raca escolher aarakocra`
- **Atributos:** HP 20 | Escala 1.00
- **Habilidades:**
  - Talons (+3 dano desarmado)
  - Elytra permanente (Curse of Binding)
  - **Super Jump (R):** pula 5 blocos, cd 10s
- **Fraquezas:** +50% dano de queda | Não pode usar peitoral no lugar da elytra

---

### 🕊️ Aasimar
- **ID:** `aasimar`
- **Comando:** `/raca escolher aasimar`
- **Atributos:** HP 24 | Escala 1.00 | Night Vision
- **Habilidades:**
  - +20% dano corpo a corpo
  - 50% resistência a Wither/Magia
  - Elytra permanente
  - **Healing Hands (H):** cura 4 ❤️, cd 60s
  - **Super Jump (R):** pula 5 blocos
- **Fraquezas:** +50% dano de fogo (elemento oposto ao celestial) | +20% dano de armas

---

### 🏔️ Goliath
- **ID:** `goliath`
- **Comando:** `/raca escolher goliath`
- **Atributos:** HP 28 | Dano 1.3 | Velocidade 0.07 | Escala 1.50
- **Habilidades:**
  - Resistência a knockback
  - **Stone's Endurance (H):** -75% dano por 10s, cd 60s
  - Redução de dano de queda: -5 blocos
- **Fraquezas:** +30% dano de explosão (vulnerabilidade devido ao tamanho)

---

### 🐱 Tabaxi
- **ID:** `tabaxi`
- **Comando:** `/raca escolher tabaxi`
- **Atributos:** HP 20 | Velocidade 0.12 | Escala 1.00 | Night Vision
- **Habilidades:**
  - +2 dano desarmado (garras)
  - **Feline Agility (H):** Speed III por 5s, cd 30s
  - Imunidade total a queda
  - Coelhos e galinhas fogem
- **Fraquezas:** Sofre 1 dano/tick na água (hidrofobia)

---

### 🌊 Triton
- **ID:** `triton`
- **Comando:** `/raca escolher triton`
- **Atributos:** HP 22 | Escala 1.00
- **Habilidades:**
  - Respira na água infinitamente
  - **Submerso:** Night Vision + Dolphin's Grace + Haste III + Strength II
  - +20% dano com tridente
  - 50% resistência a freeze
- **Fraquezas:** +50% dano de fogo (elemento oposto à água)

---

### 🪓 Orc
- **ID:** `orc`
- **Comando:** `/raca escolher orc`
- **Atributos:** HP 24 | Escala 1.30 | Night Vision
- **Habilidades:**
  - **Adrenaline Rush (H):** Speed I + Strength II + cura 3 ❤️, 10s, cd 45s
  - +10% dano corpo a corpo em mobs
  - Zumbis e esqueletos não atacam
  - -15% dano recebido corpo a corpo
- **Fraquezas:** +50% dano de fogo, raio e magia (elementos opostos à resistência física)

---

### 👺 Goblin
- **ID:** `goblin`
- **Comando:** `/raca escolher goblin`
- **Atributos:** HP 18 | Velocidade 0.12 | Escala 0.65 | Night Vision
- **Habilidades:**
  - **Nimble Escape (H):** Invisibilidade 10s, cd 30s
  - +velocidade ao agachar
  - -30% dano mágico
- **Fraquezas:** +20% dano de armas físicas

---

### ⚔️ Hobgoblin
- **ID:** `hobgoblin`
- **Comando:** `/raca escolher hobgoblin`
- **Atributos:** HP 22 | Dano 1.1 | Escala 1.00 | Night Vision
- **Habilidades:**
  - +30% dano com espadas e machados
  - **Saving Face (H):** Strength I por 10s, cd 30s
- **Fraquezas:** +30% dano de magia

---

### 🦎 Lizardfolk
- **ID:** `lizardfolk`
- **Comando:** `/raca escolher lizardfolk`
- **Atributos:** HP 22 | Armadura +4 | Escala 1.00
- **Habilidades:**
  - +3 dano desarmado (mordida)
  - Respiração aquática
  - +50% nado
  - Ao matar mob: cura 1 ❤️
- **Fraquezas:** +75% dano de lava (elemento oposto à água)

---

### 🐂 Minotaur
- **ID:** `minotaur`
- **Comando:** `/raca escolher minotaur`
- **Atributos:** HP 24 | Dano 1.2 | Escala 1.20
- **Habilidades:**
  - Ataque empurra mais (knockback)
  - +30% dano se correndo
  - -15% dano recebido
  - **Goring Rush (H):** Speed II por 5s, cd 30s
- **Fraquezas:** +30% dano perfurante (vulnerabilidade devido ao tamanho)

---

### 🌳 Firbolg
- **ID:** `firbolg`
- **Comando:** `/raca escolher firbolg`
- **Atributos:** HP 22 | Escala 1.20 | Night Vision
- **Habilidades:**
  - Speed I + Regeneration I perto de folhagem
  - **Hidden Step (H):** Invisibilidade 5s, cd 30s
  - **Animal Whisperer:** doma qualquer animal com mão vazia (100%)
- **Fraquezas:** +50% dano de fogo (elemento oposto à natureza)

---

### 🦎 Kobold
- **ID:** `kobold`
- **Comando:** `/raca escolher kobold`
- **Atributos:** HP 16 | Velocidade 0.12 | Escala 0.50 | Night Vision
- **Habilidades:**
  - **Draconic Cry (H):** Strength I (5s) + Regeneration I (15s), cd 30s
  - Pack Tactics (+10% dano com aliado perto)
- **Fraquezas:** Sunlight Sensitivity (Weakness no sol, +30% dano recebido)

---

### 🦁 Leonin
- **ID:** `leonin`
- **Comando:** `/raca escolher leonin`
- **Atributos:** HP 22 | Dano 1.1 | Velocidade 0.11 | Escala 1.10 | Night Vision
- **Habilidades:**
  - Garras (+3 dano desarmado)
  - **Daunting Roar (H):** Strength III por 10s, cd 30s
  - +20% dano em mobs
- **Fraquezas:** Carnívoro (só come carne), fome 30% mais rápida

---

### 🎵 Satyr
- **ID:** `satyr`
- **Comando:** `/raca escolher satyr`
- **Atributos:** HP 20 | Velocidade 0.11 | Escala 0.85
- **Habilidades:**
  - Speed I permanente
  - -30% dano mágico
  - **Super Jump (R):** pula 5 blocos
  - Regeneration I perto de jukebox tocando
  - Resistência a queda (5 blocos)
  - **Instrumento Mágico (H):** Nausea + Weakness raio 6, 10s, cd 45s
- **Fraquezas:** +30% dano de armas de ferro (metal ferroeiro)

---

### 🐢 Tortle
- **ID:** `tortle`
- **Comando:** `/raca escolher tortle`
- **Atributos:** HP 26 | Velocidade 0.09 | Escala 1.10
- **Habilidades:**
  - **60% redução de dano** (natural)
  - **Bloqueia armadura em TODOS os slots**
  - **Shell Defense (H):** -50% dano por 10s, cd 30s
  - Respira na água infinitamente
- **Fraquezas:** +30% dano de fogo | Velocidade reduzida (0.06) | Não pode equipar armaduras

---

### 🟢 Verdan
- **ID:** `verdan`
- **Comando:** `/raca escolher verdan`
- **Atributos:** HP 22 | Escala 1.00
- **Habilidades:**
  - **Black Blood Healing:** ao tomar dano → cura 1 ❤️ (cd 30s)
  - -20% dano mágico
  - Regeneração lenta quando ≤3 ❤️
- **Fraquezas:** +30% dano de magia

---

### 🐸 Grung (Quacho)
- **ID:** `quacho`
- **Comando:** `/raca escolher quacho`
- **Atributos:** HP 18 | Velocidade 0.11 | Escala 0.65
- **Habilidades:**
  - Respira na água infinitamente
  - Imunidade a poison
  - Poisonous Skin: quem te bate toma poison
  - -10% dano recebido (geral)
- **Fraquezas:** +30% dano de fogo e lava (elemento oposto à água)

---

## Raças Planescape / Ravnica / Eberron

### 🔮 Githyanki
- **ID:** `githyanki`
- **Comando:** `/raca escolher githyanki`
- **Atributos:** HP 22 | Dano 1.1 | Velocidade 0.10 | Night Vision
- **Habilidades:**
  - **Astral Blade (H):** Espada espectral (Sharp V, Unbreaking ∞) por 30s, cd 60s
  - **Teleport (R):** Teleporte 15 blocos, cd 30s
  - -20% dano mágico
- **Fraquezas:** +30% dano mágico

---

### 🐦 Kenku
- **ID:** `kenku`
- **Comando:** `/raca escolher kenku`
- **Atributos:** HP 18 | Velocidade 0.11 | Escala 0.85
- **Habilidades:**
  - **Mimicry:** Copia sons ao matar mobs
  - **Expert Forgery:** +50% XP de XP orbs
  - **Não pode negociar com villagers** (sem fala)
- **Fraquezas:** Villagers recusam trocas | +30% dano de projéteis

---

### 🐰 Harengon
- **ID:** `harengon`
- **Comando:** `/raca escolher harengon`
- **Atributos:** HP 20 | Velocidade 0.13 | Escala 0.90
- **Habilidades:**
  - **Rabbit Hop (R):** Super Jump, cd 5s
  - **Lucky Footwork:** Imunidade total a queda
  - **Hare-Trigger:** Speed I + Jump Boost I permanentes
  - Mobs caçam coelhos agressivamente
- **Fraquezas:** +50% dano de fogo (elemento oposto à agilidade)

---

### 🐍 Yuan-Ti
- **ID:** `yuanti`
- **Comando:** `/raca escolher yuanti`
- **Atributos:** HP 22 | Dano 1.0 | Velocidade 0.10 | Night Vision
- **Habilidades:**
  - **Poison Spray (H):** Projétil venenoso 15 blocos, cd 30s
  - Imunidade total a poison
  - +50% nado
  - Ataque mão vazia aplica Poison I
- **Fraquezas:** +30% dano de mobs mortos-vivos

---

### 🎲 Kender
- **ID:** `kender`
- **Comando:** `/raca escolher kender`
- **Atributos:** HP 18 | Velocidade 0.11 | Escala 0.70
- **Habilidades:**
  - **Lucky:** 15% drop extra de mobs
  - **Taunt (H):** Provoca mobs raio 8, duram 10s, cd 40s
  - **Fearless:** Imune a medo e Darkness
  - **Pickpocket:** Chance de roubar item ao matar
- **Fraquezas:** Fome 25% mais rápida

---

### 🐕 Khenra
- **ID:** `khenra`
- **Comando:** `/raca escolher khenra`
- **Atributos:** HP 20 | Dano 1.1 | Velocidade 0.11 | Escala 1.00 | Night Vision
- **Habilidades:**
  - **Pack Tactics:** +15% dano com aliado perto
  - **Desert Runner:** Speed I em desertos
  - **Momentum Rush (H):** Speed III por 5s, cd 30s
- **Fraquezas:** +50% dano de fogo (elemento oposto à agilidade)

---

### 🤖 Warforged
- **ID:** `warforged`
- **Comando:** `/raca escolher warforged`
- **Atributos:** HP 26 | Dano 1.1 | Velocidade 0.09 | Armadura 3.0 | Escala 1.10
- **Habilidades:**
  - **Não precisa comer** (fome não diminui)
  - **Sentry's Rest:** Regeneration I quando parado
  - **Integrated Protection (H):** Resistance II por 10s, cd 60s
  - Imunidade a poison e disease
- **Fraquezas:** +30% dano de raio

---

### 🧛 Vampire
- **ID:** `vampire`
- **Comando:** `/raca escolher vampire`
- **Atributos:** HP 22 | Dano 1.2 | Velocidade 0.10 | Night Vision
- **Habilidades:**
  - **Drenagem (SHIFT + click em animal):** Suga vida, recupera fome + HP
  - **Blood Drain (H):** +3 corações, Wither II no alvo, cd 40s
  - Ataque desarmado: +2 dano
  - Fome 20% mais lenta
- **Fraquezas:**
  - **Sol:** Fire Damage + Nausea 99 (máximo)
  - **Não pode comer nada** (só drenagem)

---

### 🎭 Changeling
- **ID:** `changeling`
- **Comando:** `/raca escolher changeling`
- **Atributos:** HP 20 | Velocidade 0.10
- **Habilidades:**
  - **Shapechanger (H):** Copia nome de player por 60s, cd 120s
  - +15% velocidade
  - Mobs neutros são mais amigáveis
- **Fraquezas:** +30% dano de magia

---

### 🧜 Siren
- **ID:** `siren`
- **Comando:** `/raca escolher siren`
- **Atributos:** HP 20 | Velocidade 0.10
- **Habilidades:**
  - Respira na água
  - +50% nado
  - **Enchanting Song (H):** Atrai mobs raio 12, duram 10s, cd 50s
- **Fraquezas:** +50% dano de fogo

---

### 🐒 Hadozee
- **ID:** `hadozee`
- **Comando:** `/raca escolher hadozee`
- **Atributos:** HP 20 | Velocidade 0.11 | Escala 0.95
- **Habilidades:**
  - **Glide:** Slow Falling permanente
  - **Climbing (SHIFT + olhar parede):** Escala paredes
  - +2 dano desarmado
- **Fraquezas:** +30% dano perfurante

---

### 🦛 Giff
- **ID:** `giff`
- **Comando:** `/raca escolher giff`
- **Atributos:** HP 28 | Dano 1.3 | Velocidade 0.08 | Armadura 2.0 | Escala 1.40
- **Habilidades:**
  - **Astral Spark (H):** Explosão raio 4, 8 de dano, cd 45s
  - Resistência a knockback
  - +20% dano com qualquer arma
- **Fraquezas:** +30% dano de explosão

---

## Raças Góticas / Místicas

### 🔄 Shifter
- **ID:** `shifter`
- **Comando:** `/raca escolher shifter <sub-raça>`
- **Sub-raças (3):** longtooth, razorclaw, wildhunt
- **Atributos:** HP 22 | Dano 1.0 | Velocidade 0.11 | Night Vision
- **Sistema de 2 estágios:**
  - **H (1ª vez):** Ativa Shifting por 30s (buffs + partículas)
  - **H (2ª vez durante Shifting):** Usa poder especial
  - **Cooldown:** 60s após Shifting terminar
- **Fraquezas:** +30% dano de armas de ferro (metal ferroeiro)

**Sub-raças:**

| Sub-raça | Shifting | Poder Especial (H durante Shifting) |
|----------|----------|-------------------------------------|
| 🦷 **Longtooth** | Strength I + Speed I | Mordida: 6 dano cone 3 blocos + Poison I, cd 5s |
| 🗡️ **Razorclaw** | Haste I + Speed I | Golpe Duplo: 2x dano no mob mais próximo, cd 5s |
| 👁️ **Wildhunt** | Speed II + Glowing (raio 16) | Rastrear: mostra mob + coords + Glowing 10s, cd 10s |

---

### 🧠 Vedalken
- **ID:** `vedalken`
- **Comando:** `/raca escolher vedalken`
- **Atributos:** HP 20 | Dano 1.0 | Velocidade 0.10 | Night Vision
- **Habilidades:**
  - -30% dano mágico
  - +10% XP de mobs
  - **Arcane Insight (H):** Glowing em monstros raio 10 por 10s, cd 60s
- **Fraquezas:** +30% dano físico (trade-off pela resistência mágica)

---

### 🧙‍♀️ Hexblood
- **ID:** `hexblood`
- **Comando:** `/raca escolher hexblood`
- **Atributos:** HP 20 | Dano 1.0 | Velocidade 0.10 | Night Vision
- **Habilidades:**
  - -20% dano mágico
  - Quem te ataca corpo a corpo toma Nausea I por 3s
  - **Hex (H):** Marca mob por 30s (+20% dano dele), cd 45s
- **Fraquezas:** +30% dano sagrado (MAGIC)

---

### 💀 Reborn
- **ID:** `reborn`
- **Comando:** `/raca escolher reborn`
- **Atributos:** HP 22 | Dano 1.0 | Velocidade 0.10 | Night Vision
- **Habilidades:**
  - Respira na água
  - 50% resistência a poison
  - +10% XP de mobs
  - **Deathly Resilience (H):** Se HP < 6, ganha Resistance II + Regeneration II por 5s, cd 60s
- **Fraquezas:** +50% dano de fogo (elemento oposto à reconstrução)

---

### 🦠 Plasmoid
- **ID:** `plasmoid`
- **Comando:** `/raca escolher plasmoid`
- **Atributos:** HP 22 | Dano 1.0 | Velocidade 0.10 | Escala 0.70 | Night Vision
- **Habilidades:**
  - Respira na água
  - Imunidade a poison
  - **Amorphous (H):** Invisibilidade + Resistance I por 3s, cd 60s
- **Fraquezas:** +50% dano de gelo (elemento oposto à forma amorfa)

---

## Raças Elementais

### 🌊 Genasi
- **ID:** `genasi`
- **Comando:** `/raca escolher genasi <elemento>`
- **Sub-raças (4):** ar, fogo, terra, agua
- **Fraquezas:** Ar: +30% dano de explosão | Fogo: +50% dano na água e chuva (elemento oposto) | Terra: +30% dano de explosão | Água: +50% dano de fogo (elemento oposto)

**Sub-raças:**

| Elemento | HP | Dano | Vel | Armadura | Escala | Night Vision | Habilidades |
|----------|-----|------|-----|----------|--------|--------------|-------------|
| 💨 **Ar** | 22 | 1.0 | 0.11 | 0 | 1.0 | ❌ | Slow Falling, Super Jump (R), **Gust (H):** empurra cone 5 blocos + Levitation I, cd 30s |
| 🔥 **Fogo** | 22 | 1.1 | 0.10 | 0 | 1.0 | ✅ | Imune fogo, ataque aplica fogo, **Flame Burst (H):** explosão raio 3, 6 dano + fogo 5s, cd 30s |
| 🪨 **Terra** | 26 | 1.1 | 0.08 | 4.0 | 1.10 | ✅ | Imune queda 5 blocos, knockback resistance, **Earth Shield (H):** Absorption V por 10s, cd 60s |
| 💧 **Água** | 22 | 1.0 | 0.10 | 0 | 1.0 | ✅ | Respira água, +50% nado, Regen I na água, **Water Whip (H):** puxa mob + 4 dano + Slowness II, cd 20s |

---

## Raças Exóticas

### 🐍 Naga
- **ID:** `naga`
- **Comando:** `/raca escolher naga`
- **Atributos:** HP 22 | Dano 1.0 | Velocidade 0.10 | Armadura 3.0 | Night Vision
- **Habilidades:**
  - Ataque mão vazia aplica Poison I por 3s
  - Respira na água
  - +50% nado
  - **Constrict (H):** Prende alvo (raio 8) no lugar por 5s + 4 dano + Poison II, cd 30s
- **Fraquezas:** +30% dano de mobs mortos-vivos (resistência à vida natural)

---

### 🧠 Kalashtar
- **ID:** `kalashtar`
- **Comando:** `/raca escolher kalashtar`
- **Atributos:** HP 22 | Dano 1.0 | Velocidade 0.10 | Night Vision
- **Habilidades:**
  - -50% dano mágico
  - Imune a Nausea
  - **Telepatia (passiva):** Mostra players raio 20 ao agachar (a cada 1s)
  - **Mind Link (H):** Glowing em hostis raio 16 por 10s, cd 30s
- **Fraquezas:** +30% dano físico (trade-off pela resistência mágica)

---

### 🧬 Simic Hybrid
- **ID:** `simichybrid`
- **Comando:** `/raca escolher simichybrid <mutação>`
- **Sub-raças (3):** manta, climber, grappling
- **Atributos:** HP 22 | Dano 1.0 | Velocidade 0.10 | Night Vision
- **Habilidade base (H):** Adaptive Shield - Resistance I por 10s, cd 45s
- **Fraquezas:** +30% dano de magia (trade-off pela adaptabilidade)

**Mutações:**

| Mutação | Especial |
|---------|----------|
| 🦋 **Manta** | Planador (Slow Falling permanente) |
| 🧗 **Climber** | Escalada em paredes (SHIFT + encostado em parede) |
| 🦾 **Grappling** | Ataque extra (+2 dano desarmado) |

---

### 🐘 Loxodon
- **ID:** `loxodon`
- **Comando:** `/raca escolher loxodon`
- **Atributos:** HP 28 | Dano 1.1 | Velocidade 0.08 | Armadura 4.0 | Escala 1.30 | Night Vision
- **Habilidades:**
  - Resistência a knockback
  - +2 dano desarmado (tromba)
  - Imunidade a queda (3 blocos)
  - **Trunk Slam (H):** 6 dano área raio 3 + knockback forte, cd 30s
- **Fraquezas:** +30% dano de explosão (vulnerabilidade devido ao tamanho)

---

### 🐴 Centaur
- **ID:** `centaur`
- **Comando:** `/raca escolher centaur`
- **Atributos:** HP 24 | Dano 1.1 | Velocidade 0.12 | Escala 1.15
- **Habilidades:**
  - +2 dano desarmado (cascos)
  - Imunidade a queda (2 blocos)
  - **Charge (H):** Speed III por 5s, ao colidir: 4 dano + knockback 2.5 blocos + termina charge, cd 30s
- **Fraquezas:** +30% dano perfurante (vulnerabilidade devido ao tamanho)

### 🐥 Fungril
- **ID:** `fungril`
- **Comando:** `/raca escolher fungril`
- **Atributos:** HP 22 | Velocidade 0.08 | Escala 1.05 | Armadura 3.0 | Night Vision
- **Habilidades:**
  - Aura de veneno
  - **Spore Cloud (H):** Nuvem de esporos, cd 30s
- **Fraquezas:** +50% dano de fogo (elemento oposto ao fungo)

### 🐵 Wukong
- **ID:** `wukong`
- **Comando:** `/raca escolher wukong`
- **Atributos:** HP 22 | Dano 1.2 | Velocidade 0.11 | Night Vision
- **Habilidades:**
  - +2 dano desarmado
  - Escala paredes agachado
  - **Rugido (H):** Rugido poderoso, cd 30s
- **Fraquezas:** Sofre 1 dano/tick na água (hidrofobia) | +30% dano de raio (elemento oposto)

### 🕷️ Drider
- **ID:** `drider`
- **Comando:** `/raca escolher drider`
- **Atributos:** HP 22 | Dano 1.2 | Night Vision
- **Habilidades:**
  - Escala paredes agachado
  - Aranhas não atacam
  - **Teia (H):** Teia aracnídea, cd 30s
  - Fome 20% mais lenta
- **Fraquezas:** Só come carne | +50% dano de fogo

---

## Como Usar

### Comandos Básicos

```
/raca listar                    # Lista todas as raças
/raca ver                       # Mostra sua raça atual
/raca reset                     # Remove sua raça
/raca breath                    # Baforada (só Draconato)
```

### Escolhendo Raças

```
# Raças simples (sem sub-raça)
/raca escolher humano
/raca escolher aarakocra
/raca escolher vampire

# Raças com sub-raça
/raca escolher dragonborn red
/raca escolher elf drow
/raca escolher genasi fogo
/raca escolher shifter longtooth
/raca escolher simichybrid manta
```

### Teclas

| Tecla | Função |
|-------|--------|
| **R** | Super Jump (Aarakocra, Aasimar, Satyr, Genasi Ar, Harengon, Githyanki) |
| **H** | Habilidade ativa (cada raça tem uma única ou Shifter tem sistema duplo) |

---

## Estatísticas

| Categoria | Quantidade |
|-----------|------------|
| **Raças totais** | 53 |
| **Raças com sub-raças** | 7 |
| **Sub-raças totais** | 22 |
| **Combinações únicas** | 67 |
| **Habilidades ativas (H)** | 43 |
| **Super Jumps (R)** | 6 |
| **Raças aquáticas** | 8 |
| **Raças tanque (HP ≥ 26)** | 6 |
| **Raças pequenas (escala < 0.70)** | 6 |
| **Raças grandes (escala > 1.20)** | 5 |

---

## Estrutura Técnica

### Arquivos de Raça
Todas as raças estão em: `src/main/java/com/pedro/racasclasses/race/impl/`

### Interface Race
```java
public interface Race {
    // Identificação
    String getId();
    String getDisplayName();
    
    // Atributos
    double getMaxHealth();
    double getAttackDamage();
    double getMovementSpeed();
    // ... (mais atributos)
    
    // Sub-raças
    boolean hasSubrace();
    String[] getSubraceIds();
    void onSubraceChosen(ServerPlayer player, String subraceId);
    
    // Habilidades
    boolean canSuperJump();
    void executeSuperJump(ServerPlayer player);
    boolean canUseAbility();
    void executeAbility(ServerPlayer player);
    
    // Eventos
    void onPlayerTick(ServerPlayer player);
    void onPlayerHurt(ServerPlayer player, LivingIncomingDamageEvent event);
    void onAttackEntity(ServerPlayer player, LivingIncomingDamageEvent event);
    // ... (mais hooks)
}
```

---

**Última atualização:** 20/09/2026  
**Versão do mod:** 1.0.0  
**Minecraft:** 1.21.1  
**NeoForge:** 21.1.x
