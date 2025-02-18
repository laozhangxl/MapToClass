package com.xxxx.plugin.domain.service;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.xxxx.plugin.application.IMapToClass;
import com.xxxx.plugin.domain.model.GenerateContext;
import com.xxxx.plugin.domain.model.GetObjConfigDO;
import com.xxxx.plugin.domain.model.SetObjConfigDO;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


public abstract class AbstractMapToClass implements IMapToClass {

    //正则:匹配set，get开头的方法
    protected final String setRegex = "set(\\w+)";
    protected final String getRegex = "get(\\w+)";
    
    
    @Override
    public void doGenerate(Project project, DataContext dataContext, PsiFile psiFile) {
        //获取上下文对象
        GenerateContext generateContext = this.getGenerateContext(project, dataContext, psiFile);
        //获取目标类的set对象，光标
        SetObjConfigDO setObjConfigDO = this.getSetObjConfigDo(generateContext);
        //setObjConfigDO转json输出
//        //对map对象进行转换，获取get方法集合，剪切板
        GetObjConfigDO getObjConfigDO = this.getMapConvertAndMethod(generateContext, setObjConfigDO);
//        //用于生成代码
        this.combineSetCode(generateContext, setObjConfigDO, getObjConfigDO);
        
        
    }
    
    protected abstract GenerateContext getGenerateContext(Project project, DataContext dataContext, PsiFile psiFile);
    
    protected abstract SetObjConfigDO getSetObjConfigDo(GenerateContext generateContext);
    
    protected abstract GetObjConfigDO getMapConvertAndMethod(GenerateContext generateContext, SetObjConfigDO setObjConfigDO);
    
    protected abstract void combineSetCode(GenerateContext generateContext, SetObjConfigDO setObjConfigDO, GetObjConfigDO getObjConfigDO);

    /**
     * 遍历继承链 得到 [父类，子类]
     * @param psiClass
     * @return
     */
    protected List<PsiClass> getPsiClassLinkList(PsiClass psiClass) {
        List<PsiClass> psiClassList = new ArrayList<>();
        PsiClass currentClass = psiClass;
        while (null != currentClass && !"Object".equals(currentClass.getName())) {
            psiClassList.add(currentClass);
            currentClass = currentClass.getSuperClass();
        }
        Collections.reverse(psiClassList);
        return psiClassList;
    }

    /**
     * 获取类下的方法：[setId, setName, setUserAccount, ....]
     * @param psiClass
     * @param regex
     * @param typeStr
     * @return
     */
    protected List<String> getMethods(PsiClass psiClass, String regex, String typeStr) {
        PsiMethod[] methods = psiClass.getMethods();
        List<String> methodList = new ArrayList<>();
        // 判断使用了 lombok，需要补全生成 get、set
        if (isUsedLombok(psiClass)) {
            PsiField[] fields = psiClass.getFields();
            for (PsiField field : fields) {
                String name = field.getName();
                //拼装: set + U + serName
                methodList.add(typeStr + name.substring(0, 1).toUpperCase() + name.substring(1));
            }
            for (PsiMethod method : methods) {
                String methodName = method.getName();
                if (Pattern.matches(regex, methodName) && !methodList.contains(methodName)) {
                    methodList.add(methodName);
                }
            }
            return methodList;
        }

        for (PsiMethod method : methods) {
            String methodName = method.getName();
            if (Pattern.matches(regex, methodName)) {
                methodList.add(methodName);
            }
        }

        return methodList;
    }

    private boolean isUsedLombok(PsiClass psiClass) {
        return null != psiClass.getAnnotation("lombok.Data");
    }

    /**
     * 获取剪切板数据
     * @return
     */
    public static String getSystemClipboardText() {
        String ret = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        Transferable contents = clipboard.getContents(null);
        if (contents == null) {
            return ret;
        }

        // 检查内容是否是文本类型
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                ret = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static int getWordStartOffset(CharSequence editorText, int cursorOffset) {
        if (editorText.length() == 0) {
            return 0;
        }

        if (cursorOffset > 0 && !Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))
                && Character.isJavaIdentifierPart(editorText.charAt(cursorOffset - 1))) {
            cursorOffset--;
        }

        if (Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))) {
            int start = cursorOffset;
            int end = cursorOffset;

            // 定位开始位置
            while (start > 0 && Character.isJavaIdentifierPart(editorText.charAt(start - 1))) {
                start--;
            }
            return start;

        }

        return 0;

    }

    
}
