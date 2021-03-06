/*
* Copyright 2000-2005 JetBrains s.r.o.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.sylvanaar.idea.Lua.lang.psi.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.intellij.psi.scope.JavaScopeProcessorEvent;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiElement;
import com.sylvanaar.idea.Lua.lang.psi.LuaResolveResult;
import com.sylvanaar.idea.Lua.util.ResolverProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author max
 */
public class ResolveUtil {
    private static Logger log = Logger.getInstance("#Lua.ResolveUtil");
    private ResolveUtil() {
    }

    public static final PsiScopeProcessor.Event DECLARATION_SCOPE_PASSED = new PsiScopeProcessor.Event() {
    };

    public static boolean treeWalkDown(@NotNull final PsiScopeProcessor processor, @NotNull final PsiElement entrance,
                                       @Nullable final PsiElement maxScope,
                                       @NotNull final ResolveState state) {
        //first walk down all childs of the entrance
        if (!entrance.processDeclarations(processor, state, entrance, entrance)) return false;

        PsiElement prevParent = entrance;
        PsiElement scope = entrance.getNextSibling();
        if (scope == null) {
            scope = LuaPsiUtils.elementAfter(entrance);
        }

        //if not yet found, walkd down all elements after the current psi elemen in the tree
        while (scope != null) {
            if (!scope.processDeclarations(processor, state, prevParent, entrance)) return false;

            if (scope == maxScope) break;
            prevParent = scope;
            scope = prevParent.getNextSibling();
            if (scope == null) {
                scope = LuaPsiUtils.elementAfter(prevParent);
            }
        }

        return true;
    }
    @Nullable
    public static PsiElement treeWalkUp(PsiScopeProcessor processor, PsiElement elt, PsiElement lastParent, PsiElement place) {
        if (elt == null) return null;

        PsiElement cur = elt;
        do {
            // Walking Local Declaration With Assignment Statement last parent Expression List place LuaReferenceExpression (a)

//            log.info("Walking elt <" + elt + "> curr <" + cur + "> last parent <" + lastParent + "> place " + place);

            if (!cur.processDeclarations(processor, ResolveState.initial(), cur == elt ? lastParent : null, place)) {
                if (processor instanceof ResolveProcessor) {
                    return ((ResolveProcessor) processor).getResult();
                }
            }


            if (cur instanceof PsiFile) break;

            cur = cur.getPrevSibling();
        } while (cur != null);

//        final PsiElement func = processFunctionDeclarations(processor, elt.getContext());
//        if (func != null) return func;


//         log.info("Recursing Up elt <" + elt + "> context <" + elt.getContext() + ">  place " + place);
        return treeWalkUp(processor, elt.getContext(), elt, place);
    }

  public static boolean treeWalkUp(PsiElement place, PsiScopeProcessor processor, boolean processNonCodeMethods) {
    PsiElement lastParent = null;
    PsiElement run = place;

    final Project project = place.getProject();
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);

    while (run != null) {
      if (!run.processDeclarations(processor, ResolveState.initial(), lastParent, place)) return false;
//      if (processNonCodeMethods) {
//        if (run instanceof GrTypeDefinition) {
//          processNonCodeMethods(factory.createType(((GrTypeDefinition)run)), processor, project, place, false);
//        }
//        else if ((run instanceof GroovyFileBase) && ((GroovyFileBase)run).isScript()) {
//          final PsiClass psiClass = ((GroovyFileBase)run).getScriptClass();
//          if (psiClass != null) {
//            processNonCodeMethods(factory.createType(psiClass), processor, project, place, false);
//          }
//        }
//      }
      lastParent = run;
      run = run.getContext();
      processor.handleEvent(JavaScopeProcessorEvent.CHANGE_LEVEL, null);
    }

    return true;
  }



    
    
//    @Nullable
//    private static PsiElement processFunctionDeclarations(final @NotNull PsiScopeProcessor processor, final @Nullable PsiElement context) {
//        if (context != null) {
//            PsiElement cur = context.getLastChild();
//            while (cur != null) {
//                if (cur instanceof LuaFunctionDefinitionStatement) {
//                    if (!processor.execute(cur, ResolveState.initial())) {
//                        if (processor instanceof ResolveProcessor) {
//                            return ((ResolveProcessor) processor).getResult();
//                        }
//                    }
//                }
//                cur = cur.getPrevSibling();
//            }
//        }
//        return null;
//    }

    public static class ResolveProcessor implements PsiScopeProcessor {
        private static final Logger log = Logger.getInstance("#ResolveProcessor");
        private String myName;
        private PsiElement myResult = null;

        public ResolveProcessor(final String name) {
            myName = name;
        }

        public PsiElement getResult() {
            return myResult;
        }

        public boolean execute(PsiElement element, ResolveState stat) {

            if (element instanceof PsiNamedElement &&
                    element instanceof LuaPsiElement
                    ) {


                log.info("resolving " + myName + " checking " + element);
                if (myName.equals(((PsiNamedElement) element).getName())) {
                    myResult = element;

                    log.info("resolved to " + element);
                    return false;
                }
            }

//            if (element instanceof LuaFunctionDefinitionStatement) {
//                // TODO: Less of a kludge
//                log.info("resolving " + myName + " checking self for " + element);
//                if (((LuaFunctionDefinitionStatement)element).getIdentifier().getUsesSelf() && myName.equals("self")) {
//                    myResult = element;
//
//                    log.info("resolved to " + element);
//                    return false;
//                }
//            }

            return true;
        }

        public <T> T getHint(Key<T> hintClass) {
            return null;
        }

        public void handleEvent(Event event, Object associated) {
            log.info("handle event " + event + " object " + associated);
        }
    }

    public static class VariantsProcessor implements PsiScopeProcessor {
        private List<PsiElement> myNames = new ArrayList<PsiElement>();

        public VariantsProcessor() {
        }

        public PsiElement[] getResult() {
            return myNames.toArray(new PsiElement[myNames.size()]);
        }

        public boolean execute(PsiElement element, ResolveState state) {
            if (element instanceof PsiNamedElement &&
                    element instanceof LuaPsiElement
                    ) {
                myNames.add(element);
            }

            return true;
        }


        public <T> T getHint(Key<T> hintClass) {
            return null;
        }

        public void handleEvent(Event event, Object associated) {
        }
    }
//
//    public static LuaPsiElement resolveProperty(LuaPsiElement place, String name) {
//      ResolverProcessor processor = new ResolverProcessor(name, place,  new PsiType[0]);
//      return resolveExistingElement(place, processor, LuaVariable.class, LuaReferenceExpression.class);
//    }


  public static boolean processChildren(PsiElement element,
                                        PsiScopeProcessor processor,
                                        ResolveState substitutor,
                                        PsiElement lastParent,
                                        PsiElement place) {
    PsiElement run = lastParent == null ? element.getLastChild() : lastParent.getPrevSibling();
    while (run != null) {
      if (!run.processDeclarations(processor, substitutor, null, place)) return false;
      run = run.getPrevSibling();
    }

    return true;
  }

  @Nullable
  public static <T> T resolveExistingElement(LuaPsiElement place, ResolverProcessor processor, Class<? extends T>... classes) {
    treeWalkUp(place, processor, true);
    final LuaResolveResult[] candidates = processor.getCandidates();
    for (LuaResolveResult candidate : candidates) {
      final PsiElement element = candidate.getElement();
      if (element == place) continue;
      for (Class<? extends T> clazz : classes) {
        if (clazz.isInstance(element)) return (T)element;
      }
    }

    return null;
  }
}


