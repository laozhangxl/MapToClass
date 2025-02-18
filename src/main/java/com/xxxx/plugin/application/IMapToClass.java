package com.xxxx.plugin.application;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;


public interface IMapToClass {
    
    void doGenerate(Project project, DataContext dataContext, PsiFile psiFile);
}
