package ir.ac.um.CU2PT;


import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import ir.ac.um.CU2PT.visitor.JavaFileVisitor;


public class JavaConverter implements Runnable {
    private PsiElement psiElement;
    private Project project;
    private int numberOfItemsToBeProcessed;
    private int itemNumber = 0;

    public JavaConverter(Project project, PsiElement psiElement) {
        this.psiElement = psiElement;
        this.project = project;
    }

    @Override
    public void run() {
        Utils.showMessage(String.format("Conversion phase started.%n"));
        long start = System.currentTimeMillis();
        itemNumber = 0;
        try {
            Utils.showMessage(String.format("Estimating number of items to be processed ...%n"));
            numberOfItemsToBeProcessed = count(psiElement);

            convert();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        Utils.showMessage(String.format("Conversion phase finished. (total time: %s)%n", getFriendlyTime(finish - start)));
    }

    private void process(PsiJavaFile psiJavaFile) {
        itemNumber++;
        Utils.showMessage(String.format("[%d / %d] Processing %s ...%n",
                itemNumber, numberOfItemsToBeProcessed, psiJavaFile.getName()));
        psiJavaFile.accept(new JavaFileVisitor());
    }

    private void processDirectory(PsiDirectory psiDirectory) {
        PsiDirectory[] directories = psiDirectory.getSubdirectories();
        for (PsiDirectory directory : directories) {
            processDirectory(directory);
        }

        PsiFile[] innerFiles = psiDirectory.getFiles();
        for (PsiFile innerFile : innerFiles) {
            if (innerFile instanceof PsiJavaFileImpl) {
                PsiJavaFileImpl psiJavaFile = (PsiJavaFileImpl) innerFile;
                process(psiJavaFile);
            }
        }
    }

    private void processProject() {
        String projectName = project.getName();
        Utils.showMessage("Project name: " + projectName);
    }

    private int count(PsiElement element) {
        int count = 0;
        if (element instanceof PsiJavaFileImpl || element instanceof PsiClassImpl) {
            count = 1;
        } else if (element instanceof PsiJavaDirectoryImpl) {
            PsiJavaDirectoryImpl psiJavaDirectory = (PsiJavaDirectoryImpl) element;
            PsiElement[] children = psiJavaDirectory.getChildren();
            for (PsiElement child : children) {
                count += count(child);
            }
        }
        return count;
    }

    private void convert() {
        processProject();

        if (psiElement instanceof PsiJavaFileImpl || psiElement instanceof PsiClassImpl) {
            PsiFile file = psiElement.getContainingFile();
            if (file instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) file;
                process(psiJavaFile);
            }
        } else if (psiElement instanceof PsiJavaDirectoryImpl) {
            PsiJavaDirectoryImpl psiJavaDirectory = (PsiJavaDirectoryImpl) psiElement;
            processDirectory(psiJavaDirectory);
        }
    }


    private String getFriendlyTime(long timeInMilliSeconds) {
        long timeInSeconds = timeInMilliSeconds / 1000;
        if (timeInMilliSeconds < 1000) {
            return String.format("%d milli second%s", timeInMilliSeconds, (timeInMilliSeconds > 1 ? "s" : ""));
        } else if (timeInSeconds < 60) {
            return String.format("%d second%s", timeInSeconds, (timeInSeconds > 1 ? "s" : ""));
        } else if (timeInSeconds == 60) {
            return "1 minute";
        } else if (timeInSeconds < 60 * 60) {
            int minutes = (int) timeInSeconds / 60;
            int seconds = (int) timeInSeconds % 60;
            return String.format("%d minute%s %d second%s", minutes, (minutes > 1 ? "s" : ""), seconds, (seconds > 1 ? "s" : ""));
        } else if (timeInSeconds == 60 * 60) {
            return "1 hour";
        } else {
            int hours = (int) timeInSeconds / (60 * 60);
            int remaining = (int) timeInSeconds % (60 * 60);
            String part2 = "";
            if (remaining > 0) {
                part2 = getFriendlyTime(remaining * 1000);
            }
            return String.format("%d hour%s %s", hours, (hours > 1 ? "s" : ""), part2);
        }
    }

}
