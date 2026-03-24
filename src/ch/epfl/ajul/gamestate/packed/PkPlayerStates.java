package ch.epfl.ajul.gamestate.packed;

import ch.epfl.ajul.Game;
import ch.epfl.ajul.PlayerId;
import ch.epfl.ajul.intarray.ImmutableIntArray;
import ch.epfl.ajul.intarray.ReadOnlyIntArray;

/// Classe utilitaire permettant de manipuler les états empaquetés de tous les joueurs
/// d'une partie d'Ajul, stockés dans un tableau de {@code 4n} entiers où {@code n}
/// est le nombre de joueurs.
/// Pour chaque joueur d'identité {@code p}, les 4 éléments correspondants du tableau
/// contiennent dans l'ordre : le contenu des lignes de motif, le contenu de la ligne
/// plancher, le contenu du mur, et le nombre de points.
///
/// @author Ismaël Ayachi (393163)
public final class PkPlayerStates {

    /// Nombre de champs empaquetés par joueur dans le tableau d'état.
    private static final int PLAYER_STATE_CONTENT_SIZE = 4;

    /// Index du champ contenant le contenu des lignes de motif dans le bloc d'un joueur.
    private static final int PATTERN_OFFSET = 0;

    /// Index du champ contenant le contenu de la ligne plancher dans le bloc d'un joueur.
    private static final int FLOOR_OFFSET = 1;

    /// Index du champ contenant le contenu du mur dans le bloc d'un joueur.
    private static final int WALL_OFFSET = 2;

    /// Index du champ contenant le nombre de points dans le bloc d'un joueur.
    private static final int POINTS_OFFSET = 3;

    /// Retourne un tableau immuable contenant l'état empaqueté initial des joueurs
    /// de la partie {@code game}, dans lequel toutes les lignes de motif, lignes
    /// plancher et murs sont vides, et tous les points valent 0.
    ///
    /// @param game la configuration de la partie
    /// @return le tableau immuable de l'état initial des joueurs
    public static ImmutableIntArray initial(Game game) {
        int[] initialState = new int[PLAYER_STATE_CONTENT_SIZE * game.playersCount()];
        for (int i = 0; i < game.playersCount(); i++) {
            initialState[PLAYER_STATE_CONTENT_SIZE * i + PATTERN_OFFSET] = PkPatterns.EMPTY;
            initialState[PLAYER_STATE_CONTENT_SIZE * i + FLOOR_OFFSET] = PkFloor.EMPTY;
            initialState[PLAYER_STATE_CONTENT_SIZE * i + WALL_OFFSET] = PkWall.EMPTY;
            initialState[PLAYER_STATE_CONTENT_SIZE * i + POINTS_OFFSET] = 0;
        }
        return ImmutableIntArray.copyOf(initialState);
    }

    /// Retourne le contenu empaqueté des lignes de motif du joueur {@code playerId},
    /// extrait du tableau {@code pkPlayerStates}.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @return le contenu empaqueté des lignes de motif du joueur
    public static int pkPatterns(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + PATTERN_OFFSET);
    }

    /// Retourne le contenu empaqueté de la ligne plancher du joueur {@code playerId},
    /// extrait du tableau {@code pkPlayerStates}.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @return le contenu empaqueté de la ligne plancher du joueur
    public static int pkFloor(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + FLOOR_OFFSET);
    }

    /// Retourne le contenu empaqueté du mur du joueur {@code playerId},
    /// extrait du tableau {@code pkPlayerStates}.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @return le contenu empaqueté du mur du joueur
    public static int pkWall(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + WALL_OFFSET);
    }

    /// Retourne le nombre de points du joueur {@code playerId},
    /// extrait du tableau {@code pkPlayerStates}.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @return le nombre de points du joueur
    public static int points(ReadOnlyIntArray pkPlayerStates, PlayerId playerId) {
        return pkPlayerStates.get(playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + POINTS_OFFSET);
    }

    /// Modifie dans le tableau {@code pkPlayerStates} le contenu empaqueté des lignes
    /// de motif du joueur {@code playerId} afin qu'il vaille {@code pkPatterns}.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @param pkPatterns     le nouveau contenu empaqueté des lignes de motif
    public static void setPkPatterns(int[] pkPlayerStates, PlayerId playerId, int pkPatterns) {
        pkPlayerStates[playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + PATTERN_OFFSET] = pkPatterns;
    }

    /// Modifie dans le tableau {@code pkPlayerStates} le contenu empaqueté de la ligne
    /// plancher du joueur {@code playerId} afin qu'il vaille {@code pkFloor}.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @param pkFloor        le nouveau contenu empaqueté de la ligne plancher
    public static void setPkFloor(int[] pkPlayerStates, PlayerId playerId, int pkFloor) {
        pkPlayerStates[playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + FLOOR_OFFSET] = pkFloor;
    }

    /// Modifie dans le tableau {@code pkPlayerStates} le contenu empaqueté du mur
    /// du joueur {@code playerId} afin qu'il vaille {@code pkWall}.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @param pkWall         le nouveau contenu empaqueté du mur
    public static void setPkWall(int[] pkPlayerStates, PlayerId playerId, int pkWall) {
        pkPlayerStates[playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + WALL_OFFSET] = pkWall;
    }

    /// Modifie dans le tableau {@code pkPlayerStates} le nombre de points du joueur
    /// {@code playerId} en lui ajoutant {@code pointsToAdd}, qui peut être négatif.
    ///
    /// @param pkPlayerStates le tableau contenant les états empaquetés des joueurs
    /// @param playerId       l'identité du joueur
    /// @param pointsToAdd    le nombre de points à ajouter (peut être négatif)
    public static void addPoints(int[] pkPlayerStates, PlayerId playerId, int pointsToAdd) {
        pkPlayerStates[playerId.ordinal() * PLAYER_STATE_CONTENT_SIZE + POINTS_OFFSET] += pointsToAdd;
    }
}