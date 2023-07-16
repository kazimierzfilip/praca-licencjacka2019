package main.interpreter;

import main.Controller;
import main.DialogCinInput;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

public class Operations {

    static ResourceBundle resourceBundle = ResourceBundle.getBundle("main.interpreter.strings");
    
    public static abstract class Operation implements Serializable {

        public abstract void interpret();

        public String toCppCode(){
            return toString();
        }
    }

    public static class AssignValueOperation extends Operation {
        private String variableName;
        private Object variableValue;
        private Expressions.Expression position = null;
        Variables.Variable variable;

        public AssignValueOperation(String variableName, Object variableValue) {
            this.variableName = variableName;
            this.variableValue = variableValue;
        }

        public AssignValueOperation(Expressions.MathVariable mathVariable, Object variableValue){
            this.variableName = mathVariable.getName();
            this.variableValue = variableValue;
        }

        public AssignValueOperation(Expressions.OperatorAt arrayElement, Object variableValue){
            this.variableName = arrayElement.getName();
            this.position = arrayElement.getIndex();
            this.variableValue = variableValue;
        }

        public AssignValueOperation(String variableName, Expressions.Expression position, Object variableValue) {
            this.variableName = variableName;
            this.variableValue = variableValue;
            this.position = position;
        }

        @Override
        public String toString() {
            variable = Utils.variables.get(variableName);
            String value=variableValue.toString();
            if(variable.getType().equals("char")) {
                value="'"+variableValue.toString()+"'";
            }
            if(position==null) {
                return variableName+" = "+value+";";
            } else {
                return variableName+"["+position.toString()+"]"+" = "+value+";";
            }

        }

        public String toCppCode(){
            return toString();
        }

