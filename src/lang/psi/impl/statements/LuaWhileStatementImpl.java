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
import com.sylvanaar.idea.Lua.lang.psi.PsiLuaToken;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaConditionalExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaWhileStatement;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 10, 2010
 * Time: 10:40:55 AM
 */
public class LuaWhileStatementImpl extends LuaStatementElementImpl implements LuaWhileStatement {

    public LuaWhileStatementImpl(ASTNode node) {
        super(node);
    }

    public void accept(LuaElementVisitor visitor) {
      visitor.visitWhileStatement(this);
    }

    @Override
    public LuaConditionalExpression getCondition() {
        return null;
    }

    @Override
    public PsiLuaToken getLParenth() {
        return null;
    }

    @Override
    public PsiLuaToken getRParenth() {
        return null;
    }

    @Override
    public LuaStatementElement getBody() {
        return null;
    }


}
