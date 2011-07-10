package com.sylvanaar.idea.Lua.lang.psi.impl.symbols;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.sylvanaar.idea.Lua.lang.psi.LuaPsiManager;
import com.sylvanaar.idea.Lua.lang.psi.LuaReferenceElement;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaExpression;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolveResult;
import com.sylvanaar.idea.Lua.lang.psi.resolve.LuaResolver;
import com.sylvanaar.idea.Lua.lang.psi.resolve.ResolveUtil;
import com.sylvanaar.idea.Lua.lang.psi.resolve.completion.CompletionProcessor;
import com.sylvanaar.idea.Lua.lang.psi.symbols.LuaIdentifier;
import com.sylvanaar.idea.Lua.lang.psi.util.LuaPsiUtils;
import com.sylvanaar.idea.Lua.lang.psi.visitor.LuaElementVisitor;
import com.sylvanaar.idea.Lua.options.LuaApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


/**
 * TODO: implement all reference stuff...
 */
public abstract class LuaReferenceElementImpl extends LuaSymbolImpl implements LuaReferenceElement {
    public LuaReferenceElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(LuaElementVisitor visitor) {
        visitor.visitReferenceElement(this);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof LuaElementVisitor) {
            ((LuaElementVisitor) visitor).visitReferenceElement(this);
        } else {
            visitor.visitElement(this);
        }
    }


    public PsiElement getElement() {
        return this;
    }

    public PsiReference getReference() {
        return this;
    }


    public PsiElement getResolvedElement() {
        return resolve();
    }


    public TextRange getRangeInElement() {
        final PsiElement nameElement = getElement();
        return new TextRange(getTextOffset() - nameElement.getTextOffset(), nameElement.getTextLength());
    }

    @Nullable
    public PsiElement resolve() {
        ResolveResult[] results = getManager().getResolveCache().resolveWithCaching(this, RESOLVER, true, false);
        return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        return getManager().getResolveCache().resolveWithCaching(this, RESOLVER, true, incompleteCode);
    }

    private static final LuaResolver RESOLVER = new LuaResolver();

    @NotNull
    public String getCanonicalText() {
        return getName();
    }

     public PsiElement setName(@NotNull String s) {
        ((PsiNamedElement)getElement()).setName(s);

        resolve();

        return this;
     }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        ((PsiNamedElement)getElement()).setName(newElementName);
        resolve();
        return this;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        findChildByClass(LuaIdentifier.class).replace(element);
        return this;
    }

    public boolean isReferenceTo(PsiElement element) {
        if (LuaApplicationSettings.getInstance().RESOLVE_ALIASED_IDENTIFIERS) {
            return resolve() == element;
        } else {
//            return  ((PsiNamedElement)getElement()).getName().equals(((PsiNamedElement)element).getName());
            return getElement().getManager().areElementsEquivalent(element, resolve());
        }
        //return false;
    }

    @NotNull
    public Object[] getVariants() {
        CompletionProcessor variantsProcessor = new CompletionProcessor(this);
        ResolveUtil.treeWalkUp(this, variantsProcessor);

        Collection<String> names = LuaPsiManager.getInstance(getProject()).filteredGlobalsCache;
        if (names == null)
            return variantsProcessor.getResultElements();

        for (PsiElement e : variantsProcessor.getResultElements()) {
            final String name = ((PsiNamedElement) e).getName();
            if (name != null)
                names.add(name);
        }

        return names.toArray();
    }




    public boolean isSoft() {
        return false;
    }

    public boolean isAssignedTo() {
        return false;
    }

    public PsiElement replaceWithExpression(LuaExpression newCall, boolean b) {
        return LuaPsiUtils.replaceElement(this, newCall);
    }

    @Override
    public String getName() {
        return ((PsiNamedElement)getElement()).getName();
    }

    @Override
    public PsiElement resolveWithoutCaching(boolean ingnoreAlias) {

        boolean save = RESOLVER.getIgnoreAliasing();
        RESOLVER.setIgnoreAliasing(ingnoreAlias);
        LuaResolveResult[] results = RESOLVER.resolve(this, false);
        RESOLVER.setIgnoreAliasing(save);

        if (results != null && results.length > 0)
            return results[0].getElement();

        return null;
    }
}