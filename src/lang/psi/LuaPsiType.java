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

package com.sylvanaar.idea.Lua.lang.psi;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Aug 25, 2010
 * Time: 7:06:05 AM
 */
public class LuaPsiType {
    public static LuaPsiType BOOLEAN = new LuaPsiType(1);
    public static LuaPsiType VOID = new LuaPsiType(0);

    int type;

    private LuaPsiType(int i) {
        type = i;
    }

    public boolean isAssignableFrom(LuaPsiType i) { return true; }

    public String getCanonicalText() {
            return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
