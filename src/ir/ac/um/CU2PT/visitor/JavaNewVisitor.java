package ir.ac.um.CU2PT.visitor;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiTypeCastExpressionImpl;

import java.util.ArrayList;

public class JavaNewVisitor extends JavaRecursiveElementVisitor {
    public JavaNewVisitor() {
    }

    ArrayList<String> newExpressionValues = new ArrayList<>();

    public void visitNewExpression(com.intellij.psi.PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (!expression.getText().contains("[]")) {
            PsiExpression[] psiExpressions = expression.getArgumentList().getExpressions();
            for (PsiExpression psiExpression : psiExpressions) {
                if (!(psiExpression instanceof PsiReferenceExpressionImpl)) {
                    if (psiExpression instanceof PsiTypeCastExpressionImpl) {
                        PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) psiExpression;
                        if (!(typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                            newExpressionValues.add(psiExpression.getText());
                        }
                    } else {
                        newExpressionValues.add(psiExpression.getText());
                    }
                }
            }
        } else {
            PsiExpression[] psiExpressions = expression.getArrayInitializer().getInitializers();
            for (PsiExpression psiExpression : psiExpressions) {
                if (!(psiExpression instanceof PsiReferenceExpressionImpl)) {
                    if (psiExpression instanceof PsiTypeCastExpressionImpl) {
                        PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) psiExpression;
                        if (!(typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                            newExpressionValues.add(psiExpression.getText());
                        }
                    } else {
                        newExpressionValues.add(psiExpression.getText());
                    }
                }
            }
        }
    }

    public ArrayList<String> getArguments() {
        return newExpressionValues;
    }
}
