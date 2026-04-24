package ch.epfl.ajul;

import ch.epfl.ajul.gamestate.ImmutableGameState;
import ch.epfl.ajul.gamestate.Move;
import ch.epfl.ajul.gamestate.MutableGameState;
import ch.epfl.ajul.gamestate.ReadOnlyGameState;
import ch.epfl.ajul.gamestate.packed.*;
import ch.epfl.ajul.mcts.HeuristicMoveSelector;
import ch.epfl.ajul.mcts.MctsPlayer;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static java.lang.IO.print;
import static java.lang.IO.println;

public final class AjulTUI {

    private static final int LABEL_WIDTH   = 10;
    private static final int WALL_WIDTH    = 5;
    private static final int PLATEAU_WIDTH = 18;

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

    private static String patternLine(int pkPatterns, int pkWall, TileDestination.Pattern line) {
        int    size     = PkPatterns.size(pkPatterns, line);
        String tiles    = size == 0 ? "" : PkPatterns.color(pkPatterns, line).toString().repeat(size);
        String dots     = ".".repeat(line.capacity() - size);
        String wallStr  = PkWall.toString(pkWall);
        String wallLine = wallStr.substring(1, wallStr.length() - 1).split(", ")[line.index()];
        String content  = String.format("%" + WALL_WIDTH + "s %s", dots + tiles, wallLine);
        return String.format("%-" + PLATEAU_WIDTH + "s", content);
    }

    static void printState(ReadOnlyGameState gameState) {
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

            String pte = "Pté 1 1 2 2 2 3 3";
            println(String.format(" %s%s | %s",
                    " ".repeat(LABEL_WIDTH), pte, p2 != null ? pte : ""));
            println("");
        }
    }

    /// Demande au joueur humain de saisir un coup valide.
    static Move queryNextMove(String playerName, ReadOnlyGameState gameState) {
        Scanner scanner = new Scanner(System.in);
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

                boolean isValid = false;
                for (int i = 0; i < validCount; i++) {
                    if (validMovesArr[i] == packed) { isValid = true; break; }
                }

                if (isValid) return move;
                println("Coup invalide, veuillez réessayer.");

            } catch (Exception e) {
                println("Coup invalide, veuillez réessayer (format: ex. 4B2).");
            }
        }
    }

    /// Sélectionne automatiquement un coup pour l'IA avec MctsPlayer.
    static Move queryAiMove(String playerName, ReadOnlyGameState gameState,
                            Map<PlayerId, Player> aiPlayers) {
        PlayerId current = gameState.currentPlayerId();
        Move move = aiPlayers.get(current).nextMove(gameState);
        println(playerName + " (IA) joue : "
                + move.source().index()
                + move.tileColor()
                + (move.destination() == TileDestination.FLOOR ? 0
                : ((TileDestination.Pattern) move.destination()).index() + 1));
        return move;
    }

    private static void printFinalScores(ReadOnlyGameState gameState) {
        for (PlayerId playerId : gameState.playerIds()) {
            String name   = gameState.game().playerDescriptions().get(playerId.ordinal()).name();
            int    points = PkPlayerStates.points(gameState.pkPlayerStates(), playerId);
            println(String.format("  %s : %d pts", name, points));
        }
    }

    /// Demande à l'utilisateur de choisir le type de chaque joueur (humain ou IA).
    private static Map<PlayerId, Game.PlayerDescription.PlayerKind> queryPlayerKinds(
            List<PlayerId> playerIds, List<String> names, Scanner scanner) {
        Map<PlayerId, Game.PlayerDescription.PlayerKind> kinds = new HashMap<>();
        for (int i = 0; i < playerIds.size(); i++) {
            while (true) {
                print(names.get(i) + " est-il humain ou IA ? (h/ia) : ");
                String choice = scanner.nextLine().trim().toLowerCase();
                if (choice.equals("h")) {
                    kinds.put(playerIds.get(i), Game.PlayerDescription.PlayerKind.HUMAN);
                    break;
                } else if (choice.equals("ia")) {
                    kinds.put(playerIds.get(i), Game.PlayerDescription.PlayerKind.AI);
                    break;
                } else {
                    println("Réponse invalide, entrez 'h' ou 'ia'.");
                }
            }
        }
        return kinds;
    }

    static void main() {
        Scanner scanner = new Scanner(System.in);
        RandomGenerator randomGenerator = RandomGeneratorFactory.getDefault().create(2026);

        // Configuration des joueurs
        print("Nombre de joueurs (2-4) : ");
        int n = Integer.parseInt(scanner.nextLine().trim());

        List<String> names = new ArrayList<>();
        List<PlayerId> playerIds = PlayerId.ALL.subList(0, n);
        for (int i = 0; i < n; i++) {
            print("Nom du joueur " + (i + 1) + " : ");
            names.add(scanner.nextLine().trim());
        }

        Map<PlayerId, Game.PlayerDescription.PlayerKind> kinds =
                queryPlayerKinds(playerIds, names, scanner);

        List<Game.PlayerDescription> playerInfos = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            playerInfos.add(new Game.PlayerDescription(playerIds.get(i), names.get(i), kinds.get(playerIds.get(i))));
        }

        Game game = new Game(playerInfos);
        MutableGameState gameState = new MutableGameState(ImmutableGameState.initial(game));
        gameState.fillFactories(randomGenerator);

        // Créer les instances MctsPlayer pour les joueurs IA
        Map<PlayerId, Player> aiPlayers = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (kinds.get(playerIds.get(i)) == Game.PlayerDescription.PlayerKind.AI) {
                aiPlayers.put(playerIds.get(i), new MctsPlayer(RandomGeneratorFactory.getDefault(), 1000000));
            }
        }

        while (!gameState.isGameOver()) {
            printState(gameState);
            PlayerId current = gameState.currentPlayerId();
            int idx = current.ordinal();
            String playerName = names.get(idx);
            Game.PlayerDescription.PlayerKind kind = kinds.get(current);

            Move move;
            if (kind == Game.PlayerDescription.PlayerKind.HUMAN) {
                move = queryNextMove(playerName, gameState);
            } else {
                move = queryAiMove(playerName, gameState, aiPlayers);
            }

            gameState.registerMove(move.packed());
            if (gameState.isRoundOver()) {
                gameState.endRound();
                if (!gameState.isGameOver()) {
                    gameState.fillFactories(randomGenerator);
                    println("=== Nouvelle manche === ");
                }
            }
        }

        gameState.endGame();
        printState(gameState);
        println("Partie terminée ! Scores finaux :");
        printFinalScores(gameState);
    }
}