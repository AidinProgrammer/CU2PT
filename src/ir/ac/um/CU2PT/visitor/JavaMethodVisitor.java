package ir.ac.um.CU2PT.visitor;

import com.intellij.psi.*;

import java.util.ArrayList;


public class JavaMethodVisitor extends JavaRecursiveElementVisitor {

    ArrayList<ArrayList<String>> testMethods = new ArrayList<>();
    ArrayList<ArrayList<String>> testExpressions = new ArrayList<>();

    public JavaMethodVisitor(int numberOfMethods) {

        for (int i = 0; i < numberOfMethods; i++) {
            testMethods.add(new ArrayList<>());
        }

        for (int i = 0; i < numberOfMethods; i++) {
            testExpressions.add(new ArrayList<>());
        }

    }


    private boolean flagWhich = false;
    private int counter = 0;

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitElement(method);

        String methodName = method.getName();
        boolean flag = flagWhich;

        PsiStatement[] statements = method.getBody().getStatements();
        for (PsiStatement statement : statements) {
            if (statement instanceof PsiTryStatement) {
                PsiTryStatement tryStatement = (PsiTryStatement) statement;
                PsiStatement[] tryStatements = tryStatement.getTryBlock().getStatements();
                for (PsiStatement stmt : tryStatements) {
                    testMethodAggregate(stmt, flag);
                }
                PsiCodeBlock[] codeBlocks = tryStatement.getCatchBlocks();
                for (PsiCodeBlock codeBlock : codeBlocks) {
                    PsiStatement[] catchStatement = codeBlock.getStatements();
                    for (PsiStatement stmt : catchStatement) {
                        testMethodAggregate(stmt, flag);
                    }
                }
            } else {
                testMethodAggregate(statement, flag);
            }
        }

        if (flag) {
            counter++;
            flagWhich = false;
        } else {
            counter++;
            flagWhich = true;
        }
    }

    @Override
    public void visitMethodCallExpression(com.intellij.psi.PsiMethodCallExpression expression) {
        super.visitMethodCallExpression(expression);

        boolean flag = flagWhich;
        testExpressionsAggregate(expression, flag);
    }


    private void testMethodAggregate(PsiStatement statement, boolean flag) {
        if (!flag) {
            testMethods.get(counter).add(statement.getClass().getName());
        } else {
            testMethods.get(counter).add(statement.getClass().getName());
        }
    }

    private void testExpressionsAggregate(PsiMethodCallExpression expression, boolean flag) {
        if (!flag) {
            testExpressions.get(counter).add(expression.getMethodExpression().getQualifiedName());
        } else {
            testExpressions.get(counter).add(expression.getMethodExpression().getQualifiedName());
        }
    }

    boolean isCodeClonesEqual(ArrayList<ArrayList<String>> testMethods) {
        int comparisonSize = testMethods.get(0).size();
        for (int i = 1; i < testMethods.size(); i++) {
            if (comparisonSize != testMethods.get(i).size()) {
                return false;
            }
        }
        ArrayList<String> comparisonStmtDcl = testMethods.get(0);
        for (int i = 1; i < testMethods.size(); i++) {
            for (int j = 0; j < testMethods.get(i).size(); j++) {
                if (!comparisonStmtDcl.get(j).equals(testMethods.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isMethodCallsEqual(ArrayList<ArrayList<String>> testExpressions) {
        int comparisonSize = testExpressions.get(0).size();
        for (int i = 1; i < testExpressions.size(); i++) {
            if (comparisonSize != testExpressions.get(i).size()) {
                return false;
            }
        }
        ArrayList<String> comparisonExpr = testExpressions.get(0);
        for (int i = 1; i < testExpressions.size(); i++) {
            for (int j = 0; j < testExpressions.get(i).size(); j++) {
                if (!comparisonExpr.get(j).equals(testExpressions.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }


    public ArrayList<ArrayList<String>> getTestMethods() {
        return this.testMethods;
    }

    public ArrayList<ArrayList<String>> getTestExpressions() {
        return this.testExpressions;
    }

}