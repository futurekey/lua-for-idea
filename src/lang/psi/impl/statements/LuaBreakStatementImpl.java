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
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaBreakStatement;
import com.sylvanaar.idea.Lua.lang.psi.statements.LuaStatementElement;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Oct 1, 2010
 * Time: 11:48:13 PM
 */
public class LuaBreakStatementImpl extends LuaStatementElementImpl implements LuaBreakStatement {
    public LuaBreakStatementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public LuaStatementElement findTargetStatement() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
