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

package com.sylvanaar.idea.Lua.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifierList;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaReferenceExpression;
import com.sylvanaar.idea.Lua.lang.psi.impl.LuaPsiElementImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaLocalDefinitionStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 6, 2010
 * Time: 10:00:19 AM
 */
public class LuaLocalDefinitionStatementImpl extends LuaPsiElementImpl implements LuaLocalDefinitionStatement, LuaStatementElement {
    public LuaLocalDefinitionStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaDeclaration[] getDeclarations() {
        return findChildByClass(LuaIdentifierList.class).getDeclarations();
    }

    @Override
    public LuaReferenceExpression[] getReferenceExprs() {
        return findChildByClass(LuaIdentifierList.class).getReferenceExprs();  // TODO
    }

    @Override
    public LuaReferenceExpression getInitializer(int idx) {
       LuaReferenceExpression[] r = getReferenceExprs();
       if (r == null) return null;

       if (getDeclarations().length <= r.length)
           return r[idx];

        return null;
    }

    @Override
    public LuaStatementElement replaceWithStatement(LuaStatementElement newCall) {
       return null;
    }

    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                   @NotNull ResolveState resolveState,
                                   PsiElement lastParent,
                                   @NotNull PsiElement place) {

        if (place.getParent().getParent().getParent().getParent() != this ) {
            final LuaDeclaration[] decls = getDeclarations();
            for (LuaDeclaration decl : decls) {
                if (!processor.execute(decl, resolveState)) return false;
            }
        }
        return true;
    }

}
