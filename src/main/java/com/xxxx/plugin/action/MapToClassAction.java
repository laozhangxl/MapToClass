package com.xxxx.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.ui.Messages;
import com.xxxx.plugin.application.IMapToClass;
import com.xxxx.plugin.domain.service.impl.MapToClassImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;

public class MapToClassAction extends AnAction {
    
    private IMapToClass mapToClass = new MapToClassImpl();
    

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        System.out.println("开始进行转换:");
        try {
            // 织入代码
            mapToClass.doGenerate(event.getProject(), event.getDataContext(), event.getData(LangDataKeys.PSI_FILE));
        } catch (Exception e) {
            Messages.showErrorDialog(event.getProject(), "请按规则进行转换", "错误提示");
        }
    }
}
