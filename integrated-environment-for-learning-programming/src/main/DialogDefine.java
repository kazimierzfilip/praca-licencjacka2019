package main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import main.interpreter.Utils;
import main.interpreter.Variables;

import java.util.Optional;

public class DialogDefine {
    private Dialog<Triplet<String, String, Integer>> dialog;

    /**
     * Creates dialog for defining variable
     */
    public DialogDefine(){
        // Create the custom dialog.
        dialog = new Dialog<>();
        dialog.setTitle("Zdefiniuj zmienną");
        dialog.setHeaderText("Podaj nazwę zmiennej i jej typ.");

// Set the icon (must be included in the project).
//        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create UI
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Nazwa zmiennej");
        Label nameErrorMessage = new Label();
        nameErrorMessage.setVisible(false);
        nameErrorMessage.setTextFill(Color.RED);
        nameErrorMessage.setText("Niedozwolona nazwa zmiennej");
        ChoiceBox types = new ChoiceBox();
        types.setItems(FXCollections.observableArrayList(
                "int", "double", "char", "int []", "double []", "char []"));
        Label typeErrorMessage = new Label();
        typeErrorMessage.setVisible(false);
        typeErrorMessage.setTextFill(Color.RED);
        typeErrorMessage.setText("Wybierz typ zmiennej");
        TextField lenInput = new TextField();
        lenInput.setVisible(false);
        lenInput.setPromptText("Rozmiar tablicy");

        grid.add(new Label("Nazwa zmiennej:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(nameErrorMessage,1,1);
        grid.add(new Label("Typ zmiennej:"), 0, 2);
        grid.add(types, 1, 2);
        grid.add(typeErrorMessage, 1,3);
        grid.add(lenInput, 2, 2);

        dialog.getDialogPane().setContent(grid);

        //Validation of variable name
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isNameAllowed(newValue)){
                nameErrorMessage.setVisible(false);
            } else {
                nameErrorMessage.setVisible(true);
            }
        });

        //you must specify the type
        types.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue==null){
                typeErrorMessage.setVisible(true);
            } else {
                typeErrorMessage.setVisible(false);
            }
            if(newValue.intValue() >= 3){
                lenInput.setVisible(true);
            } else {
                lenInput.setVisible(false);
            }
        });

        //restrict input to only 5 digits numbers
        lenInput.textProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue.matches(".*\\D.*")){
                lenInput.setText(newValue.replaceAll("\\D",""));
            } else if(newValue.length()>5){
                lenInput.setText(newValue.substring(0,5));
            }
        }));

        // Request focus on the name field by default.
        Platform.runLater(name::requestFocus);

        //Prevent confirmation without valid data
        final Button ok = (Button)dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if(types.getSelectionModel().isEmpty()){
                typeErrorMessage.setVisible(true);
                event.consume();
            } else if(!isNameAllowed(name.getText())) {
                nameErrorMessage.setVisible(true);
                event.consume();
            } else if(lenInput.isVisible() && lenInput.getText().isEmpty()){
                event.consume();
            }
        });

    // Convert the result to a name-type-pair when the OK button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String type = types.getSelectionModel().getSelectedItem().toString();
                type = type.split(" ")[0];
                int len;
                if(!lenInput.getText().isEmpty())
                    len = Integer.parseInt(lenInput.getText());
                else
                    len = 0;
                return new Triplet<>(name.getText(),type, len);
            }
            return null;
        });
    }

    private boolean isNameAllowed(String variableName){
        String declaredVariables = String.join("|",Utils.getVariableNameList());
        return !(variableName.trim().isEmpty()
                || !variableName.matches("^[a-zA-Z_]\\w*")
                || variableName.matches("asm|else|new|this|auto|enum|operator|throw|bool|explicit|private|true|break|export|protected|try|case|extern|public|typedef|catch|false|register|typeid|char|float|reinterpret_cast|typename|class|for|return|union|const|friend|short|unsigned|const_cast|goto|signed|using|continue|if|sizeof|virtual|default|inline|static|void|delete|int|static_cast|volatile|do|long|struct|wchar_t|double|mutable|switch|while|dynamic_cast|namespace|template|And|bitor|not_eq|xor|and_eq|compl|or|xor_eq|bitand|not|or_eq")
                || variableName.matches(declaredVariables));
    }


    /**
     * Shows dialog and returns result
     * @return Pair<name, type> of defined variable
     */
    public Optional<Triplet<String,String, Integer>> showAndAwait(){
        return dialog.showAndWait();
    }
}
