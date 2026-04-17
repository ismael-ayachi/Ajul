module Ajul {
    requires javafx.graphics;
    requires javafx.controls;
    requires java.net.http;
    requires java.desktop;
    exports ch.epfl.ajul.gui;
    exports ch.epfl.ajul.intarray;
    exports ch.epfl.ajul;
    exports ch.epfl.ajul.gamestate;
    exports ch.epfl.ajul.gamestate.packed;
    exports ch.epfl.ajul.mcts;
}