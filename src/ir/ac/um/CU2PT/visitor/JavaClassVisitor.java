package ir.ac.um.CU2PT.visitor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import ir.ac.um.CU2PT.Utils;
import com.intellij.psi.*;

import java.io.*;
import java.util.ArrayList;

public class JavaClassVisitor extends JavaRecursiveElementVisitor {


    public JavaClassVisitor() {
    }


    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);
        String name = aClass.getQualifiedName();
        if (name == null) {
            //the class is anonymous class, it will be processed somewhere else
            return;
        }

        String simpleName = aClass.getQualifiedName();
        int indexOfC = simpleName.lastIndexOf("C");
        String classNumber = simpleName.substring(indexOfC + 1);

        //Reg0CC1
        int indexOfG = simpleName.indexOf("g");
        int indexOfFirstC = simpleName.indexOf("C");
        String regressionTestNumber = simpleName.substring(indexOfG + 1, indexOfFirstC);

//        VisitorUtils.processClassSuperClasses(aClass);
//        VisitorUtils.processInterfacesImplementedByClass(aClass);


//        PsiField[] psiFields = aClass.getFields();
//        for (PsiField psiField : psiFields) {
//            psiField.accept(new JavaFieldVisitor());
//        }

        PsiMethod[] psiMethods = aClass.getMethods();


        JavaMethodCallVisitor methodCallVisitor = new JavaMethodCallVisitor();
        ArrayList<String> arguments = methodCallVisitor.getArguments();

        JavaNewVisitor javaNewVisitor = new JavaNewVisitor();
        ArrayList<String> newExpArguments = javaNewVisitor.getArguments();

        for (PsiMethod psiMethod : psiMethods) {
            psiMethod.accept(methodCallVisitor);
        }


        try {
            for (PsiMethod psiMethod : psiMethods) {
                psiMethod.accept(javaNewVisitor);
            }
        } catch (Exception ex) {
//                    Utils.showMessage("Exception: " + ex);
        }


        PsiStatement[] statements = psiMethods[0].getBody().getStatements();

        JavaMethodStatementVisitor statementVisitor = new JavaMethodStatementVisitor();
        for (PsiStatement statement : statements) {
            statement.accept(statementVisitor);
        }

        JavaNewExpressionVisitor newExpressionVisitor = new JavaNewExpressionVisitor();
        for (PsiStatement statement : statements) {
            statement.accept(newExpressionVisitor);
        }

        int numberOfMethods = statementVisitor.getNumberOfMethods();
        int numberOfAsserts = statementVisitor.getNumberOfAsserts();
        int numberOfNewExpression = newExpressionVisitor.getNumberOfNewExpression();

        JavaNamingMethods namingMethods = new JavaNamingMethods(numberOfMethods, numberOfAsserts, numberOfNewExpression);
        for (PsiStatement statement : statements) {
            statement.accept(namingMethods);
        }


        int numberOfArgs = statementVisitor.getNumberOfArguments();
        int numberOfNewExpArgs = newExpressionVisitor.getNumberOfArguments();

        String methodName = namingMethods.getMethodName();
        if (!methodName.isEmpty() || methodName != null) {
            methodName = methodName.substring(0, 1).toUpperCase().concat(methodName.substring(1));
        }

        boolean newExpFlag = false;
        if (newExpArguments.size() > 0) {
            newExpFlag = true;
        }

        boolean finalNewExpFlag = newExpFlag;

        String finalMethodName = methodName;
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(aClass.getProject(), () -> {


            String path = aClass.getProject().getBasePath() + "\\tests\\CodeClones\\" + aClass.getContainingFile().getOriginalFile().getName();

            try {
                File file = new File(path);
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                bufferedWriter.write(
                        "\n@DataPoints(\"" + simpleName + "\")\n" +
                                "public static Object[][] " + simpleName + "Arguments() {\n" +
                                "\treturn new Object[][]" + "{{"
                );
                if (finalNewExpFlag) {

                    int totalSize = arguments.size() + newExpArguments.size();
                    int totalStep = numberOfArgs + numberOfNewExpArgs;

                    for (int i = 0; i < totalSize; i += totalStep) {

                        int j = 0;
                        for (; j < numberOfArgs; j++) {
                            bufferedWriter.append(arguments.get(j) + ", ");
                        }
                        for (int k = 0; k < j; k++) {
                            arguments.remove(0);
                        }

                        int jj = 0;
                        for (; jj < numberOfNewExpArgs; jj++) {
                            if (jj == (numberOfNewExpArgs - 1)) {
                                bufferedWriter.append(newExpArguments.get(jj));
                                if (i + totalStep != totalSize) {
                                    bufferedWriter.append("},\n\t\t\t{");
                                }
                            } else {
                                bufferedWriter.append(newExpArguments.get(jj) + ", ");
                            }
                        }
                        for (int k = 0; k < jj; k++) {
                            newExpArguments.remove(0);
                        }


                    }

                } else {
                    for (int i = 0; i < arguments.size(); i += numberOfArgs) {
                        for (int j = i; j < (i + numberOfArgs); j++) {
                            if (j == ((i + numberOfArgs) - 1)) {
                                bufferedWriter.append(arguments.get(j));
                                if (j != arguments.size() - 1) {
                                    bufferedWriter.append("},\n\t\t\t{");
                                }
                            } else {
                                bufferedWriter.append(arguments.get(j) + ", ");
                            }
                        }
                    }
                }

                bufferedWriter.append(
                        "}};\n" +
                                "}\n\n" +
                                "@Theory\n"
                );
                if (finalMethodName.isEmpty() || finalMethodName == null) {
                    bufferedWriter.append("public void testParameterized_cc" + classNumber + "(@FromDataPoints(\"" + simpleName + "\") " + "Object[] argument) {\n");
                } else {
                    bufferedWriter.append("public void test" + finalMethodName + "_cc" + classNumber + "(@FromDataPoints(\"" + simpleName + "\") " + "Object[] argument) {\n");
                }

                for (int i = 0; i < statements.length; i++) {
                    bufferedWriter.append("\t" + statements[i].getText() + "\n");
                }
                bufferedWriter.append("}\n");

                File files = new File(aClass.getProject().getBasePath() + "\\tests\\MethodsList");
                File[] methodListsFiles = files.listFiles();
                String codeCloneFileNum = "";
                for (int i = 0; i < methodListsFiles.length; i++) {
                    if (methodListsFiles[i].getName().startsWith("MethodsList" + regressionTestNumber)) {
                        codeCloneFileNum = methodListsFiles[i].getName().substring(methodListsFiles[i].getName().indexOf("-") + 1, methodListsFiles[i].getName().indexOf("."));
                    }
                }

                int ccNum = Integer.parseInt(codeCloneFileNum) - 1;

                String codeCloneFileNumber = "Reg" + regressionTestNumber + "CC" + ccNum;

                if (codeCloneFileNumber.equals(simpleName)) {
                    bufferedWriter.append("\n\n}");
                }

                bufferedWriter.flush();
                bufferedWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                Utils.showMessage(e.getMessage());
            }


        }));

        Utils.showMessage("---> The parameterized tests of " + name + " are generated.");

    }

}