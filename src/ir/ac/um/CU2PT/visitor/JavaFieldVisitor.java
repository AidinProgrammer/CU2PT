package ir.ac.um.CU2PT.visitor;

import ir.ac.um.CU2PT.Utils;
import com.intellij.psi.*;

public class JavaFieldVisitor extends JavaRecursiveElementVisitor {

    public JavaFieldVisitor() {
    }

    @Override
    public void visitField(PsiField field) {
        super.visitVariable(field);
        Utils.showMessage("Field " + field.getName());
    }

}
