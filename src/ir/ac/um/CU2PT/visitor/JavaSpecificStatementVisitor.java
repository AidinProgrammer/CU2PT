package ir.ac.um.CU2PT.visitor;

import ir.ac.um.CU2PT.Utils;
import com.intellij.psi.*;

public class JavaSpecificStatementVisitor extends JavaElementVisitor {

    public JavaSpecificStatementVisitor() {
    }

    @Override
    public void visitForStatement(PsiForStatement statement) {
        super.visitStatement(statement);
        Utils.showMessage("For Loop");
    }

    @Override
    public void visitDoWhileStatement(PsiDoWhileStatement statement) {
        super.visitStatement(statement);
        Utils.showMessage("Do-While Loop");
    }

    @Override
    public void visitWhileStatement(PsiWhileStatement statement) {
        super.visitStatement(statement);
        Utils.showMessage("While Loop");
    }

    @Override
    public void visitForeachStatement(PsiForeachStatement statement) {
        super.visitStatement(statement);
        Utils.showMessage("ForEach Loop");
    }

    private void visitLoop(PsiLoopStatement statement, String predicate) {
        Utils.showMessage("Loop");
    }

    @Override
    public void visitBreakStatement(PsiBreakStatement statement) {
        super.visitStatement(statement);
        Utils.showMessage("Break Statement");
    }

    @Override
    public void visitContinueStatement(PsiContinueStatement statement) {
        super.visitStatement(statement);
        Utils.showMessage("Continue Statement");
    }

    @Override
    public void visitIfStatement(PsiIfStatement statement) {
        super.visitStatement(statement);
        processConditionExpressionForIf(statement);
    }

    private void processConditionExpressionForIf(PsiIfStatement statement) {
        //            TODO handle this
        /*
        PsiExpression condition = statement.getCondition();
        if (condition instanceof PsiBinaryExpressionImpl) {
        }
        */
    }

    public void visitExpressionStatement(PsiExpressionStatement statement) {
        super.visitStatement(statement);
        PsiExpression expression = statement.getExpression();
        if (expression instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) expression;
            this.visitMethodCallExpression(methodCallExpression);
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);
        try {
            PsiElement element = expression.getMethodExpression().getReference().resolve();
            if (element instanceof PsiMethod) {
                PsiMethod calledMethod = (PsiMethod) element;
                Utils.showMessage("CalledMethod is " + calledMethod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void visitLocalVariable(PsiLocalVariable variable) {
        super.visitVariable(variable);

        Utils.showMessage("Local variable " + variable.getTypeElement().getType() + " " + variable.getName());
    }


    public void visitAssertStatement(PsiAssertStatement statement) {
        super.visitStatement(statement);

        Utils.showMessage("assert statement");
        PsiExpression condition = statement.getAssertCondition();
        PsiExpression description = statement.getAssertDescription();
        Utils.showMessage("condition " + condition);
        Utils.showMessage("description " + description);
    }

}

