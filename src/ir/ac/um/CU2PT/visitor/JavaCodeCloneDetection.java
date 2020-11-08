package ir.ac.um.CU2PT.visitor;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiTryStatement;

import java.util.*;

public class JavaCodeCloneDetection extends JavaRecursiveElementVisitor {
    public JavaCodeCloneDetection() {

    }

    private ArrayList<PsiMethod> psiMethods = new ArrayList<>();
    private ArrayList<Integer> methodCodeLength = new ArrayList<>();

    @Override
    public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        int counter = 0;
        psiMethods.add(method);
        PsiStatement[] statements = method.getBody().getStatements();
        for (PsiStatement statement : statements) {
            if (statement instanceof PsiTryStatement) {
                 PsiStatement[] tryStatement = ((PsiTryStatement) statement).getTryBlock().getStatements();
                 for (PsiStatement stmt : tryStatement) {
                     counter++;
                 }
            } else {
                counter++;
            }
        }
        methodCodeLength.add(counter);
    }

    public ArrayList<Integer> getMethodCodeLength() {
        return methodCodeLength;
    }

    public ArrayList<PsiMethod> getPsiMethods() {
        return psiMethods;
    }
}
