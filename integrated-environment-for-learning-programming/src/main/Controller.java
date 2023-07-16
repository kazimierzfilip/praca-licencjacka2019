package main;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Pair;
import main.interpreter.*;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    public static Controller controller;

    /**
     * TreeView displaying program operations
     */
    @FXML
    protected TreeView<Operations.Composition> treeViewCode;

    /**
     * Root element of treeViewCode
     */
    private TreeItem<Operations.Composition> rootCode;

    /**
     * Label displaying interpreter messages
     */
    @FXML
    protected Label labelInterpreter;

    /**
     * Label displaying program's standard output
     */
    @FXML
    protected Label labelStdOutput;

    /**
     * TreeTable displaying variables content
     */
    @FXML
    protected TreeTableView<Variables.Variable> treeTableVariables;

    /**
     * rootVariable for treeTableVariables rows
     */
    private TreeItem<Variables.Variable> rootVariable;

    /**
     * User-built program
     */
    private Program program = new Program();

    /**
     * Number of currently interpreted line starting from 1 ..
     */
    private int currentLine;

    /**
     * TreeItem to which user dragged new operation
     */
    private TreeItem<Operations.Composition> focusedParentBlock;

    /**
     * Composition in TreeItem to which user dragged new operation
     */
    private Operations.Composition parent;

    /**
     * File to save user program
     */
    private File saveFile;


    /**
     * Refreshes list with code instructions and table with variables
     */
    public void refresh(){
        treeViewCode.refresh();
        treeTableVariables.refresh();
    }

    /**
     * Prints message to output window.
     * @param message to be displayed.
     */
    public void stdOutput(String message){
        labelStdOutput.setText(labelStdOutput.getText()+message);
    }

    /**
     * Prints message to interpreter messages window.
     * @param message to be displayed.
     */
    public void interpreterMessage(String message){
        labelInterpreter.setText(message);
    }

    /**
     * Prints message to standard error window.
     * @param message to be displayed.
     */
    public void stdError(String message){
        System.out.println(message);
    }

    /**
     * Handles action after click on buttonExport.
     * Creates and shows dialog, saves code in C++ form in specified file.
     */
    @FXML
    protected void handleExportButton(){
        System.out.println("handling button export");
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Kod C++ (*.cpp)", "*.cpp");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(labelStdOutput.getScene().getWindow());
        if (file != null) {
            export(file);
        }
    }

    private void export(File file){
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String cppCode = "#include<iostream>\n\nusing namespace std;\nint main(){\n";
            cppCode += program.getCode().toCppCode();
            cppCode += "\n}";
            bufferedWriter.write(cppCode);
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles action after click on buttonSave.
     * Creates and shows dialog, saves code in specified file.
     */
    @FXML
    protected void handleSaveButton(){
        System.out.println("handling button save");
        if(saveFile == null){
            FileChooser fileChooser = new FileChooser();

            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zapis programu edukacyjnego (*.zsnp)", "*.zsnp");
            fileChooser.getExtensionFilters().add(extFilter);

            //Show save file dialog
            saveFile = fileChooser.showSaveDialog(labelStdOutput.getScene().getWindow());
        }
        if (saveFile != null) {
            save();
        }
    }

    /**
     * Saves program tree to file
     */
    private void save(){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(program.getCode());
            objectOutputStream.writeObject(Utils.variables);
            fileOutputStream.close();
            objectOutputStream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles action after click on buttonOpen.
     * Creates and shows dialog, opens code from specified file.
     */
    @FXML
    protected void handleOpenButton(){
        System.out.println("handling button open");
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zapis programu edukacyjnego (*.zsnp)", "*.zsnp");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        saveFile = fileChooser.showOpenDialog(labelStdOutput.getScene().getWindow());

        if (saveFile != null) {
           open();
        }
    }

    /**
     * Open program tree from file
     */
    private void open(){
        try {
            FileInputStream fileInputStream = new FileInputStream(saveFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            program.setCode((Operations.Composition)objectInputStream.readObject());
            rootCode.getChildren().clear();
            Utils.variables = (Map<String, Variables.Variable>) objectInputStream.readObject();
            handleStopButton();
            drawCodeTree(rootCode,program.getCode());
            drawVariablesTable();
            fileInputStream.close();
            objectInputStream.close();
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * Interprets the whole code at once.
     */
    @FXML
    protected void handleRunButton(){
        handleStopButton();
        while (program.hasNext()){
            handleNextButton();
        }
        System.out.println("interpreting program");
    }

    /**
     * Interprets only one instruction at a time
     */
    @FXML
    protected void handleNextButton(){
        if(program.hasNext()) {
            System.out.println("next operation");
            refresh();
            saveState();
            program.next();

        } else {
            System.out.println("program end, no next item");
            handleStopButton();
        }
        refresh();
    }

    /**
     * Retrieves previous state and interprets previous instruction
     */
    @FXML
    public void handlePreviousButton(){
        if(program.hasPrevious()) {
            System.out.println("previous operation");
            retrieveState();
            program.previous();

        } else {
            System.out.println("program end, no previous operation");
            handleStopButton();
        }
        refresh();
    }

    /**
     * Stops interpreting program, next call to next() method will execute 1st operation
     */
    @FXML
    protected void handleStopButton(){
        program.stop();
        clearUI();
        Utils.stdOutStates = new Stack<>();
        Utils.variablesStates = new Stack<>();
    }

    /**
     * Clears labels for interpreter message and standard output.
     * Resets variables values to default
     */
    private void clearUI(){
        interpreterMessage("");
        labelStdOutput.setText("");
        //go through variables and set default value
        Utils.variables.forEach((k,v)->v.setDefaultValue());
        refresh();
    }


    /**
     * Handles action after click on buttonAssign.
     * Creates and shows dialog, adds operation constructed from it's results.
     */
    @FXML
    protected void handleButtonAssign(){
        System.out.println("handling button assign");

        DialogAssign dialogAssign = new DialogAssign();
        Optional<Triplet<Variables.Variable,Expressions.Expression,Expressions.OperatorAt>> result = dialogAssign.showAndAwait();
        if(result.isPresent()){
            Operations.Composition newCodeBlock;
            System.out.println(result.get().getFirst()+" "+result.get().getSecond());
            Variables.Variable var = result.get().getFirst();
            Expressions.Expression value = result.get().getSecond();
            Expressions.OperatorAt operatorAt = result.get().getThird();
            if(operatorAt!=null){
                newCodeBlock = program.addOperation(new Operations.AssignValueOperation(operatorAt, value), parent);
            } else {
                newCodeBlock = program.addOperation(new Operations.AssignValueOperation(var.getName(), value), parent);
            }
            addToCodeTree(focusedParentBlock,newCodeBlock);
        }
        resetFocusedBlock();
    }

    /**
     * Handles action after click on buttonCin.
     * Creates and shows dialog, adds operation constructed from it's results.
     */
    @FXML
    protected void handleButtonCin(){
        System.out.println("handling button cin");
        DialogCin dialog = new DialogCin();
        Optional<Pair<String,Expressions.Expression>> result = dialog.showAndAwait();
        result.ifPresent(nameIndex -> {
            Operations.Composition newCodeBlock;
            if(nameIndex.getValue()==null){
                newCodeBlock=program.addOperation(new Operations.CinOperation(nameIndex.getKey()),parent);
            } else {
                newCodeBlock=program.addOperation(new Operations.CinOperation(nameIndex.getKey(),nameIndex.getValue()),parent);
            }

            addToCodeTree(focusedParentBlock,newCodeBlock);
            refresh();
            System.out.println("name=" + nameIndex.getKey()+" index="+nameIndex.getValue());
        });
        resetFocusedBlock();
    }

    /**
     * Handles action after click on buttonCout.
     * Creates and shows dialog, adds operation constructed from it's results.
     */
    @FXML
    protected void handleButtonCout(){
        System.out.println("handling button cout");
        DialogCout dialog = new DialogCout();

        Optional<Triplet<String,Object,Object>> result = dialog.showAndAwait();

        result.ifPresent(whatValueNewLine -> {
            String what = whatValueNewLine.getFirst();
            Boolean newLine = (Boolean) whatValueNewLine.getThird();
            Operations.Composition newCodeBlock=null;
            if(what.equals(Utils.VARIABLE_STRING) && !whatValueNewLine.getSecond().equals("")){
                String value = (String)whatValueNewLine.getSecond();
                newCodeBlock = program.addOperation(new Operations.CoutOperation(value),parent);
            } else if(what.equals(Utils.VARIABLE)){
                Expressions.MathVariable mathVariable = (Expressions.MathVariable) whatValueNewLine.getSecond();
                Variables.Variable variable = Utils.variables.get(mathVariable.getName());
                newCodeBlock = program.addOperation(new Operations.CoutOperation(variable),parent);
            } else if(what.equals(Utils.EXPRESSION)){
                Expressions.Expression exp = (Expressions.Expression)whatValueNewLine.getSecond();
                newCodeBlock = program.addOperation(new Operations.CoutOperation(exp),parent);
            }
            addToCodeTree(focusedParentBlock,newCodeBlock);

            if(newLine) {
                newCodeBlock = program.addOperation(new Operations.CoutOperation(),parent);
                addToCodeTree(focusedParentBlock,newCodeBlock);
            }
            refresh();
        });
        resetFocusedBlock();
    }

    /**
     * Handles action after click on buttonComment.
     * Creates and shows dialog, adds operation constructed from it's results.
     */
    @FXML
    protected void handleButtonComment(ActionEvent event){
        System.out.println("handling button comment");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Komentarz");
        dialog.setHeaderText("Tworzenie komentarza");
        dialog.setContentText("Wprowadź swój komentarz:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            System.out.println("adding comment: "+result.get());
            Operations.CommentOperation operation = new Operations.CommentOperation(result.get());
            Operations.Composition addedComposition = program.addOperation(operation,parent);
            addToCodeTree(focusedParentBlock,addedComposition);
            refresh();
        }
        resetFocusedBlock();
    }

    /**
     * Handles action after click on buttonDefine.
     * Creates and shows dialog, adds operation constructed from it's results.
     */
    @FXML
    protected void handleButtonDefine(){
        System.out.println("handling button define");
        DialogDefine dialog = new DialogDefine();
        Optional<Triplet<String, String, Integer>> result = dialog.showAndAwait();

        result.ifPresent(nameTypeLen -> {
            Operations.DefineVariableOperation operation;
            Operations.Composition addedComposition;
            if(nameTypeLen.getThird()>0){
                operation = new Operations.DefineVariableOperation(nameTypeLen.getFirst(), nameTypeLen.getSecond(), nameTypeLen.getThird());
                addedComposition = program.addOperation(operation,parent);
            } else {
                operation = new Operations.DefineVariableOperation(nameTypeLen.getFirst(), nameTypeLen.getSecond());
                addedComposition = program.addOperation(operation,parent);
            }

            drawVariablesTable();

            //add to Code Tree View
            addToCodeTree(focusedParentBlock,addedComposition);

            refresh();
            System.out.println("name=" + nameTypeLen.getFirst() + ", type=" + nameTypeLen.getSecond()+" len="+nameTypeLen.getThird());
        });
        resetFocusedBlock();
    }

    /**
     * Handles action after click on buttonIf.
     * Creates and shows dialog, adds operation constructed from it's results.
     */
    @FXML
    protected void handleButtonIf(){
        System.out.println("handling button if");
        DialogIf dialogIf = new DialogIf();
        Optional<Expressions.Expression> result = dialogIf.showAndAwait();
        result.ifPresent((condition)->{
            Operations.Composition addedComposition;
            System.out.println(condition);
            Operations.IfOperation ifOperation = new Operations.IfOperation(condition);
            addedComposition = program.addOperation(ifOperation,parent);
            ifOperation.getThenOperations().setParent(addedComposition);
            ifOperation.getElseOperations().setParent(addedComposition);
            addToCodeTree(focusedParentBlock,addedComposition);
        });
        resetFocusedBlock();
    }

    /**
     * Handles action after click on buttonWhile.
     * Creates and shows dialog, adds operation constructed from it's results.
     */
    @FXML
    protected void handleButtonWhile(){
        System.out.println("handling button while");
        DialogWhile dialogWhile = new DialogWhile();
        Optional<Expressions.Expression> result = dialogWhile.showAndWait();
        result.ifPresent((condition)->{
            Operations.Composition addedComposition;
            System.out.println(condition);
            Operations.WhileOperation whileOperation = new Operations.WhileOperation(condition);
            addedComposition = program.addOperation(whileOperation,parent);
            whileOperation.getOperations().setParent(addedComposition);
            //add to Code Tree View
            addToCodeTree(focusedParentBlock,addedComposition);
        });
        resetFocusedBlock();
    }

    /**
     * Resets focusedParentBlock to root (indicates to add operation to root)
     */
    private void resetFocusedBlock(){
        System.out.println("focus block reset");
        focusedParentBlock = rootCode;
        parent = null;
    }

    /**
     * Adds item with composition to code tree
     * @param codeBlock composition to be added
     * @param parent parent treeItem for new treeItem
     */
    private TreeItem<Operations.Composition> addToCodeTree(TreeItem<Operations.Composition> parent, Operations.Composition codeBlock) {
        if(codeBlock!=null) {
            System.out.println("adding to code tree");
            Operations.Operation operation = codeBlock.getOperation();
            if (operation != null) {
                //add item to view
                TreeItem<Operations.Composition> node = new TreeItem<>(codeBlock);
                if (parent == null) {
                    parent = rootCode;
                }
                parent.getChildren().add(node);
                parent.setExpanded(true);
                if (operation instanceof Operations.IfOperation) {
                    TreeItem<Operations.Composition> elseNode = new TreeItem<>(
                            new Operations.Composition(
                                    new Operations.ElseOperation((Operations.IfOperation) operation), null));
                    parent.getChildren().add(elseNode);
                }
                return node;
            }
        }
        return null;
    }

    /**
     * Constructs whole code tree
     */
    private void drawCodeTree(TreeItem<Operations.Composition> parent, Operations.Composition codeBlock){
        Operations.Operation operation = codeBlock.getOperation();
        Operations.Composition nextBlock = codeBlock.getNextComposition();
        if(operation!=null) {
            //add item to view
            TreeItem<Operations.Composition> newNode = addToCodeTree(parent, codeBlock);

            //add sub-items to view if exist
            if (operation instanceof Operations.IfOperation) {
                Operations.IfOperation ifOperation = (Operations.IfOperation) operation;
                drawCodeTree(newNode, ifOperation.getThenOperations());
                drawCodeTree(newNode.nextSibling(), ifOperation.getElseOperations());
            } else if (operation instanceof Operations.WhileOperation) {
                drawCodeTree(newNode, ((Operations.WhileOperation) operation).getOperations());
            }
        }
        //add items from next blocks
        if(nextBlock!=null) {
            drawCodeTree(parent,nextBlock);
        }
    }

    /**
     * Constructs whole variables table
     */
    private void drawVariablesTable(){
        System.out.println("drawing Variables Table");
        rootVariable.getChildren().clear();
        Utils.variables.forEach((name, var)->{
            TreeItem<Variables.Variable> node = new TreeItem<>(var);
            rootVariable.getChildren().add(node);
            if(var instanceof Variables.Array){
                for (Variables.Variable arrEl : ((Variables.Array) var).getArray()) {
                    TreeItem<Variables.Variable> arrNode = new TreeItem<>(arrEl);
                    node.getChildren().add(arrNode);
                }
            }
        });
        refresh();
    }

    private void serializeObjects(Object... object){
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("state.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            for(Object o: object)
                out.writeObject(o);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public Object[] getDeserializedObjects(){
        Object[] objects=new Object[2];
        try {
            FileInputStream fileIn = new FileInputStream("state.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            objects[0] = in.readObject();
            objects[1] = in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("class not found");
            c.printStackTrace();
        }
        return objects;
    }

    /**
     * Saves state of the program
     */
    public void saveState(){
        System.out.println("saving state");
        serializeObjects(Utils.variables, labelStdOutput.getText());
        Object[] deserializedObjects = getDeserializedObjects();
        Utils.variablesStates.push((Map<String, Variables.Variable>)deserializedObjects[0]);
        Utils.stdOutStates.push((String)deserializedObjects[1]);
    }

    public void retrieveState(){
        System.out.println("retrieving state");
        clearUI();
        Utils.variablesStates.pop();
        Utils.stdOutStates.pop();
        Utils.variables = Utils.variablesStates.peek();
        labelStdOutput.setText(Utils.stdOutStates.peek());
        drawVariablesTable();
    }

    private void deleteOperationAtTreeItem(TreeItem<Operations.Composition> item){
        System.out.println("deleting operation");
        if(item.getValue() != program.getCurrentComposition()){
            if(item.getValue().getOperation() instanceof Operations.DefineVariableOperation){
                String variableName = ((Operations.DefineVariableOperation)item.getValue().getOperation()).getVariableName();
                TreeItem<Variables.Variable> variableToDelete = rootVariable.getChildren()
                        .stream()
                        .filter(variable -> variable.getValue().getName().equals(variableName))
                        .findFirst()
                        .orElse(null);
                //delete variable from memory
                Utils.deleteVariable(variableToDelete.getValue());
                //delete from variables table
                rootVariable.getChildren().remove(variableToDelete);
            } else if(item.getValue().getOperation() instanceof Operations.IfOperation){
                item.getParent().getChildren().remove(item.nextSibling());
            } else if(item.getValue().getOperation() instanceof Operations.ElseOperation) {
                item.getParent().getChildren().removeAll(item.getChildren());
                refresh();
                return;
            } else if(item.getValue().getOperation() instanceof Operations.WhileOperation){

            }
            program.deleteOperationFromComposition(item.getValue());
            //delete from code tree
            item.getParent().getChildren().remove(item);
            refresh();
        }
    }

    private void editOperationAtTreeItem(TreeItem<Operations.Composition> item){
        System.out.println("editing operation");
        //start edit dialog
        //insert edited operation
        if(item.getValue().getOperation() instanceof Operations.AssignValueOperation){
            DialogAssign dialogAssign = new DialogAssign();
            Optional<Triplet<Variables.Variable,Expressions.Expression,Expressions.OperatorAt>> result = dialogAssign.showAndAwait();
            if(result.isPresent()){
                Variables.Variable var = result.get().getFirst();
                Expressions.Expression value = result.get().getSecond();
                Expressions.OperatorAt operatorAt = result.get().getThird();
                Operations.AssignValueOperation editedOperation;
                if(operatorAt!=null){
                    editedOperation = new Operations.AssignValueOperation(operatorAt,value);
                } else {
                    editedOperation = new Operations.AssignValueOperation(var.getName(), value);
                }
                program.editOperationInComposition(item.getValue(),editedOperation);
            }
        } else if(item.getValue().getOperation() instanceof Operations.CinOperation){
            DialogCin dialog = new DialogCin();
            Optional<Pair<String,Expressions.Expression>> result = dialog.showAndAwait();
            result.ifPresent(nameIndex -> {
                Operations.CinOperation editedOperation = null;
                if(nameIndex.getValue()==null){
                    editedOperation = new Operations.CinOperation(nameIndex.getKey());
                } else {
                    editedOperation = new Operations.CinOperation(nameIndex.getKey(),nameIndex.getValue());
                }
                program.editOperationInComposition(item.getValue(),editedOperation);
            });
        } else if(item.getValue().getOperation() instanceof Operations.CoutOperation){
            DialogCout dialog = new DialogCout();

            Optional<Triplet<String,Object,Object>> result = dialog.showAndAwait();

            result.ifPresent(whatValueNewLine -> {
                String what = whatValueNewLine.getFirst();
                Operations.CoutOperation editedOperation = null;
                if(what.equals(Utils.VARIABLE_STRING)){
                    String value = (String)whatValueNewLine.getSecond();
                    editedOperation = new Operations.CoutOperation(value);
                } else if(what.equals(Utils.VARIABLE)){
                    Expressions.MathVariable mathVariable = (Expressions.MathVariable) whatValueNewLine.getSecond();
                    Variables.Variable variable = Utils.variables.get(mathVariable.getName());
                    editedOperation = new Operations.CoutOperation(variable);
                } else if(what.equals(Utils.EXPRESSION)){
                    Expressions.Expression exp = (Expressions.Expression)whatValueNewLine.getSecond();
                    editedOperation = new Operations.CoutOperation(exp);
                }
                program.editOperationInComposition(item.getValue(),editedOperation);
            });
        } else if(item.getValue().getOperation() instanceof Operations.CommentOperation){
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Komentarz");
            dialog.setHeaderText("Tworzenie komentarza");
            dialog.setContentText("Wprowadź swój komentarz:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                System.out.println("adding comment: "+result.get());
                Operations.CommentOperation editedOperation = new Operations.CommentOperation(result.get());
                program.editOperationInComposition(item.getValue(),editedOperation);
            }
        } else if(item.getValue().getOperation() instanceof Operations.DefineVariableOperation){
            Operations.DefineVariableOperation defineVariableOperation = (Operations.DefineVariableOperation) item.getValue().getOperation();
            DialogDefine dialog = new DialogDefine();
            Optional<Triplet<String, String, Integer>> result = dialog.showAndAwait();
            result.ifPresent(nameTypeLen -> {
                Operations.DefineVariableOperation editedOperation;

                if (nameTypeLen.getThird() > 0) {
                    editedOperation = new Operations.DefineVariableOperation(nameTypeLen.getFirst(), nameTypeLen.getSecond(), nameTypeLen.getThird());
                } else {
                    editedOperation = new Operations.DefineVariableOperation(nameTypeLen.getFirst(), nameTypeLen.getSecond());
                }
                Utils.deleteVariable(Utils.variables.get(defineVariableOperation.getVariableName()));
                program.editOperationInComposition(item.getValue(), editedOperation);
                drawVariablesTable();
            });
        } else if(item.getValue().getOperation() instanceof Operations.IfOperation){
            Operations.IfOperation ifOperation = (Operations.IfOperation)item.getValue().getOperation();
            DialogIf dialogIf = new DialogIf();
            Optional<Expressions.Expression> result = dialogIf.showAndAwait();
            result.ifPresent((condition)->{
                Operations.IfOperation editedOperation = new Operations.IfOperation(condition,ifOperation.getThenOperations(),ifOperation.getElseOperations());
                program.editOperationInComposition(item.getValue(), editedOperation);
            });
        } else if(item.getValue().getOperation() instanceof Operations.WhileOperation){
            Operations.WhileOperation whileOperation = (Operations.WhileOperation)item.getValue().getOperation();
            DialogWhile dialogWhile = new DialogWhile();
            Optional<Expressions.Expression> result = dialogWhile.showAndWait();
            result.ifPresent((condition)->{
                Operations.WhileOperation editedOperation = new Operations.WhileOperation(condition,whileOperation.getOperations());
                program.editOperationInComposition(item.getValue(), editedOperation);
            });
        }
        refresh();
    }

    @FXML
    protected void onDragDetected(MouseEvent mouseEvent){
        mouseEvent.setDragDetect(true);
        System.out.println("drag detected");
        Button source = (Button)mouseEvent.getSource();
        Dragboard db = source.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(source.getText());
        db.setContent(content);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = this;
        currentLine = 0;
        saveFile = null;

        //treeViewCode displaying operations
        rootCode = new TreeItem<>(new Operations.Composition());
        treeViewCode.setRoot(rootCode);
        treeViewCode.setShowRoot(false);

        resetFocusedBlock();

        //treeViewCode CellFactory
        Callback<TreeView<Operations.Composition>, TreeCell<Operations.Composition>> cellFactory = new Callback<TreeView<Operations.Composition>, TreeCell<Operations.Composition>>() {
            @Override
            public TreeCell<Operations.Composition> call(TreeView<Operations.Composition> param) {
                return new TreeCell<Operations.Composition>(){
                    @Override protected void updateItem(Operations.Composition item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText((getIndex() + 1) + ".\t" + item.getOperation().toString());

                            setGraphicTextGap(10);
                            DebuggerArrow debuggerArrow = new DebuggerArrow();
                            setGraphic(debuggerArrow);

                            ContextMenu codeContextMenu = new ContextMenu();
                            MenuItem edit = new MenuItem("Edytuj");
                            MenuItem delete = new MenuItem("Usuń");
                            codeContextMenu.getItems().addAll(edit,delete);
                            delete.addEventHandler(ActionEvent.ACTION,
                                    (action) -> deleteOperationAtTreeItem(getTreeItem()));
                            edit.addEventHandler(ActionEvent.ACTION,
                                    (action) -> editOperationAtTreeItem(getTreeItem()));
                            setContextMenu(codeContextMenu);

                            String tooltipString;
                            try {
                                tooltipString = resources.getString(item.getOperation().getClass().getName());
                            } catch (MissingResourceException e){
                                tooltipString="";
                            }
                            Tooltip operationTooltip = new Tooltip(tooltipString);
                            setTooltip(operationTooltip);

                            if(program.getCurrentComposition() == item){
                                debuggerArrow.setVisible(true);
                            } else {
                                debuggerArrow.setVisible(false);
                            }

                            setOnDragOver(event -> {
                                if (event.getGestureSource() != item &&
                                        event.getDragboard().hasString()) {
                                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                                    event.consume();
                                }
                            });

                            setOnDragDropped(event -> {
                                if(item.getOperation() instanceof Operations.IfOperation){
                                    focusedParentBlock=getTreeItem();
                                    parent=((Operations.IfOperation) item.getOperation()).getThenOperations();
                                } else if(item.getOperation() instanceof Operations.ElseOperation){
                                    focusedParentBlock=getTreeItem();
                                    parent=((Operations.ElseOperation) item.getOperation()).getElseOperations();
                                } else if(item.getOperation() instanceof Operations.WhileOperation) {
                                    focusedParentBlock=getTreeItem();
                                    parent=((Operations.WhileOperation)item.getOperation()).getOperations();
                                } else {
                                    resetFocusedBlock();
                                }
                                System.out.println("Event on Target: mouse drag released, "+getIndex());
                                Button button = (Button)event.getGestureSource();
                                Platform.runLater(()->button.getOnAction().handle(null));
                                event.setDropCompleted(true);
                                event.consume();
                            });
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
            }
        };
        treeViewCode.setCellFactory(cellFactory);

        treeViewCode.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        drawCodeTree(rootCode,program.getCode());

        //Create tree table for displaying variables values
        rootVariable = new TreeItem<>(new Variables.AnonymousVariable("rootVariable"));
        treeTableVariables.setRoot(rootVariable);
        treeTableVariables.setShowRoot(false);

        TreeTableColumn<Variables.Variable,String> varName = new TreeTableColumn<>(resources.getString("variableNameColumn"));
        TreeTableColumn<Variables.Variable,String> varValue = new TreeTableColumn<>(resources.getString("variableValueColumn"));

        treeTableVariables.getColumns().setAll(varName,varValue);

        varName.setCellValueFactory((cellDataFeatures) -> new SimpleStringProperty(
                cellDataFeatures.getValue().getValue().getName()));
        varValue.setCellValueFactory((cellDataFeatures) -> {
            Variables.Variable variable = cellDataFeatures.getValue().getValue();
            if(variable instanceof Variables.Array){
                return new SimpleStringProperty();
            } else {
                return new SimpleStringProperty(variable.getValue().toString());
            }
        });

        drawVariablesTable();
    }
}
