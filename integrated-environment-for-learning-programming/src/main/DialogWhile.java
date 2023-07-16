package main;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import main.interpreter.Expressions;
import main.interpreter.Utils;

import java.util.Optional;

public class DialogWhile extends Dialog<Expressions.Expression> {
    private Expressions.Expression condition;

    public DialogWhile(){
        setTitle("Pętla while");
        setHeaderText("Zbuduj pętlę while");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Button conditionButton = new Button("Wprowadź warunek");
        Label conditionLabel = new Label();

        grid.add(conditionButton,0,0);
        grid.add(conditionLabel,1,0);

        getDialogPane().setContent(grid);
        setResizable(true);

        conditionButton.setOnAction((action)->{
            DialogExpression dialog = new DialogExpression(
                    "Zbuduj warunek", Utils.getFullExpList(),
                    Utils.getFullOpList());
            Optional<Expressions.Expression> result = dialog.showAndAwait();
            if(result.isPresent()){
                condition = result.get();
                conditionLabel.setText("while("+condition.toString()+"){}");
            }
        });

        getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    if(condition==null){
                        event.consume();
                    }
                }
        );

        setResultConverter((buttonType)->{
            if(buttonType==ButtonType.OK){
                if(condition!=null){
                    return condition;
                }
            }
            return null;
        });
    }
}
