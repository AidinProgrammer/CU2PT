package ir.ac.um.CU2PT.visitor;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiTypeCastExpressionImpl;

import java.util.ArrayList;

public class JavaExpressionsVisitor extends JavaRecursiveElementVisitor {

    private int whichMethod;

    public JavaExpressionsVisitor(int whichMethod) {
        this.whichMethod = whichMethod;
    }

    private ArrayList<String> methodIExpressions = new ArrayList<>();
    private ArrayList<String> methodJExpressions = new ArrayList<>();
    private ArrayList<Integer> numberOfArgumentsInMethodI = new ArrayList<>();
    private ArrayList<Integer> numberOfArgumentsInMethodJ = new ArrayList<>();
    private ArrayList<String> methodIExpressionsType = new ArrayList<>();
    private ArrayList<String> methodJExpressionsType = new ArrayList<>();
    private ArrayList<String> methodIBinaryExpressionsType = new ArrayList<>();
    private ArrayList<String> methodJBinaryExpressionsType = new ArrayList<>();
    private ArrayList<String> argumentIRuntimeClass = new ArrayList<>();
    private ArrayList<String> argumentJRuntimeClass = new ArrayList<>();

    @Override
    public void visitMethodCallExpression(com.intellij.psi.PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        if (whichMethod == 1) {
            methodIExpressions.add(expression.getMethodExpression().getQualifiedName());
            numberOfArgumentsInMethodI.add(expression.getArgumentList().getExpressions().length);
            PsiExpression[] psiExpressions = expression.getArgumentList().getExpressions();
            for (PsiExpression psiExpression : psiExpressions) {
                argumentIRuntimeClass.add(psiExpression.getClass().getName());
                if (psiExpression instanceof PsiBinaryExpressionImpl) {
                    PsiBinaryExpressionImpl binaryExpressions = (PsiBinaryExpressionImpl) psiExpression;
                    if (binaryExpressions.getROperand() instanceof PsiReferenceExpressionImpl) {
                        methodIBinaryExpressionsType.add(psiExpression.getClass().getName());
                    }
                }
                if (psiExpression instanceof PsiReferenceExpressionImpl) {
                    methodIExpressionsType.add(psiExpression.getClass().getName());
                }
                if (psiExpression instanceof PsiTypeCastExpressionImpl) {
                    PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) psiExpression;
                    if (typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl) {
                        methodIExpressionsType.add(psiExpression.getClass().getName());
                    }
                }
            }

        } else {
            methodJExpressions.add(expression.getMethodExpression().getQualifiedName());
            numberOfArgumentsInMethodJ.add(expression.getArgumentList().getExpressions().length);
            PsiExpression[] psiExpressions = expression.getArgumentList().getExpressions();
            for (PsiExpression psiExpression : psiExpressions) {
                argumentJRuntimeClass.add(psiExpression.getClass().getName());
                if (psiExpression instanceof PsiBinaryExpressionImpl) {
                    PsiBinaryExpressionImpl binaryExpressions = (PsiBinaryExpressionImpl) psiExpression;
                    if (binaryExpressions.getROperand() instanceof PsiReferenceExpressionImpl) {
                        methodJBinaryExpressionsType.add(psiExpression.getClass().getName());
                    }
                }
                if (psiExpression instanceof PsiReferenceExpressionImpl) {
                    methodJExpressionsType.add(psiExpression.getClass().getName());
                }
                if (psiExpression instanceof PsiTypeCastExpressionImpl) {
                    PsiTypeCastExpressionImpl typeCastExpression = (PsiTypeCastExpressionImpl) psiExpression;
                    if (typeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl) {
                        methodJExpressionsType.add(psiExpression.getClass().getName());
                    }
                }
            }
        }
    }

    public ArrayList<String> getMethodIExpressions() {
        return methodIExpressions;
    }

    public ArrayList<String> getMethodJExpressions() {
        return methodJExpressions;
    }

    public ArrayList<Integer> getNumberOfArgumentsInMethodI() {
        return numberOfArgumentsInMethodI;
    }

    public ArrayList<Integer> getNumberOfArgumentsInMethodJ() {
        return numberOfArgumentsInMethodJ;
    }

    public ArrayList<String> getMethodIExpressionsType() {
        return methodIExpressionsType;
    }

    public ArrayList<String> getMethodJExpressionsType() {
        return methodJExpressionsType;
    }

    public ArrayList<String> getMethodIBinaryExpressionsType() {
        return methodIBinaryExpressionsType;
    }

    public ArrayList<String> getMethodJBinaryExpressionsType() {
        return methodJBinaryExpressionsType;
    }

    public ArrayList<String> getArgumentIRuntimeClass() {
        return argumentIRuntimeClass;
    }

    public ArrayList<String> getArgumentJRuntimeClass() {
        return argumentJRuntimeClass;
    }
}
