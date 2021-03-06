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

package com.sylvanaar.idea.Lua.editor.highlighter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Query;
import com.sylvanaar.idea.Lua.lang.parser.LuaElementTypes;
import com.sylvanaar.idea.Lua.lang.psi.expressions.LuaIdentifier;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class LuaIdentifierHighlighterEditorComponent implements CaretListener, DocumentListener {
    static Logger log = Logger.getInstance("#LuaIdentifierHighlighterEditorComponent");
    //protected enum ELEMENT_TYPE {CLASS,METHOD,FIELD,PARAMETER,LOCAL}

    protected LuaIdentifierHighlighterAppComponent _appComponent = null;
    protected Editor _editor = null;
    protected ArrayList<RangeHighlighter> _highlights = null;
    protected ArrayList<Boolean> _forWriting = null;
    protected String _currentIdentifier = null;
    // protected ELEMENT_TYPE _elemType = null;
    protected int _startElem = -1;
    protected int _currElem = -1;
    protected int _declareElem = -1;
    protected boolean _ignoreEvents;
    protected boolean _identifiersLocked = false;
    protected PsiReferenceComparator _psiRefComp = null;

    public LuaIdentifierHighlighterEditorComponent(LuaIdentifierHighlighterAppComponent appComponent, Editor editor) {
        _appComponent = appComponent;
        _ignoreEvents = false;//!_appComponent.is_pluginEnabled();
        _editor = editor;
        _editor.getCaretModel().addCaretListener(this);
        _editor.getDocument().addDocumentListener(this);
        _psiRefComp = new PsiReferenceComparator();
    }

    //CaretListener interface implementation
    public void caretPositionChanged(final CaretEvent ce) {
        //Execute later so we are not searching Psi model while updating it
        //Fixes Idea 7 exception
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                handleCaretPositionChanged(ce);
            }
        });
    }

    protected void handleCaretPositionChanged(CaretEvent ce) {
//    if(_ignoreEvents)
//      return;
//    if(_identifiersLocked)
//      return;
        if (_editor == null)
            return;
        if (_editor.getProject() == null)
            return;
        if (_editor.getDocument() == null)
            return;
        PsiFile pFile = PsiDocumentManager.getInstance(_editor.getProject()).getPsiFile(_editor.getDocument());
        if (pFile == null)
            return;
        PsiElement pElem = pFile.findElementAt(_editor.getCaretModel().getOffset());
        if (pElem == null || pElem.getParent() == null || !(pElem.getParent() instanceof LuaIdentifier))
            pElem = null;
        if (pElem == null) {
            if (_highlights != null)
                clearState();
            return;
        }
        //We have a pElem
        //Check if different identifier than before
        if (_highlights != null) {
            int foundElem = -1;
            TextRange pElemRange = pElem.getTextRange();
            for (int i = 0; i < _highlights.size(); i++) {
                RangeHighlighter highlight = _highlights.get(i);
                if ((highlight.getStartOffset() == pElemRange.getStartOffset()) && (highlight.getEndOffset() == pElemRange.getEndOffset())) {
                    foundElem = i;
                    break;
                }
            }
            if (foundElem != -1) {
                if (foundElem != _currElem) {
                    moveIdentifier(foundElem);
                    _startElem = foundElem;
                }
                return;
            } else
                clearState();
        }
        _currentIdentifier = pElem.getText();
        log.info("identifier "+pElem.getText());
        ArrayList<PsiElement> elems = new ArrayList<PsiElement>();
        PsiReference pRef = pFile.findReferenceAt(_editor.getCaretModel().getOffset());
        if (pRef == null) {
            //See if I am a declaration so search for references to me
            PsiElement pElemCtx = pElem.getContext();
//      if(pElemCtx instanceof PsiClass)
//        _elemType = ELEMENT_TYPE.CLASS;
//      else if(pElemCtx instanceof PsiMethod)
//        _elemType = ELEMENT_TYPE.METHOD;
//      else if(pElemCtx instanceof PsiField)
//        _elemType = ELEMENT_TYPE.FIELD;
//      else if(pElemCtx instanceof PsiParameter)
//        _elemType = ELEMENT_TYPE.PARAMETER;
//      else if(pElemCtx instanceof PsiLocalVariable)
//        _elemType = ELEMENT_TYPE.LOCAL;
            
            if (pElemCtx == LuaElementTypes.VARIABLE)
                log.info("Caret on VARIABLE:" + pElem);
            else if (pElemCtx == LuaElementTypes.PARAMETER)
                log.info("Caret on PARAMETER:" + pElem);

            Query<PsiReference> q = ReferencesSearch.search(pElemCtx, GlobalSearchScope.fileScope(pFile));
            PsiReference qRefs[] = q.toArray(new PsiReference[0]);
            //Sort by text offset
            Arrays.sort(qRefs, _psiRefComp);
            for (PsiReference qRef : qRefs) {
                //Find child PsiIdentifier so highlight is just on it
                PsiElement qRefElem = qRef.getElement();
                LuaIdentifier qRefElemIdent = findChildIdentifier(qRefElem, pElem.getText());
                if (qRefElemIdent == null)
                    continue;
                //Skip elements from other files
                if (!areSameFiles(pFile, qRefElemIdent.getContainingFile()))
                    continue;
                //Check if I should be put in list first to keep it sorted by text offset
                if ((_declareElem == -1) && (pElem.getTextOffset() <= qRefElemIdent.getTextOffset())) {
                    elems.add(pElem);
                    _declareElem = elems.size() - 1;
                }
                elems.add(qRefElemIdent);
            }
            //If haven't put me in list yet, put me in last
            if (_declareElem == -1) {
                elems.add(pElem);
                _declareElem = elems.size() - 1;
            }
        } else {
            //Resolve to declaration
            PsiElement pRefElem;
            try {
                pRefElem = pRef.resolve();
            } catch (Throwable t) {
                pRefElem = null;
            }
            if (pRefElem != null) {
//        if(pRefElem instanceof PsiClass)
//          _elemType = ELEMENT_TYPE.CLASS;
//        else if(pRefElem instanceof PsiMethod)
//          _elemType = ELEMENT_TYPE.METHOD;
//        else if(pRefElem instanceof PsiField)
//          _elemType = ELEMENT_TYPE.FIELD;
//        else if(pRefElem instanceof PsiParameter)
//          _elemType = ELEMENT_TYPE.PARAMETER;
//        else if(pRefElem instanceof PsiLocalVariable)
//          _elemType = ELEMENT_TYPE.LOCAL;

            if (pRefElem == LuaElementTypes.VARIABLE)
                log.info("Resolved to VARIABLE:" + pElem);
            else if (pRefElem == LuaElementTypes.PARAMETER)
                log.info("Resolved to PARAMETER:" + pElem);                
            }
            if (pRefElem != null) {
                LuaIdentifier pRefElemIdent = findChildIdentifier(pRefElem, pElem.getText());
                if (pRefElemIdent != null) {
                    //Search for references to my declaration
                    Query<PsiReference> q = ReferencesSearch.search(pRefElemIdent.getContext(), GlobalSearchScope.fileScope(pFile));
                    PsiReference qRefs[] = q.toArray(new PsiReference[0]);
                    //Sort by text offset
                    Arrays.sort(qRefs, _psiRefComp);
                    for (PsiReference qRef : qRefs) {
                        //Find child PsiIdentifier so highlight is just on it
                        PsiElement qRefElem = qRef.getElement();
                        LuaIdentifier qRefElemIdent = findChildIdentifier(qRefElem, pElem.getText());
                        if (qRefElemIdent == null)
                            continue;
                        //Skip elements from other files
                        if (!areSameFiles(pFile, qRefElemIdent.getContainingFile()))
                            continue;
                        //Check if I should be put in list first to keep it sorted by text offset
                        if ((areSameFiles(pFile, pRefElemIdent.getContainingFile())) && (_declareElem == -1) && (pRefElemIdent.getTextOffset() <= qRefElemIdent.getTextOffset())) {
                            elems.add(pRefElemIdent);
                            _declareElem = elems.size() - 1;
                        }
                        elems.add(qRefElemIdent);
                    }
                    if (elems.size() == 0) {
                        //Should at least put the original found element at cursor in list
                        //Check if I should be put in list first to keep it sorted by text offset
                        if ((areSameFiles(pFile, pRefElemIdent.getContainingFile())) && (_declareElem == -1) && (pRefElemIdent.getTextOffset() <= pElem.getTextOffset())) {
                            elems.add(pRefElemIdent);
                            _declareElem = elems.size() - 1;
                        }
                        elems.add(pElem);
                    }
                    //If haven't put me in list yet, put me in last
                    if ((areSameFiles(pFile, pRefElemIdent.getContainingFile())) && (_declareElem == -1)) {
                        elems.add(pRefElemIdent);
                        _declareElem = elems.size() - 1;
                    }
                }
            } else {
                //No declaration found, so resort to simple string search
                PsiSearchHelper search = pElem.getManager().getSearchHelper();
                PsiElement idents[] = search.findCommentsContainingIdentifier(pElem.getText(), GlobalSearchScope.fileScope(pFile));
                for (PsiElement ident : idents)
                    elems.add(ident);
            }
        }
        _highlights = new ArrayList<RangeHighlighter>();
        _forWriting = new ArrayList<Boolean>();
        for (int i = 0; i < elems.size(); i++) {
            PsiElement elem = elems.get(i);
            TextRange range = elem.getTextRange();
            //Verify range is valid against current length of document
            if ((range.getStartOffset() >= _editor.getDocument().getTextLength()) || (range.getEndOffset() >= _editor.getDocument().getTextLength()))
                continue;
            boolean forWriting = isForWriting(elem);
            _forWriting.add(forWriting);
            RangeHighlighter rh;
            if (elem.getTextRange().equals(pElem.getTextRange())) {
                _startElem = i;
                _currElem = i;
                rh = _editor.getMarkupModel().addRangeHighlighter(range.getStartOffset(), range.getEndOffset(), getHighlightLayer(), getActiveHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
//        if(_appComponent.is_showInMarkerBar())
                rh.setErrorStripeMarkColor(getActiveHighlightColor(forWriting).getBackgroundColor());
            } else {
                rh = _editor.getMarkupModel().addRangeHighlighter(range.getStartOffset(), range.getEndOffset(), getHighlightLayer(), getHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
//        if(_appComponent.is_showInMarkerBar())
                rh.setErrorStripeMarkColor(getHighlightColor(forWriting).getBackgroundColor());
            }
//      if(_appComponent.is_showInMarkerBar())
            rh.setErrorStripeTooltip(_currentIdentifier + " [" + i + "]");
            _highlights.add(rh);
        }
    }

    protected boolean isForWriting(PsiElement elem) {
        PsiExpression parentExpression = PsiTreeUtil.getParentOfType(elem, PsiExpression.class);
        if (parentExpression != null)
            return (PsiUtil.isAccessedForWriting(parentExpression));
        else {
            PsiVariable parentVariable = PsiTreeUtil.getParentOfType(elem, PsiVariable.class);
            if (parentVariable != null) {
                PsiExpression initializer = parentVariable.getInitializer();
                return (initializer != null);
            }
        }
        return (false);
    }

    protected boolean areSameFiles(PsiFile editorFile, PsiFile candidateFile) {
        if ((editorFile == null) && (candidateFile == null))
            return (true);
        if (editorFile == null)
            return (true);
        if (candidateFile == null)
            return (true);
        String editorFileName = editorFile.getName();
        String candidateFileName = candidateFile.getName();
        if ((editorFileName == null) && (candidateFileName == null))
            return (true);
        if (editorFileName == null)
            return (true);
        if (candidateFileName == null)
            return (true);
        return (editorFileName.equals(candidateFileName));
    }

    protected LuaIdentifier findChildIdentifier(PsiElement parent, String childText) {
        if ((parent instanceof LuaIdentifier) && (parent.getText().equals(childText)))
            return ((LuaIdentifier) parent);
        //Packages don't implement getChildren yet they don't throw an exception.  It is caught internally so I can't catch it.
//    if(parent instanceof PsiPackage)
//      return(null);
        PsiElement children[] = parent.getChildren();
        if (children.length == 0)
            return (null);
        for (PsiElement child : children) {
            LuaIdentifier foundElem = findChildIdentifier(child, childText);
            if (foundElem != null)
                return (foundElem);
        }
        return (null);
    }

    protected boolean isHighlightEnabled() {
        return _appComponent.isEnabled();
//    if(_elemType == ELEMENT_TYPE.CLASS)
//      return(_appComponent.is_classHighlightEnabled());
//    else if(_elemType == ELEMENT_TYPE.METHOD)
//      return(_appComponent.is_methodHighlightEnabled());
//    else if(_elemType == ELEMENT_TYPE.FIELD)
//      return(_appComponent.is_fieldHighlightEnabled());
//    else if(_elemType == ELEMENT_TYPE.PARAMETER)
//      return(_appComponent.is_paramHighlightEnabled());
//    else if(_elemType == ELEMENT_TYPE.LOCAL)
//      return(_appComponent.is_localHighlightEnabled());
//    else
//      return(_appComponent.is_otherHighlightEnabled());
    }


    protected TextAttributes getActiveHighlightColor(boolean forWriting) {
        TextAttributes retVal = new TextAttributes();
        if (!isHighlightEnabled())
            return (retVal);
//    Color c;
//    if(_elemType == ELEMENT_TYPE.CLASS)
//      c = IdentifierHighlighterConfiguration.getColorFromString(_appComponent.get_classActiveHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.METHOD)
//      c = IdentifierHighlighterConfiguration.getColorFromString(_appComponent.get_methodActiveHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.FIELD)
//      c = IdentifierHighlighterConfiguration.getColorFromString(forWriting ? _appComponent.get_fieldWriteActiveHighlightColor() : _appComponent.get_fieldReadActiveHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.PARAMETER)
//      c = IdentifierHighlighterConfiguration.getColorFromString(forWriting ? _appComponent.get_paramWriteActiveHighlightColor() : _appComponent.get_paramReadActiveHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.LOCAL)
//      c = IdentifierHighlighterConfiguration.getColorFromString(forWriting ? _appComponent.get_localWriteActiveHighlightColor() : _appComponent.get_localReadActiveHighlightColor());
//    else
//      c = IdentifierHighlighterConfiguration.getColorFromString(_appComponent.get_otherActiveHighlightColor());
        retVal.setBackgroundColor(Color.GREEN);
        return (retVal);
    }

    protected TextAttributes getHighlightColor(boolean forWriting) {
        TextAttributes retVal = new TextAttributes();
        if (!isHighlightEnabled())
            return (retVal);
//    Color c;
//    if(_elemType == ELEMENT_TYPE.CLASS)
//      c = IdentifierHighlighterConfiguration.getColorFromString(_appComponent.get_classHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.METHOD)
//      c = IdentifierHighlighterConfiguration.getColorFromString(_appComponent.get_methodHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.FIELD)
//      c = IdentifierHighlighterConfiguration.getColorFromString(forWriting ? _appComponent.get_fieldWriteHighlightColor() : _appComponent.get_fieldReadHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.PARAMETER)
//      c = IdentifierHighlighterConfiguration.getColorFromString(forWriting ? _appComponent.get_paramWriteHighlightColor() : _appComponent.get_paramReadHighlightColor());
//    else if(_elemType == ELEMENT_TYPE.LOCAL)
//      c = IdentifierHighlighterConfiguration.getColorFromString(forWriting ? _appComponent.get_localWriteHighlightColor() : _appComponent.get_localReadHighlightColor());
//    else
//      c = IdentifierHighlighterConfiguration.getColorFromString(_appComponent.get_otherHighlightColor());
        retVal.setBackgroundColor(Color.YELLOW);
        return (retVal);
    }

    protected int getHighlightLayer() {
//    String highlightLayer = _appComponent.get_highlightLayer();
//    if(highlightLayer.equals("SELECTION"))
//      return(HighlighterLayer.SELECTION);
//    else if(highlightLayer.equals("ERROR"))
//      return(HighlighterLayer.ERROR);
//    else if(highlightLayer.equals("WARNING"))
//      return(HighlighterLayer.WARNING);
//    else if(highlightLayer.equals("GUARDED_BLOCKS"))
//      return(HighlighterLayer.GUARDED_BLOCKS);
//    else if(highlightLayer.equals("ADDITIONAL_SYNTAX"))
//      return(HighlighterLayer.ADDITIONAL_SYNTAX);
//    else if(highlightLayer.equals("SYNTAX"))
//      return(HighlighterLayer.SYNTAX);
//    else if(highlightLayer.equals("CARET_ROW"))
//      return(HighlighterLayer.CARET_ROW);
        return (HighlighterLayer.ADDITIONAL_SYNTAX);
    }

    //DocumentListener interface implementation
    public void beforeDocumentChange(DocumentEvent de) {
    }

    public void documentChanged(DocumentEvent de) {
        if (_ignoreEvents)
            return;
        caretPositionChanged(null);
    }

    protected void clearState() {
        if (_highlights != null) {
            for (RangeHighlighter highlight : _highlights)
                _editor.getMarkupModel().removeHighlighter(highlight);
        }
        _highlights = null;
        _forWriting = null;
        _currentIdentifier = null;
//    _elemType = null;
        _startElem = -1;
        _currElem = -1;
        _declareElem = -1;
//    unlockIdentifiers();
    }

    public void dispose() {
        clearState();
        _editor.getCaretModel().removeCaretListener(this);
        _editor.getDocument().removeDocumentListener(this);
        _editor = null;
    }

  public void repaint()
  {
    if(_highlights == null)
      return;
    for(int i = 0; i < _highlights.size(); i++) {
      RangeHighlighter rh = _highlights.get(i);
      boolean forWriting = _forWriting.get(i);
      int startOffset = rh.getStartOffset();
      int endOffset = rh.getEndOffset();
      _editor.getMarkupModel().removeHighlighter(rh);
      if(i == _currElem) {
        rh = _editor.getMarkupModel().addRangeHighlighter(startOffset,endOffset,getHighlightLayer(),getActiveHighlightColor(forWriting),HighlighterTargetArea.EXACT_RANGE);
//        if(_appComponent.is_showInMarkerBar())
          rh.setErrorStripeMarkColor(getActiveHighlightColor(forWriting).getBackgroundColor());
      } else {
        rh = _editor.getMarkupModel().addRangeHighlighter(startOffset,endOffset,getHighlightLayer(),getHighlightColor(forWriting),HighlighterTargetArea.EXACT_RANGE);
//        if(_appComponent.is_showInMarkerBar())
          rh.setErrorStripeMarkColor(getHighlightColor(forWriting).getBackgroundColor());
      }
//      if(_appComponent.is_showInMarkerBar())
        rh.setErrorStripeTooltip(_currentIdentifier + " [" + i + "]");
      _highlights.set(i,rh);
    }
  }
//
//  public void enablePlugin(boolean enable)
//  {
//    clearState();
//    _ignoreEvents = !enable;
//  }
//
//  public void startIdentifier()
//  {
//    if(_highlights == null)
//      return;
//    moveIdentifier(_startElem);
//    int offset = _highlights.get(_currElem).getStartOffset();
//    _editor.getCaretModel().moveToOffset(offset);
//    _editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
//  }
//
//  public void declareIdentifier()
//  {
//    if(_highlights == null)
//      return;
//    if(_declareElem == -1)
//      return;
//    moveIdentifier(_declareElem);
//    int offset = _highlights.get(_currElem).getStartOffset();
//    _editor.getCaretModel().moveToOffset(offset);
//    _editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
//  }
//
//  public void nextIdentifier()
//  {
//    if(_highlights == null)
//      return;
//    int newIndex = _currElem + 1;
//    if(newIndex == _highlights.size())
//      return;
//    moveIdentifier(newIndex);
//    int offset = _highlights.get(_currElem).getStartOffset();
//    _editor.getCaretModel().moveToOffset(offset);

    //    _editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
//  }
//
//  public void previousIdentifier()
//  {
//    if(_highlights == null)
//      return;
//    int newIndex = _currElem - 1;
//    if(newIndex == -1)
//      return;
//    moveIdentifier(newIndex);
//    int offset = _highlights.get(_currElem).getStartOffset();
//    _editor.getCaretModel().moveToOffset(offset);
//    _editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
//  }
//
//  public String getCurrentIdentifier()
//  {
//    return(_currentIdentifier);
//  }
//
//  public void renameIdentifier(String newIdentifier)
//  {
//    if(_highlights == null)
//      return;
//    _ignoreEvents = true;
//    try {
//      for(RangeHighlighter rh : _highlights) {
//        int startOffset = rh.getStartOffset();
//        int endOffset = rh.getEndOffset();
//        _editor.getDocument().replaceString(startOffset,endOffset,newIdentifier);
//      }
//    } catch(Throwable t) {
//      //Ignore
//    } finally {
//      _ignoreEvents = false;
//    }
//    //Simulate a caret position change so everything is up-to-date
//    unlockIdentifiers();
//    caretPositionChanged(null);
//  }
//
//  public void lockIdentifiers()
//  {
//    if(_identifiersLocked)
//      return;
//    _identifiersLocked = true;
//  }
//
//  public void unlockIdentifiers()
//  {
//    if(!_identifiersLocked)
//      return;
//    _identifiersLocked = false;
//    //Simulate a caret position change so everything is up-to-date
//    caretPositionChanged(null);
//  }
//
//  public boolean areIdentifiersLocked()
//  {
//    return(_identifiersLocked);
//  }
//
    protected void moveIdentifier(int index) {
        try {
            if (_currElem != -1) {
                RangeHighlighter rh = _highlights.get(_currElem);
                boolean forWriting = _forWriting.get(_currElem);
                int startOffset = rh.getStartOffset();
                int endOffset = rh.getEndOffset();
                _editor.getMarkupModel().removeHighlighter(rh);
                rh = _editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, getHighlightLayer(), getHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
//        if(_appComponent.is_showInMarkerBar()) {
                rh.setErrorStripeMarkColor(getHighlightColor(forWriting).getBackgroundColor());
                rh.setErrorStripeTooltip(_currentIdentifier + " [" + _currElem + "]");
//        }
                _highlights.set(_currElem, rh);
            }
            _currElem = index;
            RangeHighlighter rh = _highlights.get(_currElem);
            boolean forWriting = _forWriting.get(_currElem);
            int startOffset = rh.getStartOffset();
            int endOffset = rh.getEndOffset();
            _editor.getMarkupModel().removeHighlighter(rh);
            rh = _editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset, getHighlightLayer(), getActiveHighlightColor(forWriting), HighlighterTargetArea.EXACT_RANGE);
//      if(_appComponent.is_showInMarkerBar()) {
            rh.setErrorStripeMarkColor(getActiveHighlightColor(forWriting).getBackgroundColor());
            rh.setErrorStripeTooltip(_currentIdentifier + " [" + _currElem + "]");
//      }
            _highlights.set(_currElem, rh);
        } catch (Throwable t) {
            //Ignore
        }
    }

    protected class PsiReferenceComparator implements Comparator<PsiReference> {
        public int compare(PsiReference ref1, PsiReference ref2) {
            int offset1 = ref1.getElement().getTextOffset();
            int offset2 = ref2.getElement().getTextOffset();
            return (offset1 - offset2);
        }
    }
}
