package ir.ac.um.CU2PT.ui;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.ui.content.Content;
import ir.ac.um.CU2PT.Constants;
import ir.ac.um.CU2PT.JavaConverter;
import ir.ac.um.CU2PT.Utils;
import org.jetbrains.annotations.NotNull;

public class ConvertToRDFAction extends AnAction {
    private ConsoleView consoleView;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Navigatable navigatable = anActionEvent.getData(CommonDataKeys.NAVIGATABLE);
        if (navigatable != null) {
            Project project = anActionEvent.getProject();
            PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
            processSelectedElement(project, psiElement);
        }
    }

    private void processSelectedElement(Project project, PsiElement psiElement) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(Constants.PLUGIN_NAME);
        if (consoleView == null) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            Utils.setConsoleView(consoleView);
            Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), Constants.PLUGIN_NAME, true);
            toolWindow.getContentManager().addContent(content);
        }
        toolWindow.show(null);
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runReadAction(new JavaConverter(project, psiElement));
            }
        });

    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        // Set the availability based on whether a project is open and the selected item is a java file or a java directory
        Project project = anActionEvent.getProject();
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        anActionEvent.getPresentation().setEnabledAndVisible(project != null
                && (psiElement instanceof PsiJavaDirectoryImpl || psiElement instanceof PsiClassImpl));
    }

}
