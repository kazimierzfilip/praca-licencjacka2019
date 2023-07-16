package main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.Pair;
import main.interpreter.Expressions;
import main.interpreter.Utils;
import main.interpreter.Variables;

import java.util.*;

public class DialogExpression {
    /**
     * Dialog to be displayed
     */
    private Dialog<Expressions.Expression> dialog;

    /**
     * Expression resulting from user input
     */
    private Expressions.Expression returnExp;

    /**
     * Sorted list of available options for creating expression.
     * Variables, constant, previously saved expressions.
     */
    private ObservableList<Pair<String,Object>> comboList;

    private ObservableList<String> operatorList;

    private ComboBox leftExpCombo;
    private ComboBox operatorCombo;
    private ComboBox rightExpCombo;

    Comparator<Pair<String, Object>> comparator;


    public DialogExpression(String message, ObservableList expList, ObservableList opList){
        // Create the custom dialog.
        dialog = new Dialog<>();
        dialog.setTitle("Wprowadzanie wyrażenia");
        dialog.setHeaderText(message);
        dialog.setResizable(true);

        //create observable list for comboboxes
        comboList = expList;
        comparator = Comparator.comparing(Pair::getKey);
        FXCollections.sort(comboList,comparator);

        operatorList = opList;

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        //Create UI
        GridPane mainBox = new GridPane();
        mainBox.setHgap(10);
        mainBox.setVgap(10);

        leftExpCombo = new ComboBox();
        operatorCombo = new ComboBox();
        rightExpCombo = new ComboBox();
        rightExpCombo.setDisable(true);
        Button buttonSaveExp = new Button("Zapisz na później");
        GridPane.setFillWidth(leftExpCombo,true);
        GridPane.setFillWidth(rightExpCombo,true);

        mainBox.add(leftExpCombo,0,0);
        mainBox.add(operatorCombo,1,0);
        mainBox.add(rightExpCombo,2,0);
        mainBox.add(buttonSaveExp,3,0);

        dialog.getDialogPane().setContent(mainBox);

        operatorCombo.setItems(operatorList);

        Callback<ListView<Pair<String,Object>>, ListCell<Pair<String,Object>>> cellFactory = new Callback<ListView<Pair<String,Object>>, ListCell<Pair<String,Object>>>() {
            @Override
            public ListCell<Pair<String,Object>> call(ListView<Pair<String,Object>> param) {
                return new ListCell<Pair<String,Object>>(){
                    @Override protected void updateItem(Pair<String,Object> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            switch (item.getKey()) {
                                case Utils.EXPRESSION:
                                    setText(item.getValue().toString());
                                    break;
                                case Utils.VARIABLE:
                                    setText(item.getValue().toString());
                                    break;
                                case Utils.CONSTANT:
                                    setText(item.getValue().toString());
                                    break;
                                case "const int":
                                    setText("liczba całkowita");
                                    break;
                                case "const double":
                                    setText("liczba rzeczywista");
                                    break;
                                case "const char":
                                    setText("znak");
                                    break;
                            }
                        } else {
                            setText(null);
                        }
                    }
                };
            }
            };

        leftExpCombo.setItems(comboList);
        leftExpCombo.setCellFactory(cellFactory);
        leftExpCombo.setButtonCell(cellFactory.call(null));

        rightExpCombo.setItems(comboList);
        rightExpCombo.setCellFactory(cellFactory);
        rightExpCombo.setButtonCell(cellFactory.call(null));

