package ir.ac.um.CU2PT.visitor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.impl.source.tree.java.*;
import ir.ac.um.CU2PT.Utils;
import com.intellij.psi.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JavaFileVisitor extends JavaRecursiveElementVisitor {

    public JavaFileVisitor() {
    }

    public static int counter = 0;

    @Override
    public void visitJavaFile(PsiJavaFile psiJavaFile) {
        super.visitFile(psiJavaFile);

        String path = psiJavaFile.getVirtualFile().getPath();
        Utils.showMessage("Processing java file path " + path);
        Utils.showMessage("Processing java file name " + psiJavaFile.getName());

        int indx1 = psiJavaFile.getName().lastIndexOf("t");
        int indx2 = psiJavaFile.getName().indexOf(".");
        String regressionFileNumber = psiJavaFile.getName().substring(indx1 + 1, indx2);

        int pathIndex = path.indexOf(psiJavaFile.getName());
        String newPath = path.substring(0, pathIndex) + "originals";

        if (psiJavaFile.getName().startsWith("RegressionTest")) {
            File directory = new File(newPath);
            if (!directory.exists()) {
                directory.mkdir();
            }
            if (directory.list().length < 1) {
                copyFile(new File(path), new File(newPath + "\\" + psiJavaFile.getName()));
            } else {
                boolean findFileFlag = false;
                for (int i = 0; i < directory.list().length; i++) {
                    if (directory.list()[i].equals(psiJavaFile.getName())) {
                        findFileFlag = true;
                    }
                }
                if (!findFileFlag) {
                    copyFile(new File(path), new File(newPath + "\\" + psiJavaFile.getName()));
                }
            }
        }

        int ccPathIndex = path.indexOf(psiJavaFile.getName());
        String ccNewPath = path.substring(0, ccPathIndex);
        PsiClass[] psiClasses = psiJavaFile.getClasses();
        String codeClonesPath = "";
        ArrayList<String> codeCloneMethodNames = new ArrayList<>();

        if (psiJavaFile.getName().startsWith("RegressionTest")) {
            JavaCodeCloneDetection codeCloneDetection = new JavaCodeCloneDetection();

            for (PsiClass psiClass : psiClasses) {
                psiClass.accept(codeCloneDetection);
            }

            ArrayList<PsiMethod> psiMethods = codeCloneDetection.getPsiMethods();
            ArrayList<Integer> codeLengths = codeCloneDetection.getMethodCodeLength();
            ArrayList<String> methodName = new ArrayList<>();
            methodName.add("testMethod");

            int numberOfMethods = psiMethods.size();
            int numberOfMethodClones = 0;
            boolean flagIsWritten = false;
            int fileNumCounter = 1;
            boolean flagAreAnyCodeClonesGenerated = false;

            for (int i = 0; i < psiMethods.size() - 1; i++) {
                boolean flagMethodIPrint = true;
                PsiStatement[] iStatements = psiMethods.get(i).getBody().getStatements();
                if (flagIsWritten) {
                    fileNumCounter++;
                    flagIsWritten = false;
                }

                for (int j = i + 1; j < codeLengths.size(); j++) {
                    boolean isMethodUsed = false;
                    boolean isExpressionsEqual = true;

                    for (String name : methodName) {
                        if (!name.equals(psiMethods.get(j).getName())) {
                            isMethodUsed = true;
                        } else {
                            isMethodUsed = false;
                            break;
                        }
                    }

                    if (isMethodUsed) {
                        if (codeLengths.get(i) == codeLengths.get(j)) {
                            boolean flagMethodStmtEqual = false;

                            PsiStatement[] jStatements = psiMethods.get(j).getBody().getStatements();
                            for (int k = 0; k < iStatements.length; k++) {
                                try {
                                    if (iStatements[k].getClass().getName().equals(jStatements[k].getClass().getName())) {
                                        if (iStatements[k] instanceof PsiDeclarationStatementImpl && jStatements[k] instanceof PsiDeclarationStatementImpl) {
                                            if (iStatements[k].getFirstChild().getChildren()[1].getText().equals(jStatements[k].getFirstChild().getChildren()[1].getText())) {
                                                if (iStatements[k].getFirstChild().getChildren()[7] != null && jStatements[k].getFirstChild().getChildren()[7] != null) {
                                                    if (iStatements[k].getFirstChild().getChildren()[7].getClass().getName()
                                                            .equals(jStatements[k].getFirstChild().getChildren()[7].getClass().getName())) {
                                                        if (iStatements[k].getFirstChild().getChildren()[7] instanceof PsiReferenceExpressionImpl
                                                                && jStatements[k].getFirstChild().getChildren()[7] instanceof PsiReferenceExpressionImpl) {
                                                            if (!iStatements[k].getFirstChild().getChildren()[7].getText()
                                                                    .equals(jStatements[k].getFirstChild().getChildren()[7].getText())) {
                                                                flagMethodStmtEqual = false;
                                                                break;
                                                            }
                                                        }
                                                        if (iStatements[k].getFirstChild().getChildren()[7] instanceof PsiLiteralExpressionImpl
                                                                && jStatements[k].getFirstChild().getChildren()[7] instanceof PsiLiteralExpressionImpl) {
                                                            if (!iStatements[k].getFirstChild().getChildren()[7].getText()
                                                                    .equals(jStatements[k].getFirstChild().getChildren()[7].getText())) {
                                                                flagMethodStmtEqual = false;
                                                                break;
                                                            }
                                                        }
                                                        if (iStatements[k].getFirstChild().getChildren()[7] instanceof PsiNewExpressionImpl
                                                                && jStatements[k].getFirstChild().getChildren()[7] instanceof PsiNewExpressionImpl) {
                                                            PsiNewExpressionImpl iNewExpression = (PsiNewExpressionImpl) iStatements[k].getFirstChild().getChildren()[7];
                                                            PsiNewExpressionImpl jNewExpression = (PsiNewExpressionImpl) jStatements[k].getFirstChild().getChildren()[7];

                                                            if (!(iNewExpression.getText().contains("[]") && jNewExpression.getText().contains("[]"))) {
                                                                if (iNewExpression.getArgumentList().getExpressionCount() != jNewExpression.getArgumentList().getExpressionCount()) {
                                                                    flagMethodStmtEqual = false;
                                                                    break;
                                                                }
                                                                PsiExpression[] psiINewExpression = iNewExpression.getArgumentList().getExpressions();
                                                                PsiExpression[] psiJNewExpression = jNewExpression.getArgumentList().getExpressions();
                                                                for (int l = 0; l < psiINewExpression.length; l++) {

                                                                    if (psiINewExpression[l] instanceof PsiReferenceExpressionImpl ||
                                                                            psiJNewExpression[l] instanceof PsiReferenceExpressionImpl) {
                                                                        if (!psiINewExpression[l].getClass().getName().equals(psiJNewExpression[l].getClass().getName())) {
                                                                            flagMethodStmtEqual = false;
                                                                            break;
                                                                        }
                                                                    }
                                                                    if (psiINewExpression[l] instanceof PsiTypeCastExpressionImpl) {
                                                                        if (psiJNewExpression[l] instanceof PsiTypeCastExpressionImpl) {
                                                                            PsiTypeCastExpressionImpl iTypeCastExpression = (PsiTypeCastExpressionImpl) psiINewExpression[l];
                                                                            PsiTypeCastExpressionImpl jTypeCastExpression = (PsiTypeCastExpressionImpl) psiJNewExpression[l];
                                                                            if (iTypeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl) {
                                                                                if (!(jTypeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                                                                                    flagMethodStmtEqual = false;
                                                                                    break;
                                                                                }
                                                                            }
                                                                            if (jTypeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl) {
                                                                                if (!(iTypeCastExpression.getOperand() instanceof PsiReferenceExpressionImpl)) {
                                                                                    flagMethodStmtEqual = false;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                }
                                                                if (!flagMethodStmtEqual) {
                                                                    break;
                                                                }

                                                            } else {
                                                                if (iNewExpression.getArrayInitializer().getInitializers().length !=
                                                                        jNewExpression.getArrayInitializer().getInitializers().length) {
                                                                    flagMethodStmtEqual = false;
                                                                    break;
                                                                }
//
                                                            }

                                                        }
                                                        flagMethodStmtEqual = true;
                                                    } else {
                                                        flagMethodStmtEqual = false;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                flagMethodStmtEqual = false;
                                                break;
                                            }
                                        } else {
                                            flagMethodStmtEqual = true;
                                        }
                                    } else {
                                        flagMethodStmtEqual = false;
                                        break;
                                    }
                                } catch (Exception ex) {
                                    Utils.showMessage("Exception: " + ex);
                                }

                                try {

                                    if (iStatements[k] instanceof PsiTryStatementImpl && jStatements[k] instanceof PsiTryStatementImpl) {
                                        PsiTryStatement iTryStatement = (PsiTryStatement) iStatements[k];
                                        PsiTryStatement jTryStatement = (PsiTryStatement) jStatements[k];

                                        PsiStatement[] iPsiStatements = iTryStatement.getTryBlock().getStatements();
                                        PsiStatement[] jPsiStatements = jTryStatement.getTryBlock().getStatements();

                                        if (iPsiStatements.length != jPsiStatements.length) {
                                            flagMethodStmtEqual = false;
                                            break;
                                        } else {
                                            for (int ii = 0; ii < iPsiStatements.length; ii++) {
                                                if (iPsiStatements[ii].getClass().getName().equals(jPsiStatements[ii].getClass().getName())) {
                                                    if (iPsiStatements[ii] instanceof PsiDeclarationStatementImpl && jPsiStatements[ii] instanceof PsiDeclarationStatementImpl) {
                                                        if (iPsiStatements[ii].getFirstChild().getChildren()[1].getText().equals(jPsiStatements[ii].getFirstChild().getChildren()[1].getText())) {
                                                            if (iPsiStatements[ii].getFirstChild().getChildren()[7] != null && jPsiStatements[ii].getFirstChild().getChildren()[7] != null) {
                                                                if (iPsiStatements[ii].getFirstChild().getChildren()[7].getClass().getName()
                                                                        .equals(jPsiStatements[ii].getFirstChild().getChildren()[7].getClass().getName())) {

                                                                    if (iPsiStatements[ii].getFirstChild().getChildren()[7] instanceof PsiReferenceExpressionImpl
                                                                            && jPsiStatements[ii].getFirstChild().getChildren()[7] instanceof PsiReferenceExpressionImpl) {
                                                                        if (!iPsiStatements[ii].getFirstChild().getChildren()[7].getText()
                                                                                .equals(jPsiStatements[ii].getFirstChild().getChildren()[7].getText())) {
                                                                            flagMethodStmtEqual = false;
                                                                            break;
                                                                        }
                                                                    }
                                                                    if (iPsiStatements[ii].getFirstChild().getChildren()[7] instanceof PsiLiteralExpressionImpl
                                                                            && jPsiStatements[ii].getFirstChild().getChildren()[7] instanceof PsiLiteralExpressionImpl) {
                                                                        if (!iPsiStatements[ii].getFirstChild().getChildren()[7].getText()
                                                                                .equals(jPsiStatements[ii].getFirstChild().getChildren()[7].getText())) {
                                                                            flagMethodStmtEqual = false;
                                                                            break;
                                                                        }
                                                                    }

                                                                    if (iPsiStatements[ii].getFirstChild().getChildren()[7] instanceof PsiNewExpressionImpl
                                                                            && jPsiStatements[ii].getFirstChild().getChildren()[7] instanceof PsiNewExpressionImpl) {
                                                                        PsiNewExpressionImpl iNewExpression = (PsiNewExpressionImpl) iPsiStatements[ii].getFirstChild().getChildren()[7];
                                                                        PsiNewExpressionImpl jNewExpression = (PsiNewExpressionImpl) jPsiStatements[ii].getFirstChild().getChildren()[7];

                                                                        if (iNewExpression.getArgumentList().getExpressionCount() != jNewExpression.getArgumentList().getExpressionCount()) {
                                                                            flagMethodStmtEqual = false;
                                                                            break;
                                                                        }
                                                                        PsiExpression[] psiINewExpression = iNewExpression.getArgumentList().getExpressions();
                                                                        PsiExpression[] psiJNewExpression = jNewExpression.getArgumentList().getExpressions();
                                                                        for (int l = 0; l < psiINewExpression.length; l++) {

                                                                            if (psiINewExpression[l] instanceof PsiReferenceExpressionImpl ||
                                                                                    psiJNewExpression[l] instanceof PsiReferenceExpressionImpl) {
                                                                                if (!psiINewExpression[l].getClass().getName().equals(psiJNewExpression[l].getClass().getName())) {
                                                                                    flagMethodStmtEqual = false;
                                                                                    break;
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    if (!flagMethodStmtEqual) {
                                                                        break;
                                                                    }

                                                                    flagMethodStmtEqual = true;

                                                                } else {
                                                                    flagMethodStmtEqual = false;
                                                                    break;
                                                                }
                                                            }
                                                        } else {
                                                            flagMethodStmtEqual = false;
                                                            break;
                                                        }
                                                    }
                                                    if (!flagMethodStmtEqual) {
                                                        break;
                                                    }
                                                } else {
                                                    flagMethodStmtEqual = false;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!flagMethodStmtEqual) {
                                            break;
                                        }

                                        PsiCatchSection[] iPsiCatchSections = iTryStatement.getCatchSections();
                                        PsiCatchSection[] jPsiCatchSections = jTryStatement.getCatchSections();

                                        if (iPsiCatchSections.length == jPsiCatchSections.length) {
                                            for (int l = 0; l < iPsiCatchSections.length; l++) {
                                                if (iPsiCatchSections[l].getParameter().getText().equals(
                                                        jPsiCatchSections[l].getParameter().getText())) {
                                                    flagMethodStmtEqual = true;
                                                } else {
                                                    flagMethodStmtEqual = false;
                                                    break;
                                                }
                                            }
                                            if (!flagMethodStmtEqual) {
                                                break;
                                            }
                                        } else {
                                            flagMethodStmtEqual = false;
                                            break;
                                        }

                                    }

                                } catch (Exception ex) {
                                    Utils.showMessage("Exception: " + ex);
                                }
                            }


                            ///////

                            if (flagMethodStmtEqual) {
                                JavaExpressionsVisitor expressionsVisitorI = new JavaExpressionsVisitor(1);
                                psiMethods.get(i).accept(expressionsVisitorI);
                                JavaExpressionsVisitor expressionsVisitorJ = new JavaExpressionsVisitor(2);
                                psiMethods.get(j).accept(expressionsVisitorJ);

                                ArrayList<String> iExpressions = expressionsVisitorI.getMethodIExpressions();
                                ArrayList<String> jExpressions = expressionsVisitorJ.getMethodJExpressions();
                                ArrayList<Integer> iArguments = expressionsVisitorI.getNumberOfArgumentsInMethodI();
                                ArrayList<Integer> jArguments = expressionsVisitorJ.getNumberOfArgumentsInMethodJ();
                                ArrayList<String> iExpressionsType = expressionsVisitorI.getMethodIExpressionsType();
                                ArrayList<String> jExpressionsType = expressionsVisitorJ.getMethodJExpressionsType();
                                ArrayList<String> iBinaryExpressionsType = expressionsVisitorI.getMethodIBinaryExpressionsType();
                                ArrayList<String> jBinaryExpressionsType = expressionsVisitorJ.getMethodJBinaryExpressionsType();
                                ArrayList<String> iArgumentRuntimeClass = expressionsVisitorI.getArgumentIRuntimeClass();
                                ArrayList<String> jArgumentRuntimeClass = expressionsVisitorJ.getArgumentJRuntimeClass();


                                for (int k = 0; k < iExpressions.size(); k++) {
                                    if (!iExpressions.get(k).equals(jExpressions.get(k))) {
                                        isExpressionsEqual = false;
                                        break;
                                    } else {
                                        if (iArguments.get(k) != jArguments.get(k)) {
                                            isExpressionsEqual = false;
                                            break;
                                        } else {

                                            if (iArgumentRuntimeClass.size() == jArgumentRuntimeClass.size()) {
                                                for (int l = 0; l < iArgumentRuntimeClass.size(); l++) {
                                                    if (iArgumentRuntimeClass.get(l).equals("com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl") ||
                                                            jArgumentRuntimeClass.get(l).equals("com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl")) {
                                                        if (!iArgumentRuntimeClass.get(l).equals(jArgumentRuntimeClass.get(l))) {
                                                            isExpressionsEqual = false;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }

                                            if (!isExpressionsEqual) {
                                                isExpressionsEqual = false;
                                                break;
                                            }

                                            if (iExpressionsType.size() != jExpressionsType.size()) {
                                                isExpressionsEqual = false;
                                                break;
                                            }

                                            if (iBinaryExpressionsType.size() != jBinaryExpressionsType.size()) {
                                                isExpressionsEqual = false;
                                                break;
                                            } else {
                                                isExpressionsEqual = true;
                                            }

                                            if (!isExpressionsEqual) {
                                                isExpressionsEqual = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            ///////

                            if (flagMethodStmtEqual && isExpressionsEqual) {
                                numberOfMethodClones++;
                                File ccDirectory = new File(ccNewPath + "CodeClones");
                                if (!ccDirectory.exists()) {
                                    ccDirectory.mkdir();
                                }
                                codeClonesPath = ccDirectory.getPath();

                                File file = new File(codeClonesPath + "\\Reg" + regressionFileNumber + "CC" + fileNumCounter + ".java");
                                try {
                                    FileWriter fileWriter = new FileWriter(file, true);
                                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                    if (flagMethodIPrint) {
                                        numberOfMethodClones++;
                                        codeCloneMethodNames.add(psiMethods.get(i).getName());
                                        bufferedWriter.append("public class Reg" + regressionFileNumber + "CC" + fileNumCounter + " {\n\n");
                                        bufferedWriter.append(psiMethods.get(i).getText());
                                        bufferedWriter.append("\n");
                                        flagMethodIPrint = false;
                                    }
                                    bufferedWriter.append(psiMethods.get(j).getText());
                                    bufferedWriter.append("\n");
                                    methodName.add(psiMethods.get(j).getName());
                                    codeCloneMethodNames.add(psiMethods.get(j).getName());

                                    bufferedWriter.flush();
                                    fileWriter.close();
                                    bufferedWriter.close();
                                } catch (Exception ex) {
                                    Utils.showMessage("Exception: " + ex.getMessage());
                                }
                                flagIsWritten = true;
                            }
                        }
                    }
                }


                if (flagIsWritten) {
                    flagAreAnyCodeClonesGenerated = true;
                    File file = new File(codeClonesPath + "\\Reg" + regressionFileNumber + "CC" + fileNumCounter + ".java");
                    try {
                        FileWriter fileWriter = new FileWriter(file, true);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.append("\n}");
                        bufferedWriter.flush();
                        fileWriter.close();
                        bufferedWriter.close();
                    } catch (IOException ex) {
                        Utils.showMessage("Exception: " + ex.getMessage());
                    }
                }
            }

            if (!flagAreAnyCodeClonesGenerated) {
                Utils.showMessage("---> No code clones find...");
            } else {
                Utils.showMessage("---> Code Clones are generated.");
                Utils.showMessage("---> Number of Methods: " + numberOfMethods);
                Utils.showMessage("---> Number of Code Clones: " + numberOfMethodClones);
                try {
                    File methodsListDirectory = new File(ccNewPath + "MethodsList");
                    if (!methodsListDirectory.exists()) {
                        methodsListDirectory.mkdir();
                    }
                    String methodsListPath = methodsListDirectory.getPath();
                    FileWriter fileWriter = new FileWriter(new File(methodsListPath + "\\MethodsList" + regressionFileNumber + "-" + fileNumCounter + ".txt"));
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    for (int i = 0; i < codeCloneMethodNames.size(); i++) {
                        bufferedWriter.write(codeCloneMethodNames.get(i) + "\n");
                    }
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    fileWriter.close();
                } catch (Exception ex) {
                    Utils.showMessage(ex.getMessage());
                }
            }

        }


        Project project = psiJavaFile.getProject();

        if (psiJavaFile.getName().startsWith("Reg") && psiJavaFile.getName().contains("CC") && psiJavaFile.getName().endsWith(".java")) {
            for (PsiClass psiClass : psiClasses) {
                psiClass.accept(new JavaClassVisitor());
            }

            int regressionNum = Integer.parseInt(psiJavaFile.getName().substring(psiJavaFile.getName().indexOf("g") + 1, psiJavaFile.getName().indexOf("C")));
            File file = new File(ccNewPath);
            String regPath = file.getParentFile().getAbsolutePath();
            File regFile = new File(regPath + "\\RegressionTest" + regressionNum + ".java");
            File srcFile = new File(ccNewPath + psiJavaFile.getName());

            Path regressionPath = Paths.get(regFile.getAbsolutePath());
            try {
                List<String> lines = Files.readAllLines(regressionPath, StandardCharsets.UTF_8);
                Long count = Files.lines(regressionPath, Charset.defaultCharset()).count();
                int endLineNumber = count.intValue() - 1;
                if (lines.get(endLineNumber).equals("}")) {
                    lines.remove(endLineNumber);
                }

                String importStmt = "import org.junit.Assert;\n" +
                        "import org.junit.experimental.theories.DataPoints;\n" +
                        "import org.junit.experimental.theories.FromDataPoints;\n" +
                        "import org.junit.experimental.theories.Theories;\n" +
                        "import org.junit.experimental.theories.Theory;\n" +
                        "import org.junit.runner.RunWith;";
                if (lines.get(4).equals("")) {
                    lines.add(4, importStmt);
                }
                if (lines.get(6).startsWith("@FixMethodOrder")) {
                    lines.add(7, "@RunWith(Theories.class)");
                    Files.write(regressionPath, lines, StandardCharsets.UTF_8);
                }
            } catch (Exception ex) {
                Utils.showMessage(ex.getMessage());
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    try {
                        FileReader fileReader = new FileReader(srcFile);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            List<String> lines = Files.readAllLines(regressionPath, StandardCharsets.UTF_8);
                            lines.add(line);
                            Files.write(regressionPath, lines, StandardCharsets.UTF_8);
                        }

                    } catch (Exception ex) {
                        Utils.showMessage(ex.getMessage());
                    }
                });
            });

            counter = 0;

        }


        if (psiJavaFile.getName().startsWith("RegressionTest")) {

            if (codeCloneMethodNames.size() > 0) {
                for (int i = 0; i < codeCloneMethodNames.size(); i++) {
                    int finalI = i;
                    ApplicationManager.getApplication().invokeLater(() -> {
                        WriteCommandAction.runWriteCommandAction(project, () -> {

                            for (PsiClass psiClass : psiClasses) {
                                PsiMethod[] psiMethods = psiClass.getMethods();
                                for (PsiMethod psiMethod : psiMethods) {
                                    if (psiMethod.getName().equals(codeCloneMethodNames.get(finalI))) {
                                        psiMethod.delete();
                                    }
                                }
                            }

                        });
                    });
                }
            }

        }

    }


    @Override
    public void visitPackage(PsiPackage aPackage) {
        super.visitElement(aPackage);
        String nameStr = aPackage.getQualifiedName();
        Utils.showMessage("package " + nameStr);
    }

    @Override
    public void visitImportList(PsiImportList list) {
        super.visitElement(list);
        PsiImportStatementBase[] importStatements = list.getAllImportStatements();
        int importCount = importStatements.length;
//        Utils.showMessage("number of import statements " + importCount);
//        for (PsiImportStatementBase importStatement : importStatements) {
//            Utils.showMessage(importStatement.getImportReference().getQualifiedName());
//        }
    }

    private void copyFile(File src, File dest) {
        try {
            FileUtils.copyFile(src, dest);
        } catch (IOException e) {
            Utils.showMessage(e.getMessage());
        }
    }


    private PsiDirectory getDirectorySrc(PsiFile psiFile) {
        PsiDirectory psiDirectory = null;
        String packageName = ((PsiJavaFileImpl) psiFile).getPackageName();
        String[] arg = packageName.split("\\.");
        psiDirectory = psiFile.getContainingDirectory();

        for (int i = 0; i < arg.length; i++) {
            psiDirectory = psiDirectory.getParent();
            if (psiDirectory == null) {
                break;
            }
        }

        return psiDirectory;
    }

    private boolean isFilesGenerated(String dirPath) {
        while (true) {
            boolean flag = false;
            File checkDirectory = new File(dirPath);
            if (checkDirectory.list().length < 1) {
                continue;
            }
            File[] dirs = checkDirectory.listFiles();
            for (File dir : dirs) {
                if (!(dir.getName().startsWith("Reg") && dir.getName().contains("CodeClones"))) {
                    continue;
                } else {
                    File[] files = dir.listFiles();
                    for (File file : files) {
                        if (file.getName().startsWith("Reg") && file.getName().contains("CC")) {
                            flag = true;
                        } else {
                            continue;
                        }
                    }
                }
            }
            if (flag) {
                break;
            }
        }
        return true;
    }


}


//    PsiDirectory psiDirectory = getDirectorySrc(psiJavaFile);
//    PsiDirectory[] directories = psiDirectory.getSubdirectories();
//        Utils.showMessage("size of directories = " + directories.length);
//                for (PsiDirectory directs : directories) {
//                Utils.showMessage("Dirs: " + directs.getName());
//                if (directs.getName().equals("src")) {
//                Utils.showMessage("Size: " + directs.getSubdirectories().length);
//                for (PsiDirectory psiDirs : directs.getSubdirectories()) {
//                Utils.showMessage("psiDirs: " + psiDirs.getName());
//                if (psiDirs.getName().startsWith("Reg") && psiDirs.getName().contains("CodeClones")) {
//                Utils.showMessage("== " + psiDirs.getName());
//                for (PsiFile files : psiDirs.getFiles()) {
//                Utils.showMessage("File: " + files.getName());
//                if (files.getName().startsWith("Reg") && files.getName().contains("CC")) {
//                Utils.showMessage(files.getName());
//                }
//                }
//                }
//                }
//                }
//                }


//        Project project = psiJavaFile.getProject();
//        ApplicationManager.getApplication().invokeLater(() -> {
//
//        });


//        PsiJavaFile psiJavaFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(virtualFile);
//        PsiClass[] javaFileClasses = psiJavaFile.getClasses();