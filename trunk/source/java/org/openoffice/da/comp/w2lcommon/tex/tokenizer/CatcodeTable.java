/************************************************************************
 *
 *  CatcodeTable.java
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
 *  Version 1.2 (2009-06-11)
 *
 */

package org.openoffice.da.comp.w2lcommon.tex.tokenizer;

/** This class maintains a mapping from characters to catcodes.
 *  In this implementation, non-ascii characters always has the
 *  category Catcode.OTHER.
 */
public class CatcodeTable {
	private Catcode[] catcodes;
	
	/** Construct a new <code>CatcodeTable</code>, defining catcodes
	 * as by INITeX plus the additional catcodes defined by plain TeX
	 */
	public CatcodeTable() {
		catcodes = new Catcode[128];
		
		// First define all the catcodes from INITeX (Chapter 7 in "The TeXbook")
		for (int i=0; i<128; i++) {
			catcodes[i] = Catcode.OTHER;
		}
		for (char c='A'; c<='Z'; c++) {
			catcodes[c] = Catcode.LETTER;
		}
		for (char c='a'; c<='z'; c++) {
			catcodes[c] = Catcode.LETTER;
		}
		catcodes['\r']=Catcode.END_OF_LINE;
		catcodes[' ']=Catcode.SPACE;
		catcodes['\u0000']=Catcode.IGNORED; // ASCII NUL
		catcodes['\u007F']=Catcode.INVALID; // ASCII DEL
		catcodes['%']=Catcode.COMMENT;
		catcodes['\\']=Catcode.ESCAPE;
		
		// Then define all the catcodes from plain TeX (Appendix B in "The TeXbook")
		catcodes['{']=Catcode.BEGIN_GROUP;
		catcodes['}']=Catcode.END_GROUP;
		catcodes['$']=Catcode.MATH_SHIFT;
		catcodes['&']=Catcode.ALIGNMENT_TAB;
		catcodes['#']=Catcode.PARAMETER;
		catcodes['^']=Catcode.SUPERSCRIPT;
		catcodes['\u000B']=Catcode.SUPERSCRIPT; // ASCII VT ("uparrow")
		catcodes['_']=Catcode.SUBSCRIPT;
		catcodes['\u0001']=Catcode.SUBSCRIPT; // ASCII SOH ("downarrow")
		catcodes['\t']=Catcode.SPACE;
		catcodes['~']=Catcode.ACTIVE;
		catcodes['\u000C']=Catcode.ACTIVE; // ASCII FF	
	}
	
	/** Set the catcode of a character. The request is silently ignored
	 *  for all characters outside the ASCII character set 
	 * 
	 * @param c	the character
	 * @param cc the desired catcode
	 */
	public void set(char c, Catcode cc) {
		if (c<128) { catcodes[c]=cc; }
	}
	
	/** Get the catcode of a character. Characters outside the ASCII character
	 *  set always have the catcode Catcode.OTHER
	 * 
	 * @param c the character
	 * @return	the current catcode
	 */
	public Catcode get(char c) {
		if (c<128) { return catcodes[c]; }
		else { return Catcode.OTHER; }
	}

}
