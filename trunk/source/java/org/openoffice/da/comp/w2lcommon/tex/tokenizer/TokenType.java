/************************************************************************
 *
 *  TokenType.java
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

/** This enumerates possible TeX tokens. According to chapter 7 in
 * "The TeX book", a token is either a character with an associated
 * catcode or a control sequence. We add "end of input" token as
 * a convenience. Not all catcodes can actually end up in a token,
 * so we only include the relevant ones.
 */
public enum TokenType {
	ESCAPE,
	BEGIN_GROUP,
	END_GROUP,
	MATH_SHIFT,
	ALIGNMENT_TAB,
	PARAMETER,
	SUPERSCRIPT,
	SUBSCRIPT,
	SPACE,
	LETTER,
	OTHER,
	ACTIVE,
	COMMAND_SEQUENCE,
	ENDINPUT;
}
