package ir.ac.um.CU2PT.visitor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiTypeCastExpressionImpl;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JavaNewExpressionVisitor extends JavaRecursiveElementVisitor {
    public JavaNewExpressionVisitor() {
    }

    private int counter = 0;
    private int numberOfNewExpression = 0;


    public void visitNewExpression(com.intellij.psi.PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (!expression.getText().contains("[]")) {
            numberOfNewExpression++;
            PsiExpression[] psiExpressions = expression.getArgumentList().getExpressions();
            for (PsiExpression psiExpression : psiExpressions) {
                if (!(psiExpression instanceof PsiReferenceExpressionImpl)) {
                    String argumentType = psiExpression.getType().getCanonicalText();
                    if (psiExpression instanceof PsiTypeCastExpressionImpl) {
                        PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) psiExpression;
                        if (!(typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                            replaceExpression(psiExpression, "(" + argumentType + ")" + "argument[" + JavaFileVisitor.counter + "]");
                            counter++;
                            JavaFileVisitor.counter++;
                        }
                    } else {
                        replaceExpression(psiExpression, "(" + argumentType + ")" + "argument[" + JavaFileVisitor.counter + "]");
                        counter++;
                        JavaFileVisitor.counter++;
                    }
                }
            }
        } else {

            PsiExpression[] psiExpressions = expression.getArrayInitializer().getInitializers();
            for (PsiExpression psiExpression : psiExpressions) {
                if (!(psiExpression instanceof PsiReferenceExpressionImpl)) {
                    String argumentType = psiExpression.getType().getCanonicalText();
                    if (psiExpression instanceof PsiTypeCastExpressionImpl) {
                        PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) psiExpression;
                        if (!(typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                            replaceExpression(psiExpression, "(" + argumentType + ")" + "argument[" + JavaFileVisitor.counter + "]");
                            counter++;
                            JavaFileVisitor.counter++;
                        }
                    } else {
                        replaceExpression(psiExpression, "(" + argumentType + ")" + "argument[" + JavaFileVisitor.counter + "]");
                        counter++;
                        JavaFileVisitor.counter++;
                    }
                }
            }

        }
    }

    private synchronized void replaceExpression(@NotNull PsiExpression expression, @NotNull @NonNls String
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

    public int getNumberOfNewExpression() {
        return numberOfNewExpression;
    }
}
