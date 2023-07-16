package main.interpreter;

import java.io.Serializable;

public class Expressions {
    public static abstract class Expression implements Serializable{
        protected Expression left, right;
        public abstract Number value() throws ArithmeticException;

        public Expression getLeft() {
            return left;
        }

        public void setLeft(Expression left) {
            this.left = left;
        }

        public Expression getRight() {
            return right;
        }

        public void setRight(Expression right) {
            this.right = right;
        }
    }

    public static class Addition extends Expression {

        public Addition(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && right.value() instanceof Integer) {
                return left.value().intValue() + right.value().intValue();
            } else {
                return left.value().doubleValue() + right.value().doubleValue();
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"+"+right.toString()+")";
        }

    }

    public static class Subtraction extends Expression {

        public Subtraction(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && right.value() instanceof Integer) {
                return left.value().intValue() - right.value().intValue();
            } else {
                return left.value().doubleValue() - right.value().doubleValue();
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"-"+right.toString()+")";
        }

    }

    public static class Multiplication extends Expression {

        public Multiplication(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && right.value() instanceof Integer) {
                return left.value().intValue() * right.value().intValue();
            } else {
                return left.value().doubleValue() * right.value().doubleValue();
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"*"+right.toString()+")";
        }

    }

    public static class Division extends Expression {

        public Division(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && right.value() instanceof Integer) {
                return left.value().intValue() / right.value().intValue();
            } else {
                return left.value().doubleValue() / right.value().doubleValue();
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"/"+right.toString()+")";
        }

    }

    public static class Modulo extends Expression {

        public Modulo(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && right.value() instanceof Integer) {
                return left.value().intValue() % right.value().intValue();
            } else {
                throw new ArithmeticException("Błąd! Złe wartości dla operatora modulo.");
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"%"+right.toString()+")";
        }
    }

    public static class MathConstant extends Expression{
        private String stringValue;
        private String type;
        private Number returnValue;

        public MathConstant(String type, String stringValue) {
            this.type = type;
            this.stringValue = stringValue;
        }

        public Number value() {
            try {
                switch (type) {
                    case Utils.VARIABLE_INT:
                        returnValue = Integer.parseInt(stringValue);
                        break;
                    case Utils.VARIABLE_DOUBLE:
                        returnValue = Double.parseDouble(stringValue);
                        break;
                    case Utils.VARIABLE_CHAR:
                        returnValue = (int) stringValue.charAt(0);
                        break;
                }
            } catch (ClassCastException e){
                System.out.println("Błąd! Zły typ.");
            }
            return returnValue;
        }

        @Override
        public String toString() {
            return stringValue;
        }

    }

    public static class MathVariable extends Expression {
        private String name;

        public MathVariable(String name) {
            this.name=name;
        }

        @Override
        public Number value() {
            Number var=null;
            try {
                var = Utils.variables.get(name).getNumericValue();
            } catch(ClassCastException e) {
                System.out.println("Błąd! Zły typ zmiennej.");
            }
            return var;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

    }

    public static class OperatorAt extends Expression {
        private String name;
        private Expression index;

        public OperatorAt(String name, Expression index) {
            this.name = name;
            this.index = index;
        }

        @Override
        public Number value() throws ArithmeticException {
            Number var=null;
            try {
                var = ((Variables.Array)Utils.variables.get(name))
                        .getElement(index.value().intValue()).getNumericValue();
            } catch(ClassCastException e) {
                System.out.println("Błąd! Zły typ zmiennej.");
            }
            return var;
        }

        @Override
        public String toString() {
            return name+"["+index.toString()+"]";
        }

        public String getName() {
            return name;
        }

        public Expression getIndex() {
            return index;
        }
    }

    public static class And extends Expression{

        public And(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value().doubleValue()!=0 && right.value().doubleValue()!=0) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"&&"+right.toString()+")";
        }

    }

    public static class Or extends Expression {
        private Expression left, right;

        public Or(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value().doubleValue()!=0 || right.value().doubleValue()!=0) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"||"+right.toString()+")";
        }


    }

    public static class Not extends Expression {
        Expression expression;

        public Not(Expression expression) {
            this.expression = expression;
        }

        @Override
        public Number value() throws ArithmeticException {
            return (expression.value().intValue()==0)?1:0;
        }
    }

    public static class Equal extends Expression {

        public Equal(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value().equals(right.value())) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"=="+right.toString()+")";
        }
    }

    public static class NotEqual extends Expression {
        private Expression left, right;

        public NotEqual(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(!left.value().equals(right.value())) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"!="+right.toString()+")";
        }


    }

    public static class Greater extends Expression {

        public Greater(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && left.value() instanceof Integer) {
                return (left.value().intValue()>right.value().intValue())?1:0;
            } else {
                return (left.value().doubleValue()>right.value().doubleValue())?1:0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+">"+right.toString()+")";
        }


    }

    public static class GreaterOrEqual extends Expression {
        private Expression left, right;

        public GreaterOrEqual(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && left.value() instanceof Integer) {
                return (left.value().intValue()>=right.value().intValue())?1:0;
            } else {
                return (left.value().doubleValue()>=right.value().doubleValue())?1:0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+">="+right.toString()+")";
        }


    }

    public static class Less extends Expression {

        public Less(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && left.value() instanceof Integer) {
                return (left.value().intValue()<right.value().intValue())?1:0;
            } else {
                return (left.value().doubleValue()<right.value().doubleValue())?1:0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"<"+right.toString()+")";
        }

    }

    public static class LessOrEqual extends Expression {

        public LessOrEqual(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public Number value() {
            if(left.value() instanceof Integer && left.value() instanceof Integer) {
                return (left.value().intValue()<=right.value().intValue())?1:0;
            } else {
                return (left.value().doubleValue()<=right.value().doubleValue())?1:0;
            }
        }

        @Override
        public String toString() {
            return "("+left.toString()+"<="+right.toString()+")";
        }


    }

    public static class BoolConstant extends Expression {
        private boolean value;

        public BoolConstant(boolean value) {
            this.value = value;
        }

        public Number value() {
            return value?1:0;
        }

        @Override
        public String toString() {
            return value?"true":"false";
        }

    }
}
