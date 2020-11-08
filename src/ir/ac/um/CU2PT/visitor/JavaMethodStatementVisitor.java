package ir.ac.um.CU2PT.visitor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiPolyadicExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiTypeCastExpressionImpl;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JavaMethodStatementVisitor extends JavaRecursiveElementVisitor {

    public JavaMethodStatementVisitor() {
    }

    private int counter = 0;
    private int numberOfMethods = 0;
    private int numberOfAsserts = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
    }

    @Override
    public void visitMethodCallExpression(com.intellij.psi.PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);

        if (expression.getText().contains("System.out.format(\"%n%s%n\"")) {
            PsiExpression ifExpression = expression.getArgumentList().getExpressions()[1];
            replaceExpression(ifExpression, "argument[" + JavaFileVisitor.counter + "]");
            counter++;
            JavaFileVisitor.counter++;
        }

        for (int i = 0; i < expression.getArgumentList().getExpressionCount(); i++) {

            if (!expression.getText().contains("System.out.format(\"%n%s%n\"")) {
                PsiExpression[] expressions = expression.getArgumentList().getExpressions();
                if (!expressions[i].getClass().getName().equals("com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl")) {
                    if (expressions[i].getType().equalsToText("boolean") && expressions[i].getChildren().length > 1) {
                        String argumentType;
                        if (!(expressions[i] instanceof PsiReferenceExpressionImpl)) {
                            if (expressions[i] instanceof PsiBinaryExpressionImpl) {
                                PsiBinaryExpressionImpl psiBinaryExpression = (PsiBinaryExpressionImpl) expressions[i];
                                argumentType = psiBinaryExpression.getROperand().getType().getCanonicalText();
                                if (!(psiBinaryExpression.getROperand() instanceof PsiReferenceExpressionImpl)) {
                                    replaceExpression(expressions[i].getLastChild(), "(" + argumentType + ") " + "argument[" + JavaFileVisitor.counter + "]");
                                    counter++;
                                    JavaFileVisitor.counter++;
                                }
                            }
                        }
                    } else {
                        if (!(expressions[i] instanceof PsiReferenceExpressionImpl)) {

                            boolean flagRefInPoly = false;

                            if (expressions[i] instanceof PsiPolyadicExpressionImpl) {
                                PsiPolyadicExpressionImpl polyadicExpression = (PsiPolyadicExpressionImpl) expressions[i];
                                PsiExpression[] polyExpressions = polyadicExpression.getOperands();
                                for (PsiExpression polyExpression : polyExpressions) {
                                    if (polyExpression instanceof PsiReferenceExpressionImpl) {
                                        flagRefInPoly = true;
                                    } else {
                                        Pattern pattern = Pattern.compile("[0-9a-zA-Z#!]+");
                                        Matcher matcher = pattern.matcher(polyExpression.getText());
                                        if (matcher.find() || polyExpression.getText().equals("\"\"") || polyExpression.getText().equals("\" \"")) {
                                            String argumentType = polyExpression.getType().getCanonicalText();
                                            replaceExpression(polyExpression, "(" + argumentType + ")" + "argument[" + JavaFileVisitor.counter + "]");
                                            counter++;
                                            JavaFileVisitor.counter++;
                                        }
                                    }
                                }
                            }

                            if (!flagRefInPoly) {
                                String argumentType = expressions[i].getType().getCanonicalText();
                                if (expressions[i] instanceof PsiTypeCastExpressionImpl) {
                                    PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) expressions[i];
                                    if (!(typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                                        replaceExpression(expressions[i], "(" + argumentType + ")" + "argument[" + JavaFileVisitor.counter + "]");
                                        counter++;
                                        JavaFileVisitor.counter++;
                                    }
                                } else {
                                    replaceExpression(expressions[i], "(" + argumentType + ")" + "argument[" + JavaFileVisitor.counter + "]");
                                    counter++;
                                    JavaFileVisitor.counter++;
                                }
                            }
                        }
                    }
                }

            }
        }

        if (!expression.getText().contains("System.out.format(\"%n%s%n\"")) {
            if (!expression.getText().contains("org.junit.Assert.")) {
                numberOfMethods++;
            } else {
                numberOfAsserts++;
            }
        }

    }

    private synchronized void replaceExpression(@NotNull PsiElement expression, @NotNull @NonNls String
            newExpressionText) throws IncorrectOperationException {
        final Project project = expression.getProject();

        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
                final PsiElementFactory factory = psiFacade.getElementFactory();
                final PsiExpression newExpression = factory.createExpressionFromText(newExpressionText, expression);
                final PsiElement replacementExpression = expression.replace(newExpression);
                final CodeStyleManager styleManager = CodeStyleManager.getInstance(project);
                styleManager.reformat(replacementExpression);
            });
        });
    }

    public int getNumberOfArguments() {
        return counter;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public int getNumberOfAsserts() {
        return numberOfAsserts;
    }
}
