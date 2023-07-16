package main;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import main.interpreter.Expressions;
import main.interpreter.Utils;

import java.awt.event.ActionEvent;
import java.util.Optional;

public class DialogIf {
    private Dialog<Expressions.Expression> dialog;
    private Expressions.Expression condition;

    public DialogIf() {
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
        grid.setPadding(new Insets(20, 20, 10, 10));

        Button conditionButton = new Button("Wprowadź warunek");
        Label conditionLabel = new Label();

        grid.add(conditionButton,0,0);
        grid.add(conditionLabel,1,0);

        dialog.getDialogPane().setContent(grid);
        dialog.setResizable(true);

        conditionButton.setOnAction((action)->{
            DialogExpression dialog = new DialogExpression(
                    "Zbuduj warunek", Utils.getFullExpList(),
                    Utils.getFullOpList());
            Optional<Expressions.Expression> result = dialog.showAndAwait();
            if(result.isPresent()){
                condition = result.get();
                conditionLabel.setText(condition.toString());
            }
        });

        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    if(condition==null){
                        event.consume();
                    }
                }
        );

        dialog.setResultConverter((buttonType)->{
            if(buttonType==ButtonType.OK){
                if(condition!=null){
                    return condition;
                }
            }
            return null;
        });

    }

    /**
     * Shows dialog and returns result
     * @return condition expression for IfOperation
     */
    public Optional<Expressions.Expression> showAndAwait(){
        return dialog.showAndWait();
    }
}