        @Override
        public void interpret() {
            variable = Utils.variables.get(variableName);
            if (variable != null) {
                if (position == null) {
                    try {
                        if (variableValue instanceof Expressions.Expression) {
                            variable.setValue(((Expressions.Expression) variableValue).value());
                            Controller.controller.interpreterMessage(
                                    MessageFormat.format(resourceBundle.getString("assignExpressionMessage")
                                            ,variable.getName(),variableValue.toString(), variable.getValue().toString()));
                        } else {
                            variable.setValue(variableValue);
                            Controller.controller.interpreterMessage(
                                    MessageFormat.format(resourceBundle.getString("assignMessage")
                                            ,variable.getName(),variableValue.toString()));
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    try {
                        int pos = position.value().intValue();
                        if (variableValue instanceof Expressions.Expression) {
                            ((Variables.Array)variable).getElement(pos).setValue(((Expressions.Expression) variableValue).value());
                        } else {
                            ((Variables.Array)variable).getElement(pos).setValue(variableValue);
                        }
                        Controller.controller.interpreterMessage(MessageFormat.format(resourceBundle.getString("assignToArrayMessage"),
                                variable.getName(), position.toString(), variableValue.toString()));
                    } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } else {
                System.out.println("Błąd! Nie ma takiej zmiennej.");
            }
        }

    }

    public static class CinOperation extends Operation {
        private DialogCinInput dialog;
        private String variableName;
        private String stringValue;
        private Object value;
        private Expressions.Expression index;
        private AssignValueOperation assign;

        public CinOperation(String variableName) {
            this.variableName=variableName;
            this.index = null;
        }

        public CinOperation(String variableName, Expressions.Expression index) {
            this.variableName=variableName;
            this.index = index;
        }

        @Override
        public String toString() {
            if(index==null)
                return "cin >> "+variableName+";";
            else
                return "cin >> "+variableName+"["+index+"]"+";";
        }

        public String toCppCode(){
            return toString();
        }

        @Override
        public void interpret() {
            //Display waiting for message text
            Controller.controller.interpreterMessage(
                    resourceBundle.getString("cinWaitingForInputMessage"));
            //Retrieve variable of given name
            Variables.Variable variable = Utils.variables.get(variableName);

            //invoke response dialog and get result
            dialog = new DialogCinInput(
                    MessageFormat.format(resourceBundle.getString("cinInputDialogMessage"),toString().substring(7)));
            Optional<String> result = dialog.showAndWait();
            //if result is present
            if(result.isPresent()){
                //Get results string value to stringValue
                stringValue = result.get();

                //Display interpreter message
                Controller.controller.interpreterMessage(
                        MessageFormat.format(resourceBundle.getString("cinMessage"),
                                variable.getName(), stringValue));
                //Try parsing value
                try {
                    switch (variable.getType()) {
                        case Utils.VARIABLE_INT:
                            value = Integer.parseInt(stringValue);
                            break;
                        case Utils.VARIABLE_DOUBLE:
                            value = Double.parseDouble(stringValue);
                            break;
                        case Utils.VARIABLE_CHAR:
                            value = stringValue.charAt(0);
                            break;
                        case Utils.VARIABLE_STRING:
                            value = stringValue;
                            break;
                    }

                    //Assign value to variable
                    if(index!=null)
                        //Assign to array
                        assign = new AssignValueOperation(variableName,index,value);
                    else
                        //assign to variable
                        assign = new AssignValueOperation(variableName,value);

                    assign.interpret();
                    Controller.controller.refresh();
                } catch (NumberFormatException e) {
                    System.out.println("Błąd! Podana wartość nie zgadza się z typem zmiennej.");
                } catch(IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                } catch(Exception e) {
                    System.out.println("Błąd! Problem przy pobieraniu wartości.");
                    e.printStackTrace();
                }
            }
        }

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }


    }

    public static class CommentOperation extends Operation {
        private String comment;

        public CommentOperation(String comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return "// "+comment;
        }

        public String toCppCode(){
            return toString();
        }

        @Override
        public void interpret() {}

    }

    public static class Composition extends Operation {
        private Composition parent = null;
        private Operation operation;
        private Composition nextComposition;

        public Composition() {
            this.operation = null;
            this.nextComposition = null;
        }

        public Composition(Operation operation, Composition nextComposition) {
            this.operation = operation;
            this.nextComposition = nextComposition;
        }

        public Composition add(Operation operation){
            if(this.operation == null){
                System.out.println("adding; op==null");
                Utils.operationsList.add(operation);
                this.operation = operation;
                return this;
            } else if(nextComposition == null){
                System.out.println("adding; nextcomp==null");
                nextComposition = new Composition();
                nextComposition.setParent(this);
                return nextComposition.add(operation);
            } else {
                System.out.println("adding; else");
                return nextComposition.add(operation);
            }
        }

        public void interpretOperation(){
            if(operation!=null){
                operation.interpret();
            }
        }

        public void stop(){
        }

        @Override
        public void interpret() {
            if(operation!=null) {
                operation.interpret();
            }
            if(nextComposition!=null) {
                nextComposition.interpret();
            }
        }

        @Override
        public String toString() {
            String s="";
            if(operation != null) {
                s+=operation.toString()+"\n";
            }
            if(nextComposition != null) {
                s+=nextComposition.toString();
            }
            return s;
        }

        public String toCppCode(){
            String s="";
            if(operation != null) {
                s+=operation.toCppCode();
            }
            if(nextComposition != null) {
                s+="\n";
                s+=nextComposition.toCppCode();
            }
            return s;
        }

        public Operation getOperation() {
            return operation;
        }

        public Composition getNextComposition() {
            return nextComposition;
        }

        public Composition getParent() {
            return parent;
        }

        public void setParent(Composition parent) {
            this.parent = parent;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        public void setNextComposition(Composition nextComposition) {
            this.nextComposition = nextComposition;
        }
    }

    public static class CoutOperation extends Operation {
        private Object message;
        private Expressions.Expression position=null;

        public CoutOperation() {
            message=null;
        }

        public CoutOperation(Variables.Variable variable) {
            this.message = variable;
        }

        public CoutOperation(Object message) {
            this.message = message.toString();
        }

        public CoutOperation(Expressions.Expression expression) {
            this.message = expression;
        }

        public CoutOperation(Variables.Variable array, Expressions.Expression position) {
            this.message = array;
            this.position = position;
        }

        @Override
        public void interpret() {
            Controller.controller.interpreterMessage(resourceBundle.getString("coutMessage"));
            if(message==null) {
                //Display empty line on output
                Controller.controller.stdOutput("\n");
            } else {
                //Display interpreter message
                if(message instanceof Variables.Variable) {
                    String name = ((Variables.Variable)message).getName();
                    message = Utils.variables.get(name);
                    if(position != null) {
                        int pos = position.value().intValue();
                        Controller.controller.stdOutput(((Variables.Array)message).getElement(pos).getValue().toString());
                    } else {
                        Controller.controller.stdOutput(((Variables.Variable)message).getValue().toString());
                    }
                } else if (message instanceof String) {
                    Controller.controller.stdOutput(message.toString());
                } else if(message instanceof Expressions.Expression) {
                    Expressions.Expression expression = (Expressions.Expression)message;
                    Controller.controller.stdOutput(expression.value().toString());
                } else {
                    Controller.controller.stdError("Błąd! Problem z wyświetleniem wartości.");
                }
            }

        }

        @Override
        public String toString() {
            String s = "cout << ";
            if(message!=null) {
                if(position != null) {
                    s+= ((Variables.Variable)message).getName()+"["+position.toString()+"]";
                } else if(message instanceof Variables.Variable) {
                    s+= ((Variables.Variable)message).toString();
                } else if (message instanceof String) {
                    s+="\""+message+"\"";
                } else if(message instanceof Expressions.Expression) {
                    s+= ((Expressions.Expression)message).toString();
                }
            } else {
                s+="endl";
            }
            s+=";";
            return s;
        }

        public String toCppCode(){
            return toString();
        }

        public Object getMessage() {
            return message;
        }
    }

    public static class DefineVariableOperation extends Operation {
        private Variables.Variable variable;
        private String variableName;
        private String variableType;
        private int size;

        /**
         * Defines variable of given name and type and puts it into variables list
         * @param variableName
         * @param variableType static string defined in main.interpreter.Utils
         */
        public DefineVariableOperation(String variableName, String variableType) {
            this.variableName=variableName;
            this.variableType=variableType;
            this.size=0;
            switch (variableType) {
//                case Utils.VARIABLE_STRING:
//                    variable = new Variables.StringVariable(variableName);
//                    break;
                case Utils.VARIABLE_CHAR:
                    variable = new Variables.CharVariable(variableName);
                    break;
                case Utils.VARIABLE_INT:
                    variable = new Variables.IntVariable(variableName);
                    break;
                case Utils.VARIABLE_DOUBLE:
                    variable = new Variables.DoubleVariable(variableName);
                    break;
            }
            Utils.variables.put(variable.getName(), variable);
        }

        /**
         * Defines Array of given name and type and puts it into variables list
         * @param variableName
         * @param variableType static string defined in main.interpreter.Utils
         * @param size maximum number of elements in array
         */
        public DefineVariableOperation(String variableName, String variableType, int size) {
            this.variableName=variableName;
            this.variableType=variableType;
            this.size=size;
            switch (variableType) {
//                case Utils.VARIABLE_STRING:
//                    variable = new Variables.StringArray(variableName, size);
//                    break;
                case Utils.VARIABLE_CHAR:
                    variable = new Variables.CharArray(variableName, size);
                    break;
                case Utils.VARIABLE_INT:
                    variable = new Variables.IntArray(variableName, size);
                    break;
                case Utils.VARIABLE_DOUBLE:
                    variable = new Variables.DoubleArray(variableName, size);
                    break;
            }
            Utils.variables.put(variable.getName(), variable);
        }

        @Override
        public String toString() {
            if(size>0) {
                return variableType+" "+variableName+"["+size+"]"+";";
            } else {
                return variableType+" "+variableName+";";
            }
        }

        public String toCppCode(){
            return toString();
        }

        @Override
        public void interpret() {
            if (size > 0) {
                Controller.controller.interpreterMessage(
                        MessageFormat.format(resourceBundle.getString("defineArrayMessage"),
                                variable.getType(), variable.getName()));
            } else {
                Controller.controller.interpreterMessage(
                        MessageFormat.format(resourceBundle.getString("defineMessage"),
                                variable.getType(), variable.getName()));
            }
        }

        public String getVariableName() {
            return variableName;
        }

        public String getVariableType() {
            return variableType;
        }
    }

    public static class IfOperation extends Operation {
        private Expressions.Expression condition;
        private boolean conditionBoolean;
        private Composition thenOperations, elseOperations;

        public IfOperation(Expressions.Expression condition, Composition thenOperations, Composition elseOperations) {
            this.condition = condition;
            this.thenOperations = thenOperations;
            this.elseOperations = elseOperations;
        }

        public IfOperation(Expressions.Expression condition) {
            this.condition = condition;
            this.thenOperations = new Composition();
            this.elseOperations = new Composition();
        }

        @Override
        public String toString() {
            return "if("+condition.toString()+")";
        }

        public String toCppCode(){
            String t="";
            String e="";
            if(thenOperations!=null) {
                t+=" {\n";
                t+=thenOperations.toCppCode();
                t+="\n}";
            }
            if(elseOperations!=null) {
                e+="else {\n";
                e+=elseOperations.toCppCode();
                e+="\n}";
            }
            return "if("+condition.toString()+")"+t+e;
        }

        @Override
        public void interpret() {
            conditionBoolean = (condition.value().doubleValue()!=0);
            if(conditionBoolean) {
                Controller.controller.interpreterMessage(
                        MessageFormat.format(resourceBundle.getString("ifMessage"),
                                condition.toString(), "Warunek prawdziwy. Wykonuję instrukcje warunkowe."));
//                if(thenOperations!=null) {
//                    thenOperations.interpret();
//                }
            } else {
                Controller.controller.interpreterMessage(
                        MessageFormat.format(resourceBundle.getString("ifMessage"),
                                condition.toString(), "Warunek nieprawdziwy. Wykonuję instrukcje alternatywne."));
//                if(elseOperations!=null) {
//                    elseOperations.interpret();
//                }
            }
        }

        public Composition getConditionalComposition(){
            if(conditionBoolean) {
                return thenOperations;
            } else {
                return elseOperations;
            }
        }

        public Composition getThenOperations() {
            return thenOperations;
        }

        public void setThenOperations(Composition thenOperations) {
            this.thenOperations = thenOperations;
        }

        public Composition getElseOperations() {
            return elseOperations;
        }

        public void setElseOperations(Composition elseOperations) {
            this.elseOperations = elseOperations;
        }



    }

    public static class ElseOperation extends Operation {
        private IfOperation ifOperation;

        public ElseOperation(IfOperation parent){
            this.ifOperation = parent;
        }

        @Override
        public String toString() {
            return "else";
        }

        @Override
        public void interpret() {
        }
        public Composition getElseOperations() {
            return ifOperation.getElseOperations();
        }

        public void setElseOperations(Composition elseOperations) {
            ifOperation.setElseOperations(elseOperations);
        }

        public IfOperation getIfOperation() {
            return ifOperation;
        }
    }

    public static class WhileOperation extends Operation {
        private Expressions.Expression condition;
        private Composition operations;
        private int loopCounter=0;

        public WhileOperation(Expressions.Expression condition, Composition operations){
            this.condition=condition;
            this.operations=operations;
        }

        public WhileOperation(Expressions.Expression condition){
            this.condition = condition;
            this.operations = new Composition();
        }

        @Override
        public String toString() {
            return "while("+condition.toString()+")";
        }

        public String toCppCode(){
            return "while("+condition.toString()+")"+"{"+"\n"+operations.toCppCode()+"\n}";
        }

        @Override
        public void interpret() {
            if(condition.value().doubleValue() != 0) {
                Controller.controller.interpreterMessage(
                        MessageFormat.format(resourceBundle.getString("whileMessage"),
                                condition.toString(), "Warunek prawdziwy. Wykonuję instrukcje pętli."));
                //operations.interpret();
            } else {
                Controller.controller.interpreterMessage(
                        MessageFormat.format(resourceBundle.getString("whileMessage"),
                                condition.toString(), "Warunek nieprawdziwy. Nie wykonuję instrukcji pętli."));
            }
        }

        public Composition getOperations() {
            return operations;
        }

        public Composition getOperationsIfCondition() {
            if(condition.value().doubleValue() != 0){
                return operations;
            } else {
                return null;
            }
        }

        public Composition getParent(){
            if(loopCounter>0){
                Composition p = operations;
                while(p.getNextComposition()!=null){
                    p = p.getNextComposition();
                }
                return p;
            } else {
                return null;
            }
        }

        public void setOperations(Composition operations) {
            this.operations = operations;
        }

        public int getLoopCounter() {
            return loopCounter;
        }

        public void setLoopCounter(int loopCounter) {
            this.loopCounter = loopCounter;
        }

        public void decrementCounter(){
            this.loopCounter--;
        }
        public void incrementCounter(){
            this.loopCounter++;
        }
    }

}
