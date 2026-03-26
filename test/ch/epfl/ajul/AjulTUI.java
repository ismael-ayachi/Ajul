package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.*;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static java.lang.IO.print;
import static java.lang.IO.println;

public final class AjulTUI {

    private static final int LABEL_WIDTH   = 10;
    private static final int WALL_WIDTH    = 5;
    private static final int PLATEAU_WIDTH = 18; // largeur de "Pté 1 1 2 2 2 3 3"

    /// Formate le contenu d'une source sous la forme "A C DD" (lettre répétée, "1" pour le marqueur).
    private static String formatSource(int pkTileSet) {
        StringBuilder sb = new StringBuilder();
        for (TileKind.Colored color : TileKind.Colored.ALL) {
            int count = PkTileSet.countOf(pkTileSet, color);
            if (count > 0) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(color.toString().repeat(count));
            }
        }
        if (PkTileSet.countOf(pkTileSet, TileKind.FIRST_PLAYER_MARKER) > 0) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append("1");
        }
        return sb.toString();
    }

    /// Formate une ligne de motif en largeur fixe PLATEAU_WIDTH :
    /// dots+tuiles alignés à droite sur WALL_WIDTH chars, espace, ligne du mur sur WALL_WIDTH chars.
    private static String patternLine(int pkPatterns, int pkWall, TileDestination.Pattern line) {
        int    size     = PkPatterns.size(pkPatterns, line);
        String tiles    = size == 0 ? "" : PkPatterns.color(pkPatterns, line).toString().repeat(size);
        String dots     = ".".repeat(line.capacity() - size);
        String wallStr  = PkWall.toString(pkWall);
        String wallLine = wallStr.substring(1, wallStr.length() - 1).split(", ")[line.index()];
        // dots+tuiles : alignés à droite sur WALL_WIDTH ; mur : toujours WALL_WIDTH chars
        String content = String.format("%" + WALL_WIDTH + "s %s", dots + tiles, wallLine);
        return String.format("%-" + PLATEAU_WIDTH + "s", content);
    }

    static void printState(ReadOnlyGameState gameState) {
        // --- Fabriques (3 par ligne) ---
        List<TileSource.Factory> factories = gameState.game().factories();
        StringBuilder factoryLine = new StringBuilder("Fabriques :");
        for (int i = 0; i < factories.size(); i++) {
            TileSource.Factory f = factories.get(i);
            factoryLine.append(String.format(" [%d] %-12s",
                    f.index(), formatSource(gameState.pkTileSources().get(f.index()))));
            if ((i + 1) % 3 == 0 && i + 1 < factories.size()) {
                println(factoryLine.toString());
                factoryLine = new StringBuilder("            ");
            }
        }
        println(factoryLine.toString());
        println(String.format("   Centre : [0] %s",
                formatSource(gameState.pkTileSources().get(0))));
        println("");

        // --- Plateaux côte à côte (par paires) ---
        List<PlayerId> players = gameState.playerIds();
        for (int i = 0; i < players.size(); i += 2) {
            PlayerId p1  = players.get(i);
            PlayerId p2  = (i + 1 < players.size()) ? players.get(i + 1) : null;
            String name1 = gameState.game().playerDescriptions().get(i).name();
            String name2 = p2 != null ? gameState.game().playerDescriptions().get(i + 1).name() : "";

            int pkPat1 = PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), p1);
            int pkW1   = PkPlayerStates.pkWall(gameState.pkPlayerStates(), p1);
            int pkFl1  = PkPlayerStates.pkFloor(gameState.pkPlayerStates(), p1);
            int pts1   = PkPlayerStates.points(gameState.pkPlayerStates(), p1);

            int pkPat2 = p2 != null ? PkPlayerStates.pkPatterns(gameState.pkPlayerStates(), p2) : 0;
            int pkW2   = p2 != null ? PkPlayerStates.pkWall(gameState.pkPlayerStates(), p2) : 0;
            int pkFl2  = p2 != null ? PkPlayerStates.pkFloor(gameState.pkPlayerStates(), p2) : 0;
            int pts2   = p2 != null ? PkPlayerStates.points(gameState.pkPlayerStates(), p2) : 0;

            for (TileDestination.Pattern line : TileDestination.Pattern.ALL) {
                String left  = patternLine(pkPat1, pkW1, line);
                String right = p2 != null ? patternLine(pkPat2, pkW2, line) : " ".repeat(PLATEAU_WIDTH);

                String lLabel, rLabel;
                if (line.index() == 0) {
                    lLabel = String.format("%-" + LABEL_WIDTH + "s", name1);
                    rLabel = String.format("%-" + LABEL_WIDTH + "s", name2);
                } else if (line.index() == 1) {
                    lLabel = String.format("%-" + LABEL_WIDTH + "s", pts1 + " pts");
                    rLabel = String.format("%-" + LABEL_WIDTH + "s", p2 != null ? pts2 + " pts" : "");
                } else {
                    lLabel = " ".repeat(LABEL_WIDTH);
                    rLabel = " ".repeat(LABEL_WIDTH);
                }
                println(String.format(" %s%s | %s%s", lLabel, left, rLabel, right));
            }

            // Plancher (affiché uniquement si non vide)
            String floorLeft  = pkFl1 != PkFloor.EMPTY
                    ? String.format("%-" + PLATEAU_WIDTH + "s", PkFloor.toString(pkFl1))
                    : " ".repeat(PLATEAU_WIDTH);
            String floorRight = (p2 != null && pkFl2 != PkFloor.EMPTY) ? PkFloor.toString(pkFl2) : "";
            if (pkFl1 != PkFloor.EMPTY || (p2 != null && pkFl2 != PkFloor.EMPTY)) {
                println(String.format(" %s%s | %s%s",
                        " ".repeat(LABEL_WIDTH), floorLeft,
                        " ".repeat(LABEL_WIDTH), floorRight));
            }
            println("");

            // Pénalités
            String pte = "Pté 1 1 2 2 2 3 3";
            println(String.format(" %s%s | %s",
                    " ".repeat(LABEL_WIDTH), pte, p2 != null ? pte : ""));
            println("");
        }
    }

    static Move queryNextMove(String playerName, ReadOnlyGameState gameState) {
        Scanner scanner = new Scanner(System.in);

        // Calculer les coups valides
        short[] validMovesArr = new short[Move.MAX_MOVES];
        int validCount = gameState.validMoves(validMovesArr);

        while (true) {
            print("Quel coup désirez-vous jouer, " + playerName + " ? ");
            String input = scanner.nextLine().trim();

            if (input.length() < 3) {
                println("Coup invalide, veuillez réessayer (format: ex. 4B2).");
                continue;
            }

            try {
                int    sourceIndex      = Character.getNumericValue(input.charAt(0));
                String colorName        = String.valueOf(input.charAt(1));
                int    destinationIndex = Character.getNumericValue(input.charAt(2));

                TileSource       source      = TileSource.ALL.get(sourceIndex);
                TileKind.Colored color       = TileKind.Colored.valueOf(colorName);
                TileDestination  destination = destinationIndex == 0
                        ? TileDestination.FLOOR
                        : TileDestination.Pattern.ALL.get(destinationIndex - 1);

                Move move = new Move(source, color, destination);
                short packed = move.packed();

                // Vérifier que le coup est dans la liste des coups valides
                boolean isValid = false;
                for (int i = 0; i < validCount; i++) {
                    if (validMovesArr[i] == packed) {
                        isValid = true;
                        break;
                    }
                }

                if (isValid) return move;
                println("Coup invalide, veuillez réessayer.");

            } catch (Exception e) {
                println("Coup invalide, veuillez réessayer (format: ex. 4B2).");
            }
        }
    }

    private static void printFinalScores(ReadOnlyGameState gameState) {
        for (PlayerId playerId : gameState.playerIds()) {
            String name   = gameState.game().playerDescriptions().get(playerId.ordinal()).name();
            int    points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            println(String.format("  %s : %d pts", name, points));
        }
    }

    static void main() {
        RandomGenerator randomGenerator = RandomGeneratorFactory.getDefault().create(2026);

        List<Game.PlayerDescription> playerInfos = List.of(
                new Game.PlayerDescription(PlayerId.P1, "Aline",    Game.PlayerDescription.PlayerKind.HUMAN),
                new Game.PlayerDescription(PlayerId.P2, "Bertrand", Game.PlayerDescription.PlayerKind.HUMAN));

        Game game = new Game(playerInfos);

        MutableGameState gameState = new MutableGameState(ImmutableGameState.initial(game));
        gameState.fillFactories(randomGenerator);

        while (!gameState.isGameOver()) {
            printState(gameState);
            String playerName = gameState.game().playerDescriptions()
                    .get(gameState.currentPlayerId().ordinal()).name();
            Move move = queryNextMove(playerName, gameState);
            gameState.registerMove(move.packed());
            if (gameState.isRoundOver()) {
                gameState.endRound();
                if (!gameState.isGameOver())
                    gameState.fillFactories(randomGenerator);
            }
        }

        gameState.endGame();
        printState(gameState);
        println("Partie terminée ! Scores finaux :");
        printFinalScores(gameState);
    }
}