/************************************************************************
 *
 *  Mouth.java
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
 *  Version 1.2 (2009-06-18)
 *
 */

package org.openoffice.da.comp.w2lcommon.tex.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

enum State {
	N, // new line
	M, // middle of line
	S; // ignoring spaces
}

/** <p>The Mouth is the main class of this package. It is a tokenizer to TeX files: According to "The TeXBook", the
 *  "eyes" and "mouth" of TeX are responsible for turning the input to TeX into a sequence of tokens.
 *  We are not going to reimplement TeX, but rather providing a service for parsing high-level languages based on
 *  TeX (eg. LaTeX, ConTeXt). For this reason the tokenizer deviates slightly from TeX: We're not reading a stream
 *  of bytes but rather a stream of characters (which makes no difference for ASCII files).</p>
 *  
 *  <p>In tribute to Donald E. Knuths digestive metaphors, we divide the process in four levels</p>
 *  <ul>
 *  <li>The parser should provide a <em>pair of glasses</em> to translate the stream of bytes into a stream of characters</li>
 *  <li>The <em>eyes</em> sees the stream of characters as a sequence of lines</li>
 *  <li>The <em>mouth</em> chews a bit on the characters to turn them into tokens</li>
 *  <li>The <em>tongue</em> reports the "taste" of the token to the parser</li>
 *  </ul>
 */
public class Mouth {
	private Reader reader; // The input
	private CatcodeTable catcodes; // The current catcode table
	private char cEndlinechar; // The current value of \endlinechar
	private Token token; // The token object
	private State state; // The current state of the tokenizer
	private Eyes eyes; // sic!

	/** Construct a new <code>Mouth</code> based on a character stream
	 * 
	 * @param reader the character stream to tokenize
	 * @throws IOException if we fail to read the character stream
	 */
	public Mouth(Reader reader) throws IOException {
		this.reader = reader;
		catcodes = new CatcodeTable();
		cEndlinechar = '\r';
		token = new Token();
		state = State.N;
		eyes = new Eyes();
	}
	
	private class Eyes {
		private BufferedReader br; // The inpuy
		private String sLine; // The current line
		private int nLen; // The length of the current line
		private int nIndex; // The current index in the current line
		
		Eyes() throws IOException {
			br = new BufferedReader(reader);
			nextLine();
		}
		
		/** Start looking at the next line of input
		 * 
		 * @throws IOException if we fail to read the underlying stream
		 */
		void nextLine() throws IOException {
			sLine = br.readLine();
			if (sLine!=null) {
				nLen = sLine.length();
				nIndex = 0;
				// Delete trailing spaces
				while (nLen>0 && sLine.charAt(nLen-1)==' ') { nLen--; }
			}
			else { // end of stream
				nLen = 0;
				nIndex = 1;
			}
		}
		
		/** Test whether the eyes are looking at a character
		 * 
		 * @return true if the current line still has characters to look at
		 */
		boolean lookingAtChar() {
			return nIndex<=nLen;
		}
		
		/** Test whether the eyes a looking at a line
		 * 
		 * @return true if a current line is available
		 */ 
		boolean lookingAtLine() {
			return sLine!=null;
		}
		
		/** Get the character that the eyes currently sees
		 * 
		 * @return the character or U+FFFF if the eyes are not looking at a character
		 */
		char peekChar() {
			return getChar(false);
		}
		
		/** Get the character that the eyes currently sees and start looking at the next character
		 * 
		 * @return the character or U+FFFF if the eyes are not looking at a character
		 */
		char getChar() {
			return getChar(true);
		}
		
		private char getChar(boolean bMove) {
			if (nIndex<nLen) {
				char c = sLine.charAt(nIndex);
				if (catcodes.get(c)==Catcode.SUPERSCRIPT && nIndex+2<nLen && catcodes.get(sLine.charAt(nIndex+1))==Catcode.SUPERSCRIPT) {
					// Found ^^ and at least one more character
					char c1 = sLine.charAt(nIndex+2);
					if (nIndex+3<nLen && isHex(c1)) {
						char c2 = sLine.charAt(nIndex+3);
						if (isHex(c2)) {
							// Found ^^ and a lower case hexidecimal number
							if (bMove) { nIndex+=4; }
							char[] digits = {c1, c2};
							return (char) Integer.parseInt(new String(digits), 16);
						}
					}
					else if (c1<128) {
						// Found ^^ and an ASCII character
						if (bMove) { nIndex+=3; }
						if (c1<64) { return (char)(c1+64); }
						else { return (char)(c1-64); }
					}
				}
				// Found an ordinary character!
				if (bMove) { nIndex++; }
				return c;
			}
			else if (nIndex==nLen) {
				// Add \endlinechar at the end of the line
				if (bMove) { nIndex++; }
				return cEndlinechar;
			}
			else {
				// No more characters on the current line
				return '\uFFFF';
			}
		}
		
