/************************************************************************
 *
 *  ReplacementTrieNode.java
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License version 2.1, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA  02111-1307  USA
 *
 *  Copyright: 2002-2009 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.2 (2009-09-20)
 *
 */

package writer2latex.latex.i18n;

import java.util.Set;

/** This class contains a node in a trie of string -> LaTeX code replacements  
*/
public class ReplacementTrieNode {

    private char cLetter;
    private int nInputLength;
    private String sLaTeXCode = null;
    private int nFontencs = 0;
    private ReplacementTrieNode son = null;
    private ReplacementTrieNode brother = null;
	
    public ReplacementTrieNode(char cLetter, int nInputLength) {
        this.cLetter = cLetter;
        this.nInputLength = nInputLength;
    }
	
    public char getLetter() { return this.cLetter; }
	
    public int getInputLength() { return this.nInputLength; }

    public String getLaTeXCode() { return this.sLaTeXCode; }
	
    public int getFontencs() { return this.nFontencs; }
	
    protected void setLaTeXCode(String sLaTeXCode) {
        this.sLaTeXCode = sLaTeXCode;
    }
	
    protected void setFontencs(int nFontencs) {
        this.nFontencs = nFontencs;
    }
	
    protected ReplacementTrieNode getFirstChild() {
        return this.son;
    }
	
    protected ReplacementTrieNode getNextSibling() {
        return this.brother;
    }
	
    protected ReplacementTrieNode getChildByLetter(char cLetter) {
        ReplacementTrieNode child = this.getFirstChild();
        while (child!=null) {
            if (cLetter==child.getLetter()) { return child; }
            child = child.getNextSibling();
        }
        return null;
    }
	
    protected void appendChild(ReplacementTrieNode node) {
        if (son==null) { son = node; }
        else { son.appendSibling(node); }
    }

    protected void appendSibling(ReplacementTrieNode node) {
        if (brother==null) { brother = node; }
        else { brother.appendSibling(node); }
    }
	
    protected ReplacementTrieNode get(String sInput, int nStart, int nEnd) {
        if (nStart>=nEnd) { return null; }
        char c = sInput.charAt(nStart);
        ReplacementTrieNode child = this.getFirstChild();
        while (child!=null) {
            if (child.getLetter()==c) {
                if (child.getLaTeXCode()!=null) { return child; }
                else { return child.get(sInput,nStart+1,nEnd); }
            }
            child = child.getNextSibling();
        }
        return null;
    }
	
    protected void put(String sInput, String sLaTeXCode, int nFontencs) {
        char c = sInput.charAt(0);
        ReplacementTrieNode child = this.getChildByLetter(c);
        if (child==null) {
            child = new ReplacementTrieNode(c,this.getInputLength()+1);
            this.appendChild(child);
        }
        if (sInput.length()>1) {
            child.put(sInput.substring(1),sLaTeXCode,nFontencs);
        }
        else {
            child.setLaTeXCode(sLaTeXCode);
            child.setFontencs(nFontencs);
        }
    }
    
    protected void collectStrings(Set<String> strings, String sPrefix) {
    	ReplacementTrieNode child = this.getFirstChild();
    	while (child!=null) {
        	if (child.getLaTeXCode()!=null) {
        		strings.add(sPrefix+child.getLetter());
        		System.out.println("Found "+sPrefix+child.getLetter());
        	}
    		child.collectStrings(strings, sPrefix+child.getLetter());
    		child = child.getNextSibling();
    	}
    }
	
    public String toString() {
        String s = Character.toString(cLetter);
        if (brother!=null) { s+=brother.toString(); }
        if (son!=null) { s+="\nInputLength "+(nInputLength+1)+", "+son.toString(); }
        else { s+="\n"; }
        return s;
    }


}
