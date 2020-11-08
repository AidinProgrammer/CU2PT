package ir.ac.um.CU2PT.visitor;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiPolyadicExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiTypeCastExpressionImpl;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaMethodCallVisitor extends JavaRecursiveElementVisitor {
    public JavaMethodCallVisitor() {

    }

    ArrayList<String> methodArgsValue = new ArrayList<>();
    ArrayList<String> methodArgsType = new ArrayList<>();

    @Override
    public void visitMethodCallExpression(com.intellij.psi.PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);

        if (expression.getText().contains("System.out.format(\"%n%s%n\"")) {
            methodArgsValue.add(expression.getArgumentList().getExpressions()[1].getText());
            methodArgsType.add(expression.getArgumentList().getExpressionTypes()[1].getCanonicalText());
        }

        for (int i = 0; i < expression.getArgumentList().getExpressionCount(); i++) {

            if (!expression.getText().contains("System.out.format(\"%n%s%n\"")) {
                PsiExpression psiExpression = expression.getArgumentList().getExpressions()[i];
                if (!psiExpression.getClass().getName().equals("com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl")) {
                    PsiType psiType = expression.getArgumentList().getExpressionTypes()[i];
                    if (psiType.equalsToText("boolean")) {
                        if (!(psiExpression.getLastChild() instanceof PsiReferenceExpressionImpl)) {
                            if (psiExpression instanceof PsiBinaryExpressionImpl) {
                                PsiBinaryExpressionImpl binaryExpressions = (PsiBinaryExpressionImpl) psiExpression;
                                if (!(binaryExpressions.getROperand() instanceof PsiReferenceExpressionImpl)) {
                                    methodArgsValue.add(psiExpression.getLastChild().getText());
                                    methodArgsType.add(psiType.getCanonicalText());
                                }
                            } else {
                                methodArgsValue.add(psiExpression.getLastChild().getText());
                                methodArgsType.add(psiType.getCanonicalText());
                            }
                        }

                    } else {
                        if (!(psiExpression instanceof PsiReferenceExpressionImpl)) {

                            boolean flagRefInPoly = false;

                            if (psiExpression instanceof PsiPolyadicExpressionImpl) {
                                PsiPolyadicExpressionImpl polyadicExpression = (PsiPolyadicExpressionImpl) psiExpression;
                                PsiExpression[] polyExpressions = polyadicExpression.getOperands();
                                for (PsiExpression polyExpression : polyExpressions) {
                                    if (polyExpression instanceof PsiReferenceExpressionImpl) {
                                        flagRefInPoly = true;
                                    } else {
                                        Pattern pattern = Pattern.compile("[0-9a-zA-Z#!]+");
                                        Matcher matcher = pattern.matcher(polyExpression.getText());
                                        if (matcher.find() || polyExpression.getText().equals("\"\"") || polyExpression.getText().equals("\" \"")) {
                                            methodArgsValue.add(polyExpression.getText());
                                        }
                                    }
                                }
                            }

                            if (!flagRefInPoly) {
                                if (psiExpression instanceof PsiTypeCastExpressionImpl) {
                                    PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) psiExpression;
                                    if (!(typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                                        methodArgsValue.add(psiExpression.getText());
                                        methodArgsType.add(psiType.getCanonicalText());
                                    }
                                } else {
                                    methodArgsValue.add(psiExpression.getText());
                                    methodArgsType.add(psiType.getCanonicalText());
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public ArrayList<String> getArguments() {
        return this.methodArgsValue;
    }

}
