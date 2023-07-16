package main.interpreter;

import java.io.Serializable;

public class Variables {

    public static abstract class Variable implements Serializable {
        protected String name;
        protected String type;
        protected Object value;

        public Variable(String name, String type) {
            this.name = name;
            this.type = type;
            setDefaultValue();
        }

        public Variable(Variable variable){
            this.name = variable.getName();
            this.type = variable.getType();
            this.value = variable.getValue();
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

        public Number getNumericValue() {return null;}

        public abstract void setValue(Object value) throws IllegalArgumentException;

        public abstract void setDefaultValue();
    }

    public static class AnonymousVariable extends Variable {

        public AnonymousVariable(String name) {
            super(name, "int");
        }

        @Override
        public void setValue(Object value) throws IllegalArgumentException {

        }

        @Override
        public String toString() {
            return name;
        }

        public void setDefaultValue(){
            value = 0;
        }
    }

    public static abstract class Array extends Variable {
        protected Variable[] array;
        protected int size;

        public Array(String name, String type, int size) {
            super(name, type);
            this.size = size;
        }

        @Override
        public void setValue(Object value) throws IllegalArgumentException {
            if (value instanceof Array) {
                this.array = ((Array) value).getArray();
            } else {
                throw new IllegalArgumentException("Błąd! Zmienna jest innego typu niż przypisywana wartość.");
            }
        }

        public Variable[] getArray() {
            return this.array;
        }

        public Variable getElement(int position) throws IllegalArgumentException {
            try {
                return array[position];
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Błąd! Sięgasz poza tablicę.");
            }
        }

        public void setElement(Object value, int position) throws Exception {
            try {
                array[position].setValue(value);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Błąd! Sięgasz poza tablicę.");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Błąd! Przypisywana wartość jest niezgodnego typu.");
            }

        }

        public void setDefaultValue(){}
    }

    public static class CharArray extends Array {

        public CharArray(String name, int size) {
            super(name, "char", size);
            array = new CharVariable[size];
            for (int i = 0; i < size; i++) {
                array[i] = new CharVariable(String.valueOf(i));
            }
        }

    }

    public static class CharVariable extends Variable {

        public CharVariable(String name) {
            super(name, "char");
        }

        public CharVariable(String name, char value) {
            super(name, "char");
            this.value = value;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Integer getNumericValue() {
            return (int)((char)value);
        }

        @Override
        public void setValue(Object value) throws IllegalArgumentException {
            if (value instanceof Character) {
                this.value = value;
                System.out.println("value1: "+this.value.toString());
            } else if (value instanceof Number) {
                this.value = (char)((Number) value).intValue();
                System.out.println("value2: "+this.value.toString());
            } else if (value instanceof Expressions.Expression){
                this.value = (char)(((Expressions.Expression) value).value()).intValue();
                System.out.println("value3: "+this.value.toString());
            } else {
                throw new IllegalArgumentException("Błąd! Zmienna jest innego typu niż przypisywana wartość.");
            }

        }

        @Override
        public String toString() {
            return name;
        }

        public void setDefaultValue(){
            value = '\0';
        }

    }

    public static class DoubleArray extends Array {

        public DoubleArray(String name, int size) {
            super(name, "double", size);
            array = new DoubleVariable[size];
            for(int i=0; i<size; i++) {
                array[i]=new DoubleVariable(String.valueOf(i));
            }
        }

    }

    public static class DoubleVariable extends Variable {
        private double value;

        public DoubleVariable(String name) {
            super(name,"double");
        }

        @Override
        public Double getValue() {
            return value;
        }

        @Override
        public Double getNumericValue() {
            return value;
        }

        @Override
        public void setValue(Object value) throws IllegalArgumentException {
            if(value instanceof Double || value instanceof Integer) {
                this.value = Double.parseDouble(value.toString());
            } else if(value instanceof DoubleVariable) {
                this.value = ((DoubleVariable) value).getValue();
            } else {
                throw new IllegalArgumentException("Błąd! Zmienna jest innego typu niż przypisywana wartość.");
            }
        }

        @Override
        public String toString() {
            return name;
        }

        public void setDefaultValue(){
            value = 0.0;
        }
    }

    public static class IntArray extends Array {

        public IntArray(String name, int size) {
            super(name, "int", size);
            array = new IntVariable[size];
            for(int i=0; i<size; i++) {
                array[i]=new IntVariable(String.valueOf(i));
            }
        }

    }

    public static class IntVariable extends Variable {
        private int value;

        public IntVariable(String name) {
            super(name,"int");
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer getNumericValue() {
            return value;
        }

        @Override
        public void setValue(Object value) throws IllegalArgumentException {
            if(value instanceof Integer) {
                this.value = (int) value;
            } else if(value instanceof IntVariable) {
                this.value = ((IntVariable) value).getValue();
            } else {
                throw new IllegalArgumentException("Błąd! Zmienna jest innego typu niż przypisywana wartość.");
            }
        }

        @Override
        public String toString() {
            return name;
        }

        public void setDefaultValue(){
            value = 0;
        }
    }


//    public static class StringArray extends Array {
//
//        public StringArray(String name, int size) {
//            super(name, "string", size);
//            array = new StringVariable[size];
//            for(int i=0; i<size; i++) {
//                array[i]=new StringVariable(String.valueOf(i));
//            }
//        }
//
//    }
//
//    public static class StringVariable extends Variable {
//
//        public StringVariable(String name) {
//            super(name, "string");
//        }
//
//        public StringVariable(String name, String value) {
//            super(name, "string");
//            this.value = value;
//        }
//
//        @Override
//        public void setValue(Object value) throws IllegalArgumentException {
//            if(value instanceof String) {
//                this.value = (String) value;
//            } else if(value instanceof StringVariable) {
//                this.value = ((StringVariable) value).getValue().toString();
//            } else {
//                throw new IllegalArgumentException("Błąd! Zmienna jest innego typu niż przypisywana wartość.");
//            }
//        }
//
//        @Override
//        public String toString() {
//            return name;
//        }
//
//        public void setDefaultValue(){
//            value = "";
//        }
//
//    }


}
