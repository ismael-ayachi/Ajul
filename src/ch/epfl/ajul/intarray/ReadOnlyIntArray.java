package ch.epfl.ajul.intarray;

///
/// Vue en lecture seule d'un tableau d'entiers.
///
/// @author Ismaël Ayachi (393163)
///
public interface ReadOnlyIntArray {
    ///
    /// Retourne la taille du tableau.
    ///
    /// @return le nombre d'éléments
    ///
    int size();

    ///
    /// Retourne l'élément à l'indice donné.
    ///
    /// @param i l'indice (entre 0 inclus et {@link #size()} exclus)
    /// @return la valeur à l'indice {@code i}
    /// @throws ArrayIndexOutOfBoundsException si {@code i} est hors bornes
    ///
    int get(int i);

    ///
    /// Retourne une version immuable contenant les mêmes valeurs.
    ///
    /// @return une copie immuable de ce tableau
    ///
    ImmutableIntArray immutable();

    ///
    /// Retourne une copie du contenu sous forme de tableau Java.
    ///
    /// @return une copie du contenu
    ///
    int[] toArray();
}