        leftExpCombo.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<Pair<String, Object>>) (observable, oldValue, newValue) -> {
                    if(newValue!=null) {
                        String key = newValue.getKey();
                        if (key.matches("const .*")) {
                            String type = "";
                            TextInputDialog newConstant = new TextInputDialog();
                            newConstant.setTitle("Stała");
                            switch (key) {
                                case "const int":
                                    type = Utils.VARIABLE_INT;
                                    newConstant.setHeaderText("Podaj wartość stałej typu Integer");
                                    break;
                                case "const double":
                                    type = Utils.VARIABLE_DOUBLE;
                                    newConstant.setHeaderText("Podaj wartość stałej typu Double");
                                    break;
                                case "const char":
                                    type = Utils.VARIABLE_CHAR;
                                    newConstant.setHeaderText("Podaj wartość stałej typu Char");
                                    break;
                            }
                            Optional<String> result = newConstant.showAndWait();
                            if (result.isPresent()) {
                                System.out.println("got " + result.get() + " constant");
                                Pair<String, Object> newItem = new Pair<>(Utils.CONSTANT, new Expressions.MathConstant(type, result.get()));
                                Platform.runLater(() -> addToListAndSelect(leftExpCombo,newItem));
                            }
                        }
                    }
                });

        rightExpCombo.getSelectionModel().selectedItemProperty().addListener(
                (ChangeListener<Pair<String, Object>>) (observable, oldValue, newValue) -> {
                    if(newValue!=null) {
                        String key = newValue.getKey();
                        if (key.matches("const .*")) {
                            String type = "";
                            TextInputDialog newConstant = new TextInputDialog();
                            newConstant.setTitle("Stała");
                            switch (key) {
                                case "const int":
                                    type = Utils.VARIABLE_INT;
                                    newConstant.setHeaderText("Podaj wartość stałej typu Integer");
                                    break;
                                case "const double":
                                    type = Utils.VARIABLE_DOUBLE;
                                    newConstant.setHeaderText("Podaj wartość stałej typu Double");
                                    break;
                                case "const char":
                                    type = Utils.VARIABLE_CHAR;
                                    newConstant.setHeaderText("Podaj wartość stałej typu Char");
                                    break;
                            }
                            Optional<String> result = newConstant.showAndWait();
                            if (result.isPresent()) {
                                System.out.println("got " + result.get() + " constant");
                                Pair<String, Object> newItem = new Pair<>("const", new Expressions.MathConstant(type, result.get().trim()));
                                Platform.runLater(() -> addToListAndSelect(rightExpCombo,newItem));
                            }
                        }
                    }
                });

        operatorCombo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue)-> {
                    if(newValue==null){

                    } else {
                        switch (newValue.toString()){
                            case "!": leftExpCombo.setDisable(true);
                            break;
                            default: {
                                leftExpCombo.setDisable(false);
                                rightExpCombo.setDisable(false);
                            }
                        }
                    }
                }
        );

        //button save OnAction
        buttonSaveExp.setOnAction(event -> {

            Expressions.Expression exp = getExpression();
            comboList.add(new Pair<>(Utils.EXPRESSION,exp));
            FXCollections.sort(comboList,comparator);

            System.out.println(leftExpCombo.getSelectionModel().getSelectedIndex()+", "+leftExpCombo.getSelectionModel().getSelectedItem());
            System.out.println(operatorCombo.getSelectionModel().getSelectedIndex()+", "+operatorCombo.getSelectionModel().getSelectedItem());
            System.out.println(rightExpCombo.getSelectionModel().getSelectedIndex()+", "+rightExpCombo.getSelectionModel().getSelectedItem());
            System.out.println("saved "+exp.toString());
            //            System.out.println(comboList.get(leftExpCombo.getSelectionModel().getSelectedIndex()).getValue());
        });

        //prevent returning array
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION,
                event->{
                    returnExp = getExpression();
                    if(returnExp instanceof Expressions.MathVariable) {
                        String variableName = ((Expressions.MathVariable) returnExp).getName();
                        if (Utils.variables.get(variableName) instanceof Variables.Array) {
                            event.consume();
                        }
                    }
                });

        //sending result expression
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                returnExp = getExpression();
                return returnExp;
            }
            return null;
        });

    }

    private void addToListAndSelect(ComboBox box, Pair<String,Object> item){
        comboList.add(item);
        box.getSelectionModel().select(comboList.size()-1);
        FXCollections.sort(comboList, comparator);
    }

    private Expressions.Expression getExpression() {
        Pair<String,Object> left = null;
        Pair<String,Object> right = null;
        Expressions.Expression leftExpression = null;
        Expressions.Expression rightExpression = null;
        if(!leftExpCombo.getSelectionModel().isEmpty()) {
            left = comboList.get(leftExpCombo.getSelectionModel().getSelectedIndex());

            switch (left.getKey()) {
                case Utils.VARIABLE:
                    leftExpression = new Expressions.MathVariable((String) left.getValue());
                    break;
                case Utils.CONSTANT:
                    leftExpression = (Expressions.MathConstant) left.getValue();
                    break;
                case Utils.EXPRESSION:
                    leftExpression = (Expressions.Expression) left.getValue();
                    break;
            }
        }
        if(!rightExpCombo.getSelectionModel().isEmpty()) {
            right = comboList.get(rightExpCombo.getSelectionModel().getSelectedIndex());

            switch (right.getKey()) {
                case Utils.VARIABLE:
                    rightExpression = new Expressions.MathVariable((String) right.getValue());
                    break;
                case Utils.CONSTANT:
                    rightExpression = (Expressions.MathConstant) right.getValue();
                    break;
                case Utils.EXPRESSION:
                    rightExpression = (Expressions.Expression) right.getValue();
                    break;
            }
        }

        Expressions.Expression exp = null;
        String operator;
        if(!operatorCombo.getSelectionModel().isEmpty()) {
            operator = operatorCombo.getSelectionModel().getSelectedItem().toString();
            switch (operator) {
                case ">":
                    exp = new Expressions.Greater(leftExpression, rightExpression);
                    break;
                case ">=":
                    exp = new Expressions.GreaterOrEqual(leftExpression, rightExpression);
                    break;
                case "<":
                    exp = new Expressions.Less(leftExpression, rightExpression);
                    break;
                case "<=":
                    exp = new Expressions.LessOrEqual(leftExpression, rightExpression);
                    break;
                case "==":
                    exp = new Expressions.Equal(leftExpression, rightExpression);
                    break;
                case "!=":
                    exp = new Expressions.NotEqual(leftExpression, rightExpression);
                    break;
                case "&&":
                    exp = new Expressions.And(leftExpression, rightExpression);
                    break;
                case "||":
                    exp = new Expressions.Or(leftExpression, rightExpression);
                    break;
                case "+":
                    exp = new Expressions.Addition(leftExpression, rightExpression);
                    break;
                case "-":
                    exp = new Expressions.Subtraction(leftExpression, rightExpression);
                    break;
                case "*":
                    exp = new Expressions.Multiplication(leftExpression, rightExpression);
                    break;
                case "/":
                    exp = new Expressions.Division(leftExpression, rightExpression);
                    break;
                case "%":
                    exp = new Expressions.Modulo(leftExpression, rightExpression);
                    break;
                case "[]":
                    exp = new Expressions.OperatorAt(left.getValue().toString(), rightExpression);
                    break;
                case "!":
                    exp = new Expressions.Not(rightExpression);
                    break;
            }
        } else {
            exp = leftExpression;
        }
        return exp;
    }

    public Optional<Expressions.Expression> showAndAwait(){
        return dialog.showAndWait();
    }
}
