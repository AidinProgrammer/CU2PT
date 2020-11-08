package ir.ac.um.CU2PT.visitor;

import ir.ac.um.CU2PT.Utils;
import com.intellij.psi.*;


public class VisitorUtils {
    public static void processClassSuperClasses(PsiClass aClass) {
        PsiClass superClass = aClass.getSuperClass();
        if (superClass != null) {
            String superClassName = superClass.getQualifiedName();
            Utils.showMessage("SuperClass " + superClassName);
        }
    }

    public static void processInterfacesImplementedByClass(PsiClass aClass) {
        for (PsiClass implementedInterface : aClass.getInterfaces()) {
            String interfaceName = implementedInterface.getQualifiedName();
            Utils.showMessage("Implements interface " + interfaceName);
        }
    }

}

