package com.xxxx.plugin.domain.service.impl;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.xxxx.plugin.domain.model.GenerateContext;
import com.xxxx.plugin.domain.model.GetObjConfigDO;
import com.xxxx.plugin.domain.model.SetObjConfigDO;
import com.xxxx.plugin.domain.service.AbstractMapToClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MapToClassImpl extends AbstractMapToClass {

    //补偿光标偏移量，用于计算正确的代码生成位置
    private int blank = 0;

    @Override
    protected GenerateContext getGenerateContext(Project project, DataContext dataContext, PsiFile psiFile) {
        // 基础信息
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        assert editor != null;
        Document document = editor.getDocument();

        // 封装生成对象上下文
        GenerateContext generateContext = new GenerateContext();
        generateContext.setProject(project);
        generateContext.setPsiFile(psiFile);
        generateContext.setDataContext(dataContext);
        generateContext.setEditor(editor);
        generateContext.setPsiElement(psiElement);
        generateContext.setOffset(editor.getCaretModel().getOffset());
        generateContext.setDocument(document);
        generateContext.setLineNumber(document.getLineNumber(generateContext.getOffset()));
        generateContext.setStartOffset(document.getLineStartOffset(generateContext.getLineNumber()));
        generateContext.setEditorText(document.getCharsSequence());

        return generateContext;
    }

    @Override
    protected SetObjConfigDO getSetObjConfigDo(GenerateContext generateContext) {
        blank = 0;
        String clazzParamName = null;
        PsiElement psiElement = generateContext.getPsiElement();
        PsiClass psiClass = null;
        //公共资源对象
        PsiFile psiFile = generateContext.getPsiFile();
        Editor editor = generateContext.getEditor();
        //鼠标定位到类
        if (psiElement instanceof PsiClass) {
            psiClass = (PsiClass) psiElement;
            //通过光标步长定位到属性名称
            int offsetStep = generateContext.getOffset() + 1;
            PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
            //element != 空/类的名称/空格,遍历得到属性名称
            while (null == elementAt || elementAt.getText().equals(psiClass.getName()) || elementAt instanceof PsiWhiteSpace) {
                elementAt = psiFile.findElementAt(++offsetStep);
            }
            clazzParamName = elementAt.getText();
        }

        //光标定位到局部变量
        if (psiElement instanceof PsiLocalVariable) {
            PsiLocalVariable psiLocalVariable = (PsiLocalVariable) psiElement;
            //直接获取属性名称
            clazzParamName = psiLocalVariable.getName();

            // 通过光标步长递进找到类名称
            int offsetStep = generateContext.getOffset() - 1;

            PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
            while (null == elementAt || elementAt.getText().equals(clazzParamName) || elementAt instanceof PsiWhiteSpace) {
                elementAt = psiFile.findElementAt(--offsetStep);
                if (elementAt instanceof PsiWhiteSpace) {
                    ++blank;
                }
            }
            String clazzName = elementAt.getText();
            PsiClass[] psiClasses = PsiShortNamesCache.getInstance(generateContext.getProject()).getClassesByName(clazzName, GlobalSearchScope.projectScope(generateContext.getProject()));
            psiClass = psiClasses[0];
            blank += psiClass.getName().length();

        }

        Map<String, String> paramTypeMap = new HashMap<>();
        // 遍历类所有字段
        for (PsiField field : psiClass.getFields()) {
            // 获取字段类型（含泛型信息）
            PsiType fieldType = field.getType();
            String fieldName = field.getName();
            // 获取类型名称（两种方式）
            String type = fieldType.getPresentableText(); // 简化名称（如 Map<String, Integer>）
            paramTypeMap.put(fieldName, type);
            
        }

        Pattern setMtd = Pattern.compile(setRegex);
        // 获取类的set方法并存放起来
        List<String> paramList = new ArrayList<>();
        Map<String, String> paramMtdMap = new HashMap<>();
        List<PsiClass> psiClassLinkList = getPsiClassLinkList(psiClass);
        for (PsiClass psi : psiClassLinkList) {
            List<String> methodsList = getMethods(psi, setRegex, "set");
            for (String methodName : methodsList) {
                // 替换属性
                String originParam = setMtd.matcher(methodName).replaceAll("$1");
                String param = originParam.substring(0, 1).toLowerCase() + originParam.substring(1);
                // 保存获取的属性信息
                paramMtdMap.put(param, methodName);
                paramList.add(param);
            }
        }
        return new SetObjConfigDO(clazzParamName, paramList, paramMtdMap, paramTypeMap);
    }

    @Override
    protected GetObjConfigDO getMapConvertAndMethod(GenerateContext generateContext, SetObjConfigDO setObjConfigDO) {
        List<String> paramList = setObjConfigDO.getParamList();
        Map<String, String> paramTypeMap = setObjConfigDO.getParamTypeMap();
        //获取剪切板信息
        String blankText = getSystemClipboardText().trim();
        
        //提取信息:按空格 分割
        String[] split = blankText.split("\\s");
        if (split.length < 2) {
            return new GetObjConfigDO(null, null, new HashMap<>());
        }
        
        // Map map
        String clazzName = split[0].trim();
        String clazzParam = split[1].trim();
        
        //拼装 map.get("param") -- 并适配其他数据类型
        Map<String, String> paramMtdMap = new HashMap<>();
        
        for (String param : paramList) {
            paramMtdMap.put(param, MapKeyConvert.toAnyType(param, clazzParam + ".get(\"" + param + "\")", paramTypeMap));
        }
        
        
        return new GetObjConfigDO(clazzName, clazzParam, paramMtdMap);
    }

    @Override
    protected void combineSetCode(GenerateContext generateContext, SetObjConfigDO setObjConfigDO, GetObjConfigDO getObjConfigDO) {
        Application application = ApplicationManager.getApplication();
        // 获取空格位置长度
        int distance = getWordStartOffset(generateContext.getEditorText(), generateContext.getOffset()) - generateContext.getStartOffset() - blank;
        
        application.runWriteAction(() -> {
            StringBuilder blankSpace = new StringBuilder();
            for (int i = 0; i < distance; i++) {
                blankSpace.append(" ");
            }
            int lineNumberCurrent = generateContext.getDocument().getLineNumber(generateContext.getOffset()) + 1;
            List<String> paramList = setObjConfigDO.getParamList();
            for (String param : paramList) {
                int lineStartOffset = generateContext.getDocument().getLineStartOffset(lineNumberCurrent++);

                // 检查下一行是否为空
                boolean nextLineIsEmpty = true;
                if (lineNumberCurrent < generateContext.getDocument().getLineCount()) {
                    int nextLineStart = generateContext.getDocument().getLineStartOffset(lineNumberCurrent);
                    int nextLineEnd = generateContext.getDocument().getLineEndOffset(lineNumberCurrent);
                    String nextLineContent = generateContext.getDocument().getText().substring(nextLineStart, nextLineEnd);
                    nextLineIsEmpty = nextLineContent.trim().isEmpty();
                }
                
                boolean isBlankLine = nextLineIsEmpty;

                WriteCommandAction.runWriteCommandAction(generateContext.getProject(), () -> {
                    if (!isBlankLine) {
                        generateContext.getDocument().insertString(lineStartOffset, "\n");
                    }
                    generateContext.getDocument().insertString(lineStartOffset, blankSpace + 
                            setObjConfigDO.getClazzParamName() + "." + setObjConfigDO.getParamMtdMap().get(param) + "(" + 
                            getObjConfigDO.getParamMtdMap().get(param) + ");");
                    generateContext.getEditor().getCaretModel().moveToOffset(lineStartOffset + 2);
                    generateContext.getEditor().getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                });
            }
        });
    }
}
