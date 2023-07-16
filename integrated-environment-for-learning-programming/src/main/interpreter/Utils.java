package main.interpreter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.util.Pair;

import java.util.*;

public class Utils {
    public static Map<String, Variables.Variable> variables = new HashMap<>();
    public static Stack<Map<String, Variables.Variable>> variablesStates = new Stack<>();
    public static Stack<String> stdOutStates = new Stack<>();
    public static ObservableList<Operations.Operation> operationsList = FXCollections.observableArrayList();

    public static final String VARIABLE_STRING = "string";
    public static final String VARIABLE_CHAR = "char";
    public static final String VARIABLE_INT = "int";
    public static final String VARIABLE_DOUBLE = "double";

    public static final String VARIABLE = "var";
    public static final String EXPRESSION = "exp";
    public static final String CONSTANT = "const";

    public static void deleteVariable(Variables.Variable variable){
        variables.remove(variable.getName());
    }

    public static List<String> getVariableNameList(){
        List<String> variablesList = new ArrayList<>();
        variables.forEach((k, v) -> variablesList.add(k));
        return variablesList;
    }

    public static List<Variables.Variable> getVariables(){
        List<Variables.Variable> variablesList = new ArrayList<>();
        variables.forEach((k, v) -> variablesList.add(v));
        return variablesList;
    }

    public static ObservableList getFullExpList(){
        ObservableList list = FXCollections.observableArrayList();
        List<String> variables = Utils.getVariableNameList();
        variables.forEach((varName)-> list.add(new Pair<>(Utils.VARIABLE,varName)));
        list.add(new Pair<>("const int",null));
        list.add(new Pair<>("const double",null));
        list.add(new Pair<>("const char",null));
        return list;
    }

    public static ObservableList getFullOpList(){
        return FXCollections.observableArrayList(">", ">=", "<", "<=", "==",
                "!=", "&&", "||", "+", "-", "*", "/", "%", "!", "[]");
    }
}
