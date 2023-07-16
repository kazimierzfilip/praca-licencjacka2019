package main;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import main.interpreter.Expressions;
import main.interpreter.Utils;
import main.interpreter.Variables;

import java.util.Optional;

public class DialogCin {
    private Dialog<Pair<String,Expressions.Expression>> dialog;
    private Expressions.Expression index = null;
    private String selectedName;

    /**
     * Creates dialog for input into given variable
     */
    public DialogCin(){
        // Create the custom dialog.
        dialog = new Dialog<>();
        dialog.setTitle("Wczytaj zmienną");
        dialog.setHeaderText("Wybierz zmienną, do której wczytam wartość.");

// Set the icon (must be included in the project).
//        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create UI
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ChoiceBox name = new ChoiceBox();
        name.setItems(FXCollections.observableArrayList(Utils.getVariableNameList()));
        Label nameErrorMessage = new Label("Wybierz zmienną");
        nameErrorMessage.setVisible(false);
        nameErrorMessage.setTextFill(Color.RED);

        Button indexInput = new Button("Indeks w tablicy");
        indexInput.setVisible(false);
        Label indexLabel = new Label();
        indexLabel.setVisible(false);


        grid.add(new Label("Zmienna:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(nameErrorMessage,1,1);
        grid.add(indexInput, 2, 0);
        grid.add(indexLabel,2,1);

        // Do some validation (using the Java 8 lambda syntax).

        name.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
            if (Utils.variables.get(newValue) instanceof Variables.Array){
                indexInput.setVisible(true);
                indexLabel.setVisible(true);
            } else {
                indexInput.setVisible(false);
                indexLabel.setVisible(false);
            }
            selectedName = newValue.toString();
        });

        indexInput.setOnAction((action)->{
            DialogExpression indexExpression = new DialogExpression(
                    "Zbuduj indeks do zmiennej tablicowej",
                    Utils.getFullExpList(),Utils.getFullOpList());
            Optional<Expressions.Expression> result = indexExpression.showAndAwait();
            if(result.isPresent()){
                index = result.get();
                indexLabel.setText(
                        selectedName+"["+index.toString()+"]");
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

        dialog.getDialogPane().setContent(grid);

        //Prevent confirmation without valid data
        final Button ok = (Button)dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if(name.getSelectionModel().isEmpty()){
                nameErrorMessage.setVisible(true);
                event.consume();
            }
        });

        // Convert the result to a name-type-pair when the OK button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if(index!=null){
                    System.out.println("jest index");
                }
                return new Pair<>(name.getSelectionModel().getSelectedItem().toString(),
                        index);
            }
            return null;
        });
    }


    /**
     * Shows dialog and returns result
     * @return name of variable to read into
     */
    public Optional<Pair<String,Expressions.Expression>> showAndAwait(){
        return dialog.showAndWait();
    }
}
