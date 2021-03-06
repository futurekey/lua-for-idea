/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.sylvanaar.idea.Lua.editor.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;


public abstract class BaseInspection extends LuaSuppressableInspectionTool {

  private final String m_shortName = null;
  protected static final String ASSIGNMENT_ISSUES = "Assignment issues";
  protected static final String CONFUSING_CODE_CONSTRUCTS = "Potentially confusing code constructs";
  protected static final String CONTROL_FLOW = "Control Flow";
  protected static final String PROBABLE_BUGS = "Probable bugs";
  protected static final String ERROR_HANDLING = "Error handling";
  protected static final String GPATH = "GPath inspections";
  protected static final String METHOD_METRICS = "Method Metrics";
  protected static final String THREADING_ISSUES = "Threading issues";
  protected static final String VALIDITY_ISSUES = "Validity issues";
  protected static final String ANNOTATIONS_ISSUES = "Annotations verifying";

  @NotNull
  @Override
  public String[] getGroupPath() {
    return new String[]{"Lua", getGroupDisplayName()};
  }

  @NotNull
  public String getShortName() {
    if (m_shortName == null) {
      final Class<? extends BaseInspection> aClass = getClass();
      @NonNls final String name = aClass.getName();
      return name.substring(name.lastIndexOf((int) '.') + 1,
          name.length() - "Inspection".length());
    }
    return m_shortName;
  }

  @Nullable BaseInspectionVisitor buildGroovyVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFly) {
    final BaseInspectionVisitor visitor = buildVisitor();
    visitor.setProblemsHolder(problemsHolder);
    visitor.setOnTheFly(onTheFly);
    visitor.setInspection(this);
    return visitor;
  }


  @Nullable
  protected String buildErrorString(Object... args) {
    return null;
  }

  protected boolean buildQuickFixesOnlyForOnTheFlyErrors() {
    return false;
  }

  @Nullable
  protected LuaFix buildFix(PsiElement location) {
    return null;
  }

  @Nullable
  protected LuaFix[] buildFixes(PsiElement location) {
    return null;
  }

  @Nullable
  public ProblemDescriptor[] checkFile(@NotNull PsiFile psiFile, @NotNull InspectionManager inspectionManager, boolean isOnTheFly) {
    if (!(psiFile instanceof LuaPsiFile)) {
      return super.checkFile(psiFile, inspectionManager, isOnTheFly);
    }
    final LuaPsiFile file = (LuaPsiFile) psiFile;

    final ProblemsHolder problemsHolder = new ProblemsHolder(inspectionManager, psiFile, isOnTheFly);
    final BaseInspectionVisitor visitor = buildGroovyVisitor(problemsHolder, isOnTheFly);
    file.accept(visitor);
    final List<ProblemDescriptor> problems = problemsHolder.getResults();
    if (problems == null) {
      return null;
    }
    return problems.toArray(new ProblemDescriptor[problems.size()]);

  }

  public boolean hasQuickFix() {
    final Method[] methods = getClass().getDeclaredMethods();
    for (final Method method : methods) {
      @NonNls final String methodName = method.getName();
      if ("buildFix".equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  protected abstract BaseInspectionVisitor buildVisitor();
}