		private boolean isHex(char c) {
			return ('0'<=c && c<='9') || ('a'<=c && c<='z');
		}		
	}
	
	/** Get the currently used catcode table
	 * 
	 * @return the table
	 */
	public CatcodeTable getCatcodes() {
		return catcodes;
	}
	
	/** Set the catcode table. The catcode table can be changed at any time during tokenization.
	 * 
	 * @param catcodes the table
	 */
	public void setCatcodes(CatcodeTable catcodes) {
		this.catcodes = catcodes;
	}
	
	/** Return the current value of the \endlinechar (the character added to the end of each input line)
	 * 
	 * @return the character
	 */
	public char getEndlinechar() {
		return cEndlinechar;
	}
	
	/** Set a new \endlinechar (the character added to the end of each input line). The character can be changed at
	 * any time during tokenization.
	 * 
	 * @param c the character
	 */
	public void setEndlinechar(char c) {
		cEndlinechar = c;
	}
	
	/** Return the object used to store the current token (the "tongue" of TeX).
	 *  The same object is reused for all tokens, so for convenience the parser can keep a reference to the object.
	 *  If on the other hand the parser needs to store a token list, it must explicitly clone all tokens.
	 * 
	 * @return the token
	 */
	public Token getTokenObject() {
		return token;
	}
	
	/** Get the next token
	 * 
	 * @return the token (for convenince; the same object is returned by {@link Mouth#getTokenObject}).
	 * @throws IOException if we fail to read the underlying stream
	 */
	public Token getToken() throws IOException {
		while (eyes.lookingAtLine()) {
			while (eyes.lookingAtChar()) {
				char c = eyes.getChar();
				switch (catcodes.get(c)) {
				case ESCAPE:
					token.setType(TokenType.COMMAND_SEQUENCE);
					token.clearChars();
					// TODO: The description in the TeXBook is not completely clear, 
					// (as long as \r and no other character has catcode END_OF_LINE this should be correct)
					if (catcodes.get(eyes.peekChar())==Catcode.LETTER) {
						state = State.S;
						while (eyes.lookingAtChar() && catcodes.get(eyes.peekChar())==Catcode.LETTER) {
							token.addChar(eyes.getChar());
						}
					}
					else if (catcodes.get(eyes.peekChar())==Catcode.SPACE) {
						state = State.S;
						token.setChar(eyes.getChar());
					}
					else if (catcodes.get(eyes.peekChar())!=Catcode.END_OF_LINE) {
						state = State.M;
						token.setChar(eyes.getChar());
					}
					else {
						// Empty control sequence
						state = State.M;
					}
					return token;
				case BEGIN_GROUP:
					token.set(c, TokenType.BEGIN_GROUP);
					return token;
				case END_GROUP:
					token.set(c, TokenType.END_GROUP);
					return token;
				case MATH_SHIFT:
					token.set(c, TokenType.MATH_SHIFT);
					return token;
				case ALIGNMENT_TAB:
					token.set(c, TokenType.ALIGNMENT_TAB);
					return token;
				case END_OF_LINE:
					// Skip rest of line
					while (eyes.lookingAtChar()) { eyes.getChar(); }
					switch (state) {
					case N:
						// This terminates an empty line -> insert a \par
						token.setType(TokenType.COMMAND_SEQUENCE);
						token.clearChars();
						token.addChar('p');
						token.addChar('a');
						token.addChar('r');
						return token;
					case M:
						// Replace with a space token
						token.set(' ', TokenType.SPACE);
						return token;
					case S:
						// ignore the character
					}
					break;
				case PARAMETER:
					token.set(c, TokenType.PARAMETER);
					return token;
				case SUPERSCRIPT:
					token.set(c, TokenType.SUPERSCRIPT);
					return token;
				case SUBSCRIPT:
					token.set(c, TokenType.SUBSCRIPT);
					return token;
				case IGNORED:
					// ignore this character
					break;
				case SPACE:
					if (state==State.M) {
						state=State.S;
						token.set(' ', TokenType.SPACE);
						return token;
					}
					// In state N and S the space character is ignored
					break;
				case LETTER:
					token.set(c, TokenType.LETTER);
					return token;
				case OTHER:
					token.set(c, TokenType.OTHER);
					return token;
				case ACTIVE:
					token.set(c, TokenType.ACTIVE);
					return token;
				case COMMENT:
					// Skip rest of line
					while (eyes.lookingAtChar()) { eyes.getChar(); }
					break;
				case INVALID:
					// ignore this character (should issue an error message, but we ignore that)
				}
			}
			eyes.nextLine();
			state = State.N;
		}
		// Nothing more to read
		token.setType(TokenType.ENDINPUT);
		token.clearChars();
		return token;
	}
	
}
