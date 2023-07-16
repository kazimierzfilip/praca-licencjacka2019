package main.interpreter;

import main.Controller;

import java.util.List;
import java.util.Stack;

public class Program {
    private Operations.Composition code;
    private Operations.Composition currentComposition;
    private Operations.Composition none;
    private Stack<Operations.Composition> recursionStack;
    private Stack<Operations.Composition> backwardStack;

    public Program() {
        code = new Operations.Composition();
        none = new Operations.Composition();
        currentComposition = none;
        recursionStack = new Stack<>();
        backwardStack = new Stack<>();
    }

    public Operations.Composition addOperation(Operations.Operation operation){
        return code.add(operation);
    }

    public Operations.Composition addOperation(Operations.Operation operation, Operations.Composition parent){
        if(parent==null){ //add to end
            System.out.println("adding; parent==null");
            return code.add(operation);
        } else { //add to specific parent
            System.out.println("adding; parent!=null");
            Operations.Composition insertedComposition = parent.add(operation);

            return insertedComposition;
        }
    }

    public void deleteOperationFromComposition(Operations.Composition composition){
        if(composition==code){
            //deleting root element
            code = composition.getNextComposition();
            if(code==null){
                code = new Operations.Composition();
            } else {
                code.setParent(null);
            }
        } else {
            Operations.Composition previousComposition = composition.getParent();
            Operations.Composition nextComposition = composition.getNextComposition();
            if(previousComposition.getOperation() instanceof Operations.IfOperation){
                Operations.IfOperation ifOperation = (Operations.IfOperation) previousComposition.getOperation();
                if(composition == ifOperation.getThenOperations()) {
                    ifOperation.setThenOperations(nextComposition);
                } else if(composition == ifOperation.getElseOperations()) {
                    ifOperation.setElseOperations(nextComposition);
                } else {
                    previousComposition.setNextComposition(nextComposition);
                }
            } else if(previousComposition.getOperation() instanceof Operations.WhileOperation
                    && composition == ((Operations.WhileOperation) previousComposition.getOperation()).getOperations()){
                ((Operations.WhileOperation) previousComposition.getOperation()).setOperations(nextComposition);
            } else {
                previousComposition.setNextComposition(nextComposition);
            }
            if(nextComposition!=null) {
                nextComposition.setParent(previousComposition);
            }
        }
        composition.setOperation(null);
    }

    public void editOperationInComposition(Operations.Composition composition, Operations.Operation editedOperation){
        composition.setOperation(editedOperation);
    }

    public void next(){
        if(hasNext()) {
            if (currentComposition == none) {
                currentComposition = code;
                currentComposition.interpretOperation();
            } else {
                backwardStack.push(currentComposition);
                //changing to next composition based on previous operation
                if (currentComposition.getOperation() instanceof Operations.IfOperation) {
                    Operations.IfOperation ifOperation = (Operations.IfOperation) currentComposition.getOperation();
                    recursionStack.push(currentComposition);
                    currentComposition = ifOperation.getConditionalComposition();
                } else if (currentComposition.getOperation() instanceof Operations.WhileOperation) {
                    Operations.WhileOperation whileOperation = (Operations.WhileOperation) currentComposition.getOperation();
                    if(recursionStack.empty() || recursionStack.peek()!=currentComposition)
                        recursionStack.push(currentComposition);
                    if (whileOperation.getOperationsIfCondition() != null) {
                        //beginning loop turn
                        whileOperation.incrementCounter();
                        currentComposition = whileOperation.getOperationsIfCondition();
                    } else {
                        //end of While loop
                        recursionStack.pop();
                        currentComposition = currentComposition.getNextComposition();
                        System.out.println("end while " + currentComposition + " " + recursionStack.empty());

                    }
                } else {
                    currentComposition = currentComposition.getNextComposition();
                }
                //going up from recursion
                while (currentComposition == null && !recursionStack.empty()) {
                    if (recursionStack.peek().getOperation() instanceof Operations.WhileOperation) {
                        currentComposition = recursionStack.pop();
                    } else {
                        currentComposition = recursionStack.pop().getNextComposition();
                    }
                }
                //interpreting operation
                if (currentComposition != null) {
                    currentComposition.interpretOperation();
                }
            }
        }
    }

    public void previous(){
        if(hasPrevious()){
            currentComposition = backwardStack.pop();
            if(currentComposition!=null && currentComposition.getOperation()!=null) {
                //interpret previous operation
                currentComposition.interpretOperation();
            } else {
                Controller.controller.handlePreviousButton();
            }
        }
    }

    public void interpret(){
        code.interpret();
    }

    public void stop(){
        currentComposition = none;
        recursionStack = new Stack<>();
        backwardStack = new Stack<>();
        code.stop();
    }

    public boolean hasNext(){
        return currentComposition!=null;
    }

    public boolean hasPrevious(){
        return !backwardStack.empty();
    }

    public Operations.Composition getCode() {
        return code;
    }

    public void setCode(Operations.Composition code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code.toString();
    }

    public Operations.Composition getCurrentComposition() {
        return currentComposition;
    }
}
