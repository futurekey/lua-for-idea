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
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaReturnStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jun 12, 2010
 * Time: 10:42:37 PM
 */
public class LuaReturnStatementImpl  extends LuaStatementElementImpl implements LuaReturnStatement{
    public LuaReturnStatementImpl(ASTNode node) {
        super(node);
    }


    @Override
    public LuaExpression getReturnValue() {
        return findChildByClass(LuaExpression.class);
    }

    @Override
    public boolean isTailCall() {
         return getNode().getElementType() == LuaElementTypes.RETURN_STATEMENT_WITH_TAIL_CALL;
    }
}
