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

package com.sylvanaar.idea.Lua.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiType;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.impl.expressions.LuaIdentifierImpl;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaDeclaration;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaLocalDefinitionStatement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Sep 3, 2010
 * Time: 12:38:19 AM
 */
public class LuaDeclarationImpl extends LuaIdentifierImpl implements LuaDeclaration, LuaIdentifier {
    public LuaDeclarationImpl(ASTNode node) {
        super(node);
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    @Override
    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return null;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public LuaPsiType getLuaType() {
        return LuaPsiType.VOID;
    }

    @Override
    public LuaIdentifier getNameSymbol() {
        return this;
    }

    @Override
    public String getDefinedName() {
        return getName();
    }


    
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState resolveState,
                                       PsiElement lastParent,
                                       @NotNull PsiElement place) {
        if (isLocal() && lastParent != null && !(lastParent instanceof LuaLocalDefinitionStatement))
           return processor.execute(this, resolveState);
        
        return true;
    }

    @Override
    public PsiElement setName(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiElement getNameIdentifierLua() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
