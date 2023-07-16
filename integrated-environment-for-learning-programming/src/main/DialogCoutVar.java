package main;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import main.interpreter.Expressions;
import main.interpreter.Utils;
import main.interpreter.Variables;

public class DialogCoutVar {
    private Dialog<Pair<Variables.Variable,Expressions.Expression>> dialog;

    public DialogCoutVar(){
        dialog = new Dialog<>();
        dialog.setTitle("Wyświetl wiadomość");
        dialog.setHeaderText("Wpisz wiadomość do wyświetlenia");

        //Create UI for printing variables
        GridPane gridVar = new GridPane();
        gridVar.setVisible(false);
        gridVar.setHgap(10);
        gridVar.setVgap(10);
        gridVar.setPadding(new Insets(20, 150, 10, 10));

        ChoiceBox name = new ChoiceBox();
        name.setItems(FXCollections.observableArrayList(Utils.getVariableNameList()));
        javafx.scene.control.Label nameErrorMessage = new javafx.scene.control.Label();
        nameErrorMessage.setVisible(false);
        nameErrorMessage.setTextFill(Color.RED);
        nameErrorMessage.setText("Wybierz zmienną");
        javafx.scene.control.TextField indexInput = new javafx.scene.control.TextField();
        indexInput.setVisible(false);
        indexInput.setPromptText("Indeks w tablicy");

        gridVar.add(new javafx.scene.control.Label("Zmienna:"), 0, 0);
        gridVar.add(name, 1, 0);
        gridVar.add(nameErrorMessage,1,1);
        gridVar.add(indexInput, 2, 0);

        // Do some validation (using the Java 8 lambda syntax).

        name.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (Utils.variables.get(newValue) instanceof Variables.Array){
                        indexInput.setVisible(true);
                    } else {
                        indexInput.setVisible(false);
                    }
                });

        name.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue==null){
                        nameErrorMessage.setVisible(true);
                    } else {
                        nameErrorMessage.setVisible(false);
                    }
                });

        // force the field to be numeric only
        indexInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                indexInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

//        Prevent confirmation without valid data
        final javafx.scene.control.Button ok = (Button)dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if(name.getSelectionModel().isEmpty()){
                nameErrorMessage.setVisible(true);
                event.consume();
            }
        });

        // Convert the result to a name-type-pair when the OK button is clicked.
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == ButtonType.OK) {
//                Expressions.Expression index;
//                if(!indexInput.getText().isEmpty())
//                    index=Integer.parseInt(indexInput.getText());
//                return new Pair<>(name.getSelectionModel().getSelectedItem(),index);
//            }
//            return null;
//        });
    }

//    public Optional<Variables.Variable,Expressions.Expression>> showAndAwait(){
//        return dialog.showAndWait();
//    }
}
