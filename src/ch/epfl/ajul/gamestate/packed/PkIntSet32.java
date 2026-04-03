package ch.epfl.ajul.gamestate.packed;

/// Représentation compacte d'un ensemble d'entiers compris entre 0 et 31 (inclus),
/// empaqueté dans un {@code int} : le bit {@code i} vaut 1 si et seulement si
/// l'entier {@code i} appartient à l'ensemble.
///
/// @author Ismaël Ayachi (393163)
public final class PkIntSet32 {

    /// L'ensemble vide, dont la valeur empaquetée est 0.
    public static final int EMPTY = 0;

    /// Masque d'un seul bit, utilisé pour isoler ou modifier un bit dans l'ensemble.
    public static final int ONE_BIT_MASK = 0b1;

    /// Retourne {@code true} si l'entier {@code i} appartient à l'ensemble empaqueté
    /// {@code pkIntSet32}.
    ///
    /// @param pkIntSet32 l'ensemble empaqueté
    /// @param i l'entier dont on teste l'appartenance (entre 0 et 31 inclus)
    /// @return {@code true} si {@code i} appartient à l'ensemble, {@code false} sinon
    public static boolean contains(int pkIntSet32, int i) {
        assert isIndexValid(i);
        return (((pkIntSet32 >> i) & ONE_BIT_MASK) == ONE_BIT_MASK);
    }

    /// Retourne {@code true} si tous les éléments de l'ensemble empaqueté
    /// {@code pkIntSet32b} sont également présents dans {@code pkIntSet32a},
    /// c'est-à-dire si {@code pkIntSet32b} est un sous-ensemble de {@code pkIntSet32a}.
    ///
    /// @param pkIntSet32a le premier ensemble empaqueté
    /// @param pkIntSet32b le second ensemble empaqueté
    /// @return {@code true} si {@code pkIntSet32b} ⊆ {@code pkIntSet32a}
    public static boolean containsAll(int pkIntSet32a, int pkIntSet32b) {
        return (pkIntSet32a & pkIntSet32b) == pkIntSet32b;
    }

    /// Retourne l'ensemble empaqueté obtenu en ajoutant l'entier {@code i} à
    /// l'ensemble empaqueté {@code pkIntSet32}. Si {@code i} est déjà présent,
    /// l'ensemble est retourné inchangé.
    ///
    /// @param pkIntSet32 l'ensemble empaqueté
    /// @param i l'entier à ajouter (entre 0 et 31 inclus)
    /// @return l'ensemble empaqueté contenant {@code i}
    public static int add(int pkIntSet32, int i) {
        assert isIndexValid(i);
        return pkIntSet32 | (ONE_BIT_MASK << i);
    }

    /// Retourne l'ensemble empaqueté obtenu en retirant l'entier {@code i} de
    /// l'ensemble empaqueté {@code pkIntSet32}. Si {@code i} est absent,
    /// l'ensemble est retourné inchangé.
    ///
    /// @param pkIntSet32 l'ensemble empaqueté
    /// @param i l'entier à retirer (entre 0 et 31 inclus)
    /// @return l'ensemble empaqueté ne contenant plus {@code i}
    public static int remove(int pkIntSet32, int i) {
        assert isIndexValid(i);
        return pkIntSet32 & ~(ONE_BIT_MASK << i);
    }

    /// Retourne {@code true} si l'index {@code i} est valide,
    /// c'est-à-dire compris entre 0 (inclus) et {@link Integer#SIZE} (exclus).
    ///
    /// @param i l'index à valider
    /// @return {@code true} si {@code 0 <= i < 32}
    private static boolean isIndexValid(int i) {
        return i >= 0 && i < Integer.SIZE;
    }
}