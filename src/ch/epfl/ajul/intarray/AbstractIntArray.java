package ch.epfl.ajul.intarray;

import java.util.Arrays;

///
/// Classe de base pour des tableaux d'entiers offrant une vue en lecture seule.
/// <p>
/// Cette classe fournit des implémentations communes pour les opérations
/// d'accès et de conversion, et laisse aux sous-classes le choix de la
/// politique de mutabilité (immuable / enveloppante, etc.).
///
/// @author Ismael Ayachi (393163)
///
public abstract class AbstractIntArray implements ReadOnlyIntArray {
    private final int[] tab;

    ///
    /// Construit un tableau d'entiers à partir du tableau donné.
    /// <p>
    /// Le tableau est stocké tel quel. Les sous-classes décident si elles
    /// exposent un comportement mutable (enveloppant) ou non.
    ///
    /// @param tab le tableau sous-jacent
    ///
    protected AbstractIntArray(int[] tab) {
        this.tab = tab;
    }

    ///
    /// Retourne la taille du tableau.
    ///
    /// @return le nombre d'éléments
    ///
    @Override
    public int size() {
        return tab.length;
    }

    ///
    /// Retourne l'élément à l'indice donné.
    ///
    /// @param i l'indice (entre 0 inclus et {@link #size()} exclus)
    /// @return la valeur à l'indice {@code i}
    /// @throws ArrayIndexOutOfBoundsException si {@code i} est hors bornes
    ///
    @Override
    public int get(int i) {
        return tab[i];
    }

    ///
    /// Retourne une version immuable contenant les mêmes valeurs.
    ///
    /// @return une copie immuable de ce tableau
    ///
    @Override
    public ImmutableIntArray immutable() {
        return ImmutableIntArray.copyOf(tab);
    }

    ///
    /// Retourne une copie du contenu sous forme de tableau Java.
    ///
    /// @return une copie du contenu
    ///
    @Override
    public int[] toArray() {
        return tab.clone();
    }

    ///
    /// Retourne une représentation textuelle du contenu.
    ///
    /// @return une chaîne représentant ce tableau
    ///
    @Override
    public String toString() {
        return Arrays.toString(tab);
    }
}