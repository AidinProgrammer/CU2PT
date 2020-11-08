package ir.ac.um.CU2PT.visitor;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiType;

public class JavaNamingMethods extends JavaRecursiveElementVisitor {

    private int numberOfMethods;
    private int numberOfAsserts;
    private int numberOfNewExpression;

    public JavaNamingMethods(int numberOfMethods, int numberOfAsserts, int numberOfNewExpression) {
        this.numberOfMethods = numberOfMethods;
        this.numberOfAsserts = numberOfAsserts;
        this.numberOfNewExpression = numberOfNewExpression;
    }

    private String methodName;
    private boolean flagLessThanTwoArgs = false;

    @Override
    public void visitMethodCallExpression(com.intellij.psi.PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        if (numberOfMethods == 1) {
            if (!expression.getText().contains("System.out.format(\"%n%s%n\"")) {
                if (!expression.getText().contains("org.junit.Assert.")) {
                    int numberOfArguments = expression.getArgumentList().getExpressions().length;
                    if (numberOfArguments < 2) {
                        flagLessThanTwoArgs = true;
                        methodName = expression.getMethodExpression().getReferenceName();
                    } else if (numberOfArguments == 2) {
                        PsiType[] psiTypes = expression.getArgumentList().getExpressionTypes();
                        String type1 = psiTypes[0].getPresentableText();
                        type1 = type1.substring(0, 1).toUpperCase().concat(type1.substring(1));
                        String type2 = psiTypes[1].getPresentableText();
                        type2 = type2.substring(0, 1).toUpperCase().concat(type2.substring(1));
                        if (psiTypes[0].getCanonicalText().equals(psiTypes[1].getCanonicalText())) {
                            if (type1.contains("(") || type1.contains("<") || type1.contains(".") || type1.contains("{")) {
                                methodName = expression.getMethodExpression().getReferenceName() +
                                        "Taking" + numberOfArguments + "Arguments";
                            } else {
                                methodName = expression.getMethodExpression().getReferenceName() +
                                        "Taking" + numberOfArguments + type1;
                            }
                        } else {
                            if (type1.contains("(") || type1.contains("<") || type1.contains(".") || type1.contains("{")
                                    || type2.contains("(") || type2.contains("<") || type2.contains(".") || type2.contains("{")) {
                                methodName = expression.getMethodExpression().getReferenceName() +
                                        "Taking" + numberOfArguments + "Arguments";
                            } else {
                                methodName = expression.getMethodExpression().getReferenceName() +
                                        "Taking" + type1 + "And" + type2;
                            }
                        }
                    } else {
                        methodName = expression.getMethodExpression().getReferenceName() +
                                "Taking" + numberOfArguments + "Arguments";
                    }
                }
                if (flagLessThanTwoArgs && (numberOfAsserts == 1)) {
                    if (expression.getText().contains("org.junit.Assert.assertTrue")) {
                        methodName = methodName.concat("ReturningTrue");
                    } else if (expression.getText().contains("org.junit.Assert.assertFalse")) {
                        methodName = methodName.concat("ReturningFalse");
                    } else if (expression.getText().contains("org.junit.Assert.assertNotNull")) {
                        methodName = methodName.concat("ReturningNonEmpty");
                    } else if (expression.getText().contains("org.junit.Assert.assertNull")) {
                        methodName = methodName.concat("IsNull");
                    } else if (expression.getText().contains("org.junit.Assert.fail")) {
                        String failExcTxt = expression.getArgumentList().getExpressions()[0].getText();
                        int lastIndexOfDot = failExcTxt.lastIndexOf(".");
                        failExcTxt = failExcTxt.substring(lastIndexOfDot + 1, failExcTxt.length() - 1);
                        methodName = methodName.concat("Throws" + failExcTxt);
                    }
                }

            }
        } else if (numberOfMethods > 1) {
            if ((!expression.getText().contains("System.out.format(\"%n%s%n\""))) {
                methodName = numberOfMethods + "MethodCall";
                if (numberOfAsserts == 1) {
                    if (expression.getText().contains("org.junit.Assert.assertTrue")) {
                        methodName = methodName.concat("ReturningTrue");
                    } else if (expression.getText().contains("org.junit.Assert.assertFalse")) {
                        methodName = methodName.concat("ReturningFalse");
                    } else if (expression.getText().contains("org.junit.Assert.assertNotNull")) {
                        methodName = methodName.concat("ReturningNonEmpty");
                    } else if (expression.getText().contains("org.junit.Assert.assertNull")) {
                        methodName = methodName.concat("IsNull");
                    } else if (expression.getText().contains("org.junit.Assert.fail")) {
                        String failExcTxt = expression.getArgumentList().getExpressions()[0].getText();
                        int lastIndexOfDot = failExcTxt.lastIndexOf(".");
                        failExcTxt = failExcTxt.substring(lastIndexOfDot + 1, failExcTxt.length() - 1);
                        methodName = methodName.concat("Throws" + failExcTxt);
                    }
                }
            }
        }
    }

    public void visitNewExpression(com.intellij.psi.PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (numberOfNewExpression == 1 && numberOfMethods < 1) {
            methodName = "Creates" + expression.getType().getPresentableText();
            if (methodName.contains("<")) {
                int index = methodName.indexOf("<");
                methodName = methodName.substring(0, index);
            }
        } else if (numberOfNewExpression > 1 && numberOfMethods < 1) {
            methodName = "Creates" + numberOfNewExpression + "DifferentObjects";
//            if (!expression.getArgumentList().isEmpty()) {
//                PsiType[] psiTypes = expression.getArgumentList().getExpressionTypes();
//                String type1 = psiTypes[0].getPresentableText();
//                type1 = type1.substring(0, 1).toUpperCase().concat(type1.substring(1));
//                String type2 = psiTypes[1].getPresentableText();
//                type2 = type2.substring(0, 1).toUpperCase().concat(type2.substring(1));
//                if (psiTypes[0].getCanonicalText().equals(psiTypes[1].getCanonicalText())) {
//                    if (type1.contains("(") || type1.contains("<") || type1.contains(".") || type1.contains("{")) {
//                        methodName = "Creates" + expression.getType().getPresentableText() +
//                                "Taking" + numberOfNewExpression + "Arguments";
//                    } else {
//                        methodName = "Creates" + expression.getType().getPresentableText() +
//                                "Taking" + numberOfNewExpression + type1;
//                    }
//                } else {
//                    if (type1.contains("(") || type1.contains("<") || type1.contains(".") || type1.contains("{")
//                            || type2.contains("(") || type2.contains("<") || type2.contains(".") || type2.contains("{")) {
//                        methodName = "Creates" + expression.getType().getPresentableText() +
//                                "Taking" + numberOfNewExpression + "Arguments";
//                    } else {
//                        methodName = "Creates" + expression.getType().getPresentableText() +
//                                "Taking" + type1 + "And" + type2;
//                    }
//                }
//            }
        }

    }

    public String getMethodName() {
        return methodName;
    }
}
