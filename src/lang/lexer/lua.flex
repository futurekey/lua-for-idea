package com.sylvanaar.idea.Lua.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

%%

//--- file: lua.l ---
/*
* lua.l - flex lexer for Lua 5.1
* Copyright: Same as Lua
*/

%class _LuaLexer
%implements FlexLexer, LuaTokenTypes

%unicode

%char
%line
%column


%function advance
%type IElementType

%eof{ return;
%eof}

%{
    int yyline, yychar, yycolumn;

    ExtendedSyntaxStrCommentHandler longCommentOrStringHandler = new ExtendedSyntaxStrCommentHandler();
%}

%init{
%init}

w           =   [ \t]+
nl          =   \r|\n|\r\n
name        =   [_a-zA-Z][_a-zA-Z0-9]*
n           =   [0-9]+
exp         =   [Ee][+-]?{n}
number      =   (({n}|{n}[.]{n}){exp}?|[.]{n}|{n}[.])
sep         =   =*


%x XLONGSTRING
%x XLONGSTRING_BEGIN
%x XSHORTCOMMENT
%x XLONGCOMMENT
%x XSTRINGQ
%x XSTRINGA
%x XSHORTCOMMENT_BEGIN

%%

/* Keywords */
"and"          { return AND; }
"break"        { return BREAK; }
"do"           { return DO; }
"else"         { return ELSE; }
"elseif"       { return ELSEIF; }
"end"          { return END; }
"false"        { return FALSE; }
"for"          { return FOR; }
"function"     { return FUNCTION; }
"if"           { return IF; }
"in"           { return IN; }
"local"        { return LOCAL; }
"nil"          { return NIL; }
"not"          { return NOT; }
"or"           { return OR; }
"repeat"       { return REPEAT; }
"return"       { return RETURN; }
"then"         { return THEN; }
"true"         { return TRUE; }
"until"        { return UNTIL; }
"while"        { return WHILE; }
{number}     { return NUMBER; }

--+\[?      { yypushback(yylength()); yybegin( XSHORTCOMMENT_BEGIN ); return advance(); }

"["{sep}"[" { longCommentOrStringHandler.setCurrentExtQuoteStart(yytext().toString()); yybegin( XLONGSTRING_BEGIN ); return LONGSTRING_BEGIN; }

"\""           { yybegin(XSTRINGQ);  return STRING; }
'            { yybegin(XSTRINGA); return STRING; }


"#!"         { yybegin( XSHORTCOMMENT ); return SHEBANG; }
{w}          { return WS; }
"..."        { return ELLIPSIS; }
".."         { return CONCAT; }
"=="         { return EQ; }
">="         { return GE; }
"<="         { return LE; }
"~="         { return NE; }
"-"          { return MINUS; }
"+"          { return PLUS;}
"*"          { return MULT;}
"%"          { return MOD;}
"/"          { return DIV; }
"="          { return ASSIGN;}
">"          { return GT;}
"<"          { return LT;}
"("          { return LPAREN;}
")"          { return RPAREN;}
"["          { return LBRACK;}
"]"          { return RBRACK;}
"{"          { return LCURLY;}
"}"          { return RCURLY;}
"#"          { return GETN;}
","          { return COMMA; }
";"          { return SEMI; }
":"          { return COLON; }
"."          { return DOT;}
"^"          { return EXP;}
{nl}         { return NEWLINE; }
\r           { return WS; }



<XSTRINGQ>
{
  \"\"       {return STRING;}
  \"         { yybegin(YYINITIAL); return STRING; }
  \\[abfnrt] {return STRING;}
  \\\n       {return STRING;}
  \\\"       {return STRING; }
  \\'        {return STRING;}
  \\"["      {return STRING;}
  \\"]"      {return STRING;}
   \\\\        { return STRING; }
  [\n\r]    { yybegin(YYINITIAL); return WRONG; }
  .          {return STRING;}
}

<XSTRINGA>
{
  ''          { return STRING; }
  '           { yybegin(YYINITIAL); return STRING; }
  \\[abfnrt] { return STRING; }
  \\\n        { return STRING; }
  \\\'          { return STRING; }
  \\'          { yybegin(YYINITIAL); return STRING; }
  \\"["       { return STRING; }
  \\"]"       { return STRING; }
  \\\\        { return STRING; }
  [\n\r]     { yybegin(YYINITIAL);return WRONG;  }
  .          { return STRING; }
}


<XLONGSTRING_BEGIN>
{
    [\n\r]     { return LONGSTRING; }
    .          { yypushback(1); yybegin(XLONGSTRING); return advance(); }
}


<XLONGSTRING>
{
  "]"{sep}"]"     { if (longCommentOrStringHandler.isCurrentExtQuoteStart(yytext())) {
                       yybegin(YYINITIAL); longCommentOrStringHandler.resetCurrentExtQuoteStart(); return LONGSTRING_END;
                       } else { yypushback(yytext().length()-1); }
                        return LONGSTRING;
                  }
                  
  [\n\r]     { return LONGSTRING; }
  .          { return LONGSTRING; }
}

<XSHORTCOMMENT_BEGIN>
{
  --+*"["{sep}"["  { longCommentOrStringHandler.setCurrentExtQuoteStart(yytext().toString());   yybegin(XLONGCOMMENT); return LONGCOMMENT_BEGIN; }
  
  .              { yybegin(XSHORTCOMMENT); return SHORTCOMMENT; }
}

<XSHORTCOMMENT>
{
  [\n\r]      {yybegin(YYINITIAL); return NEWLINE; }
  
  .          { return SHORTCOMMENT;}
}

<XLONGCOMMENT>
{
  "]"{sep}"]"     { if (longCommentOrStringHandler.isCurrentExtQuoteStart(yytext())) {
                       yybegin(YYINITIAL); longCommentOrStringHandler.resetCurrentExtQuoteStart(); return LONGCOMMENT_END;
                       }  else { yypushback(yytext().length()-1); }
                        return LONGCOMMENT;  }

  [\n\r]     { return LONGCOMMENT;}
  .          { return LONGCOMMENT;}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////      identifiers      ////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

{name}       { return NAME; }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Other ////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
.            { return WRONG; }