package main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import main.interpreter.Expressions;
import main.interpreter.Utils;
import main.interpreter.Variables;

import java.util.List;
import java.util.Observable;
import java.util.Optional;

public class DialogCout {

    /**
     * Dialog to be displayed
     */
    private Dialog<Triplet<String,Object,Object>> dialog;

    Optional<Expressions.Expression> result;

    /**
     * Creates dialog for displaying message on output
     */
    public DialogCout(){
        // Create the custom dialog.
        dialog = new Dialog<>();
        dialog.setTitle("Wyświetl wiadomość");
        dialog.setHeaderText("Wybierz co chcesz wyświetlić.");

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        //Create UI
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        final ToggleGroup group = new ToggleGroup();

        RadioButton str = new RadioButton("Napis");
        str.setToggleGroup(group);

        RadioButton exp = new RadioButton("Wyrażenie");
        exp.setToggleGroup(group);

        TextField strInput = new TextField();
        strInput.setVisible(false);
        strInput.setPromptText("Napis");

        Button expButton = new Button("Wprowadź wyrażenie");
        expButton.setVisible(false);

        CheckBox newLine = new CheckBox("Dodaj znak nowej linii");

        Label expression = new Label();

        grid.add(str, 0, 0);
        grid.add(exp, 1, 0);
        grid.add(strInput,0,1);
        grid.add(expButton,1,1);
        grid.add(newLine,2,0);
        grid.add(expression,2,1);

        dialog.getDialogPane().setContent(grid);

        group.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (group.getSelectedToggle() != null) {
                if(new_toggle.equals(str)){
                    System.out.println("str");
                    expButton.setVisible(false);
                    strInput.setVisible(true);
                    Platform.runLater(strInput::requestFocus);
                } else if(new_toggle.equals(exp)){
                    System.out.println("exp");
                    strInput.setVisible(false);
                    expButton.setVisible(true);
                }
            }
        });

        expButton.setOnAction((action)->{
            ObservableList comboList = Utils.getFullExpList();
            ObservableList opList = Utils.getFullOpList();
            DialogExpression dialogExpression = new DialogExpression(
                    "Zbuduj wyrażenie do wyświetlenia",
                    comboList, opList);
            result = dialogExpression.showAndAwait();
            result.ifPresent(expression1 -> expression.setText(expression1.toString()));
        });

       dialog.setResultConverter((buttonType)->{
           if(buttonType==ButtonType.OK){
               if(group.getSelectedToggle()==null && newLine.isSelected()){
                   return new Triplet<>(Utils.VARIABLE_STRING,"",newLine.isSelected());

               }
               else if(group.getSelectedToggle().equals(str)){
                   return new Triplet<>(Utils.VARIABLE_STRING,strInput.getText(),newLine.isSelected());
               } else if(group.getSelectedToggle().equals(exp) && result.isPresent()){
                   if(result.get() instanceof Expressions.MathVariable){
                       return new Triplet<>(Utils.VARIABLE,result.get(),newLine.isSelected());
                   } else {
                       return new Triplet<>(Utils.EXPRESSION, result.get(), newLine.isSelected());
                   }
               }
           }
           return null;
       });
    }


    /**
     * Shows dialog and returns result
     * @return name of variable to read into
     */
    public Optional<Triplet<String,Object,Object>> showAndAwait(){
        return dialog.showAndWait();
    }
}
