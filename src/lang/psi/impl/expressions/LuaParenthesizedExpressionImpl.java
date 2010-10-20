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

package com.sylvanaar.idea.Lua.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaParenthesizedExpression;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Jul 3, 2010
 * Time: 11:08:50 AM
 */
public class LuaParenthesizedExpressionImpl extends LuaExpressionImpl implements LuaParenthesizedExpression {
    public LuaParenthesizedExpressionImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaExpression getEnclosedExpression() {
        return findChildByClass(LuaExpression.class);
    }

    @Override
    public LuaExpression removeParens() {
        LuaExpression newExpr = getEnclosedExpression();

        return (LuaExpression) replaceWithExpression(newExpr, true);
    }


    @Override
    public String toString() {
        return "Parens: (" + getEnclosedExpression().getText() + ")";
    }
}
