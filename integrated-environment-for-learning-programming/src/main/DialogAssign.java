package main;

import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.Pair;
import main.interpreter.Expressions;
import main.interpreter.Utils;
import main.interpreter.Variables;

import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;

public class DialogAssign {
    /**
     * Dialog to be displayed
     */
    private Dialog<Triplet<Variables.Variable,Expressions.Expression,Expressions.OperatorAt>> dialog;

    Variables.Variable variable;
    Expressions.Expression expression;
    Expressions.Expression index;
    Expressions.OperatorAt operatorAt;

    public DialogAssign() {
        // Create the custom dialog.
        dialog = new Dialog<>();
        dialog.setTitle("Przypisz wartość");
        dialog.setHeaderText("Wybierz elementy przypisania.");

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        //Create UI
        //Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        //Elements

        grid.add(new Label("Wybierz do czego przypisać:"), 0, 0);

        //variable
        ChoiceBox varBox = new ChoiceBox();
        varBox.setItems(FXCollections.observableArrayList(Utils.getVariableNameList()));
        grid.add(varBox, 1, 0);

        //array index
        Button arrButton = new Button("Wybierz element tablicy");
        grid.add(arrButton, 1, 1);
        arrButton.setVisible(false);

        //expression to be assigned
        grid.add(new Label("Wybierz co przypisać:"),2,0);
        Button expButton = new Button("Wprowadź wyrażenie");
        grid.add(expButton,3,0);

        //display
        Label nameLabel = new Label();
        GridPane.setHalignment(nameLabel, HPos.RIGHT);

        Label eq = new Label("=");
        GridPane.setHalignment(eq, HPos.CENTER);

        Label expressionLabel = new Label();
        nameLabel.setTextAlignment(TextAlignment.LEFT);

        grid.add(nameLabel,0,2);
        grid.add(eq,1,2);
        grid.add(expressionLabel,2,2);

        //setting dialog content
        dialog.getDialogPane().setContent(grid);

        //choosing variable
        varBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (Utils.variables.get(newValue) instanceof Variables.Array){
                        arrButton.setVisible(true);
                        arrButton.setVisible(true);
                        variable = Utils.variables.get(varBox.getSelectionModel().getSelectedItem().toString());
                        nameLabel.setText(variable.getName()+"[]");
                    } else {
                        arrButton.setVisible(false);
                        arrButton.setVisible(false);
                        variable = Utils.variables.get(varBox.getSelectionModel().getSelectedItem().toString());
                        nameLabel.setText(variable.getName());
                    }
                });

        arrButton.setOnAction((action)->{
            DialogExpression dialog = new DialogExpression(
                    "Wybierz indeks w tablicy",
                    Utils.getFullExpList(),
                    Utils.getFullOpList());
            Optional<Expressions.Expression> result = dialog.showAndAwait();
            if(result.isPresent()){
                index = result.get();
                nameLabel.setText(variable.getName()+"["+index.toString()+"]");
            }
        });

        expButton.setOnAction((action)->{
            DialogExpression dialog = new DialogExpression(
                    "Wprowadź wyrażenie do przypisania",
                    Utils.getFullExpList(),
                    Utils.getFullOpList());
            Optional<Expressions.Expression> result = dialog.showAndAwait();
            if(result.isPresent()){
                expression = result.get();
                expressionLabel.setText(expression.toString());
            }
        });

        //Prevent confirmation without valid data
        final Button ok = (Button)dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if(varBox.getSelectionModel().isEmpty()){
                nameLabel.setText("Wybierz zmienną!");
                event.consume();
            } else {
                if(variable instanceof Variables.Array && index==null){
                    nameLabel.setText("Wybierz indeks!");
                    event.consume();
                }
            }
        });

        dialog.setResultConverter((buttonType)->{
            if(buttonType == ButtonType.OK){
                if(variable instanceof Variables.Array){
                    operatorAt = new Expressions.OperatorAt(variable.getName(),index);
                }
                if(expression!=null && variable!=null){
                    return new Triplet<>(variable,expression,operatorAt);
                }
            }
            return null;
        });
    }

    public Optional<Triplet<Variables.Variable,Expressions.Expression,Expressions.OperatorAt>> showAndAwait(){
        return dialog.showAndWait();
    }
}

