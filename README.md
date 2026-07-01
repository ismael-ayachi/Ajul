# Ajul

> A Java + JavaFX implementation of the award‑winning board game **Azul**, built for the [EPFL CS‑108 course](https://cs108.epfl.ch/) ("Pratique de la programmation orientée objet").

Ajul is a full digital version of Azul supporting **2 to 4 players**, where each seat can be controlled by a **human** or by an **AI** driven by a Monte‑Carlo Tree Search (MCTS) engine. The project was developed incrementally over 12 weekly stages, moving from low‑level, bit‑packed data structures all the way up to an animated, interactive graphical interface.

<p align="center">
  <img src="https://cs108.epfl.ch/p/i/ajul-numbered-gui;64.png" alt="Ajul graphical interface" width="720">
</p> 

---

## Table of Contents

- [About the Game](#about-the-game)
- [Screenshots](#screenshots)
- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Building & Running](#building--running)
- [Command‑Line Arguments](#command-line-arguments)
- [Development Stages](#development-stages)
- [Tech Stack](#tech-stack)
- [Credits](#credits)

---

## About the Game

In Azul, players are artisans decorating the walls of the Royal Palace of Évora with colored ceramic tiles. The goal is to **score as many points as possible** by progressively filling the squares of your personal wall.

**Core rules**

- The game uses **100 tiles** in 5 colors (20 each), plus a first‑player marker.
- The number of **factories** depends on the player count: **5** (2 players), **7** (3 players), **9** (4 players).
- On your turn, you take **all tiles of a single color** from one factory or from the central zone, then place them on one of your five **pattern lines** (capacities 1–5).
- Tiles that don't fit overflow onto the **floor line**, costing penalty points.
- When all tiles have been drafted, the round ends: the rightmost tile of each **completed** pattern line moves onto the **wall**, scoring points based on horizontal and vertical adjacency.
- The game ends when a player completes a full **horizontal line** on their wall. Bonus points are then awarded for complete rows, columns, and colors.

---

## Screenshots

| Player board | Pattern lines | Floor line |
| --- | --- | --- |
| <img src="https://cs108.epfl.ch/p/i/player-board;64.png" width="240"> | <img src="https://cs108.epfl.ch/p/i/pattern-lines;32.png" width="200"> | <img src="https://cs108.epfl.ch/p/i/floor-line;16.png" width="200"> |

| The wall | Wall groups (scoring) | 4‑player board |
| --- | --- | --- |
| <img src="https://cs108.epfl.ch/p/i/wall-contents;64.png" width="200"> | <img src="https://cs108.epfl.ch/p/i/wall-groups;64.png" width="200"> | <img src="https://cs108.epfl.ch/p/i/board-ui-4;64.png" width="240"> |

---

## Features

- 🎮 **2–4 players**, any mix of humans and AI opponents.
- 🤖 **AI player** based on **Monte‑Carlo Tree Search** with a heuristic move selector and configurable iteration budget.
- ⚡ **Bit‑packed game state** — pattern lines, floor, wall, tile sets and moves are all encoded into `int`/`short` values, using SWAR (SIMD‑Within‑A‑Register) techniques for fast simulation.
- 🖱️ **Drag‑and‑drop** interface with **smooth tile animations** built in JavaFX.
- 🧵 **Multithreaded design** — a dedicated game thread runs the simulation while the JavaFX thread stays responsive, communicating safely via a `SynchronousQueue`.
- 🌱 **Deterministic seeding** — pass a seed for reproducible games.

---

## Architecture

The codebase separates the **game logic** (pure, packed, testable) from the **graphical interface** (JavaFX), with the AI as a third pillar.

```
ch.epfl.ajul
├── (root)              Core domain types: TileKind, TileSource, TileDestination,
│                       PlayerId, Player, Game, Points, RankComputer, PointsObserver
├── intarray            Mutable / ReadOnly / Immutable int‑array abstractions
├── gamestate           High‑level game state: ReadOnlyGameState, ImmutableGameState,
│   │                   MutableGameState, Move
│   └── packed          Bit‑level encoders: PkPatterns, PkFloor, PkWall, PkTileSet,
│                       PkMove, PkPlayerStates, PkIntSet32
├── mcts                AI: MctsNode, MctsPlayer, HeuristicMoveSelector
└── gui                 JavaFX UI: Main, BoardUI, Tiles, TileOverlayUI, TileLocation,
                        TileAnimator, RelocationTransition
```

**Key design ideas**

- **Packed representation.** Instead of allocating objects, most of the state lives in primitive integers. For example a tile set fits in 26 bits, a move in 10 bits, the five pattern lines in 30 bits, and the wall as a set of positions in a 32‑bit integer. This makes the millions of simulations required by the AI cheap.
- **Immutable vs. mutable state.** `ImmutableGameState` provides a safe, shareable snapshot used by the AI's search tree, while `MutableGameState` drives the actual game progression (`fillFactories`, `registerMove`, `endRound`, `endGame`).
- **Observer pattern.** `PointsObserver` reports scoring events (new wall tile, floor penalties, full row/column/color bonuses) so the UI can react and animate.

---

## Project Structure

```
Ajul/
├── src/                 Main source code (module "Ajul")
│   ├── module-info.java
│   └── ch/epfl/ajul/…
├── test/                JUnit 5 tests + a text UI (AjulTUI) and GUI launchers
├── resources/
│   └── ajul.css         JavaFX stylesheet
├── Ajul.iml             IntelliJ module descriptor
└── README.md
```

---

## Building & Running

### Prerequisites

- **JDK 21+** (developed against a recent OpenJDK; the repository was last built with JDK 25).
- **JavaFX / OpenJFX** (graphics + controls modules). The project is a Java module (`module Ajul`) that `requires javafx.graphics` and `javafx.controls`.
- **JUnit 5 (Jupiter)** for running the tests.

### Easiest path: IntelliJ IDEA

1. Open the project folder in IntelliJ IDEA.
2. Add the **OpenJFX** and **JUnit 5** libraries to the module (the `Ajul.iml` references `OpenJFX 26` and `junit.jupiter`).
3. Run `ch.epfl.ajul.gui.Main` with your chosen program arguments (see below).

### From the command line

Assuming the sources are compiled into `out/` and `PATH_TO_FX` points to your JavaFX `lib` directory:

```bash
# Compile
javac --module-path "$PATH_TO_FX" -d out $(find src -name "*.java")

# Run — e.g. one human ("Alice") vs one AI ("Bot")
java --module-path "out:$PATH_TO_FX" \
     --add-modules javafx.controls,javafx.graphics \
     -m Ajul/ch.epfl.ajul.gui.Main Alice _Bot
```

> 💡 A **text‑based** version (`ch.epfl.ajul.AjulTUI`, in `test/`) is available for playing/testing the game logic without the graphical interface.

---

## Command-Line Arguments

The `Main` class configures the game entirely from its launch arguments:

| Argument | Meaning |
| --- | --- |
| `Name` | A **human** player named `Name`. |
| `_Name` | An **AI** player named `Name` (leading underscore marks it as a bot). |
| `--seed=VALUE` | Optional seed for the random generator, for reproducible games. |

- Provide **2 to 4** player names (any order/mix of human and AI).
- The AI runs `100000` MCTS iterations per move by default.

**Examples**

```bash
# 2 players: human vs AI
… ch.epfl.ajul.gui.Main Alice _DeepTile

# 4 players: two humans, two AIs, fixed seed
… ch.epfl.ajul.gui.Main Alice Bob _Bot1 _Bot2 --seed=cs108
```

---

## Development Stages

The project was built over 12 stages of increasing autonomy. The detailed brief for each is published on the course site: `https://cs108.epfl.ch/p/NN.html` (with `NN` from `00` to `11`).

| Stage | Title | Focus |
| --- | --- | --- |
| [00](https://cs108.epfl.ch/p/00.html) | Overview | Game rules, project goals and structure |
| [01](https://cs108.epfl.ch/p/01.html) | Mise en place | Core types (`TileKind`, `TileSource`, `TileDestination`), int arrays, Fisher–Yates shuffle |
| [02](https://cs108.epfl.ch/p/02.html) | Moves & tile sets | `Move`, `PlayerId`, packed `PkMove` and `PkTileSet` (reservoir sampling, SWAR) |
| [03](https://cs108.epfl.ch/p/03.html) | Pattern lines & floor | `PkPatterns`, `PkFloor`, `Preconditions`, `Game` |
| [04](https://cs108.epfl.ch/p/04.html) | Wall & points | `PkIntSet32`, `PkWall`, `Points` (scoring & bonuses) |
| [05](https://cs108.epfl.ch/p/05.html) | Immutable game state | `PkPlayerStates`, `ReadOnlyGameState`, `ImmutableGameState`, valid‑move enumeration |
| [06](https://cs108.epfl.ch/p/06.html) | Mutable game state | `MutableGameState`, `PointsObserver`, factory filling, round/game resolution |
| [07](https://cs108.epfl.ch/p/07.html) | AI setup | `MctsNode`, `HeuristicMoveSelector`, `RankComputer`, `Player`, game‑tree concept |
| [08](https://cs108.epfl.ch/p/08.html) | Artificial intelligence | `MctsPlayer` (selection/simulation/backpropagation), `TileLocation`, `Tiles`, `RelocationTransition` |
| [09](https://cs108.epfl.ch/p/09.html) | Game board | `BoardUI` — sources, player boards, bonus display, highlighting |
| [10](https://cs108.epfl.ch/p/10.html) | Tile management & animation | `TileAnimator`, `TileOverlayUI` — drag‑and‑drop and animated moves |
| [11](https://cs108.epfl.ch/p/11.html) | Main program | `Main` — argument parsing, scene graph, threads & synchronization |

---

## Tech Stack

- **Language:** Java (JPMS module `Ajul`)
- **UI:** JavaFX (graphics, controls, CSS styling via `resources/ajul.css`)
- **AI:** Monte‑Carlo Tree Search with heuristic playouts
- **Testing:** JUnit 5 (Jupiter)
- **Build/IDE:** IntelliJ IDEA

---

## Credits

- **Author:** Ismaël Ayachi
- **Course:** [EPFL CS‑108 — Pratique de la programmation orientée objet](https://cs108.epfl.ch/)
- **Game:** *Azul*, designed by Michael Kiesling (Plan B Games / Next Move Games). This project is an educational reimplementation.

> Project images are courtesy of the EPFL CS‑108 course material.
