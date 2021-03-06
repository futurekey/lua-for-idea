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

package com.sylvanaar.idea.Lua.lang.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.sylvanaar.idea.Lua.lang.lexer.LuaElementType;
import com.sylvanaar.idea.Lua.lang.lexer.LuaTokenTypes;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 3:54:46 PM
 */
public interface LuaElementTypes extends LuaTokenTypes {
    IElementType EMPTY_INPUT = new LuaElementType("empty input");

    IElementType FUNCTION_DEFINITION = new LuaElementType("Function Definition");

    IElementType LOCAL_NAME = new LuaElementType("local name");
    IElementType LOCAL_NAME_DECL = new LuaElementType("local name declaration");

    IElementType GLOBAL_NAME = new LuaElementType("global name");
    IElementType FIELD_NAME = new LuaElementType("field name");
    IElementType GETSELF = new LuaElementType("get self");
    IElementType GETTABLE = new LuaElementType("get table");

    IElementType TABLE_INDEX = new LuaElementType("table index");
    IElementType KEY_ASSIGNMENT = new LuaElementType("keyed field initializer");
    IElementType IDX_ASSIGNMENT = new LuaElementType("indexed field initializer");

    IElementType TOKEN_OR_KEYWORD = new LuaElementType("Token or Keyword");

    IElementType REFERENCE = new LuaElementType("Reference");

    IElementType VARIABLE = new LuaElementType("Variable");
    IElementType IDENTIFIER_LIST = new LuaElementType("Identifier List");

    IElementType EXPR = new LuaElementType("Expression");
    IElementType EXPR_LIST = new LuaElementType("Expression List");

    IElementType LITERAL_EXPRESSION = new LuaElementType("Literal Expression");

    IElementType TABLE_CONSTUCTOR = new LuaElementType("Table Constructor");
    IElementType FUNCTION_CALL_ARGS = new LuaElementType("Function Call Args");
    IElementType FUNCTION_CALL = new LuaElementType("Function Call Statement");
    IElementType FUNCTION_CALL_EXPR = new LuaElementType("Function Call Expression");
    IElementType ANONYMOUS_FUNCTION_EXPRESSION = new LuaElementType("Anonymous function expression");

    IElementType ASSIGN_STMT = new LuaElementType("Assignment Statement");
    IElementType CONDITIONAL_EXPR = new LuaElementType("Conditional Expression");

    IElementType LOCAL_DECL_WITH_ASSIGNMENT = new LuaElementType("Local Declaration With Assignment Statement");
    IElementType LOCAL_DECL = new LuaElementType("Local Declaration ");

    IElementType FUNCTION_IDENTIFIER = new LuaElementType("Function identifier");
    IElementType SELF_PARAMETER = new LuaElementType("Implied parameter (self)");

    TokenSet FUNCTION_IDENTIFIER_SET = TokenSet.create(FUNCTION_IDENTIFIER);//, FUNCTION_IDENTIFIER_NEEDSELF);

    IElementType BLOCK = new LuaElementType("Block");

    IElementType UNARY_EXP = new LuaElementType("UnExp");
    IElementType BINARY_EXP = new LuaElementType("BinExp");
    IElementType UNARY_OP = new LuaElementType("UnOp");
    IElementType BINARY_OP = new LuaElementType("BinOp");

    IElementType DO_BLOCK = new LuaElementType("Do Block");

    IElementType ANON_FUNCTION_BLOCK = new LuaElementType("Anonymous Function Block");
    IElementType WHILE_BLOCK = new LuaElementType("While Block");
    IElementType GENERIC_FOR_BLOCK = new LuaElementType("Generic For Block");
    IElementType IF_THEN_BLOCK = new LuaElementType("If-Then Block");
    IElementType NUMERIC_FOR_BLOCK = new LuaElementType("Numeric For Block");

    TokenSet EXPRESSION_SET = TokenSet.create(LITERAL_EXPRESSION, VARIABLE, BINARY_EXP, UNARY_EXP, EXPR);
    IElementType RETURN_STATEMENT = new LuaElementType("Return statement");
    IElementType RETURN_STATEMENT_WITH_TAIL_CALL = new LuaElementType("Tailcall Return statement");

    TokenSet BLOCK_SET = TokenSet.create(FUNCTION_DEFINITION, ANON_FUNCTION_BLOCK, WHILE_BLOCK,
            GENERIC_FOR_BLOCK, IF_THEN_BLOCK, NUMERIC_FOR_BLOCK);

    IElementType LOCAL_FUNCTION = new LuaElementType("local function def");
    IElementType PARAMETER = new LuaElementType("function parameters");
    IElementType PARAMETER_LIST = new LuaElementType("function parameter");
}
