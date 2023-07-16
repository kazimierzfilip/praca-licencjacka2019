package main;

import javafx.scene.control.TextInputDialog;

import java.io.Serializable;
import java.util.Optional;

public class DialogCinInput extends TextInputDialog implements Serializable {

    public DialogCinInput(String message) {
        setTitle("Wczytywanie wartości");
        setHeaderText("Wprowadź wartość");
        setContentText(message);
    }
}
