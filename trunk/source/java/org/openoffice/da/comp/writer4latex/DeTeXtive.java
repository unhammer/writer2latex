/************************************************************************
 *
 *  DeTeXtive.java
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
 *  Version 1.2 (2009-06-19)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashSet;

import org.openoffice.da.comp.w2lcommon.tex.tokenizer.Mouth;
import org.openoffice.da.comp.w2lcommon.tex.tokenizer.Token;
import org.openoffice.da.comp.w2lcommon.tex.tokenizer.TokenType;

/** This class analyzes a stream and detects if it is a TeX stream.
 *  Currently it is able to identify LaTeX and XeLaTeX (ConTeXt and plain TeX may be
 *  added later).
 */
public class DeTeXtive {
	private Mouth mouth;
	private Token token;
	
	private HashSet<String> packages;
	
	/** Construct a new DeTeXtive
	 */
	public DeTeXtive() {
	}
	
	/** Detect the format of a given stream
	 * 
	 * @param is the input stream
	 * @return a string representing the detected format; null if the format is unknown.
	 * Currently the values "LaTeX", "XeLaTeX" are supported.
	 * @throws IOException if we fail to read the stream 
	 */
	public String deTeXt(InputStream is) throws IOException {
		// It makes no harm to assume that the stream uses ISO Latin1 - we only consider ASCII characters
		mouth = new Mouth(new InputStreamReader(is,"ISO8859_1"));
		token = mouth.getTokenObject();
		
		packages = new HashSet<String>();
		
		mouth.getToken();

		if (parseHeader() && parsePreamble()) {
			if (packages.contains("xunicode")) {
				return "XeLaTeX";
			}
			else {
				return "LaTeX";
			}
		}

		// Unknown format
		return null;
		
	}
	
	// The parser!
	
	// Parse a LaTeX header such as \documentclass[a4paper]{article}
	// Return true in case of success
	private boolean parseHeader() throws IOException {
		skipBlanks();
		if (token.isCS("documentclass") || token.isCS("documentstyle")) {
			// The first non-blank token is \documentclass or \documentstyle => could be a LaTeX document
			System.out.println("** Found "+token.toString());
			mouth.getToken();
			skipSpaces();
			// Skip options, if any
			if (token.is('[',TokenType.OTHER)) {
				skipOptional();
				skipSpaces();
			}
			if (token.getType()==TokenType.BEGIN_GROUP) {
				// Get class name
				String sClassName = parseArgumentAsString();
				System.out.println("** Found the class name "+sClassName);
				// Accept any class name of one or more characters
				if (sClassName.length()>0) { return true; }
			}
		}
		System.out.println("** Doesn't look like LaTeX; failed to get class name");
		return false;
	}
	
	// Parse a LaTeX preamble 
	// Return true in case of success (that is, \begin{document} was found)
	private boolean parsePreamble() throws IOException {
		while (token.getType()!=TokenType.ENDINPUT) {
			if (token.isCS("usepackage")) {
				// We collect the names of all used packages, but discard their options
				// (Recall that this is only relevant for LaTeX 2e)
				mouth.getToken();
				skipSpaces();
				if (token.is('[',TokenType.OTHER)) {
					skipOptional();
					skipSpaces();
				}
				String sName = parseArgumentAsString();
				System.out.println("** Found package "+sName);
				packages.add(sName);
			}
			else if (token.getType()==TokenType.BEGIN_GROUP) {
				// We ignore anything inside a group
				skipGroup();
			}
			else if (token.isCS("begin")) {
				// This would usually indicate the end of the preamble
				mouth.getToken();
				skipSpaces();
				if ("document".equals(parseArgumentAsString())) {
					System.out.println("Found \\begin{document}");
					return true;
				}
			}
			else {
				// Any other content in the preamble is simply ignored
				mouth.getToken();
			}
		}
		System.out.println("** Doesn't look like LaTeX; failed to find \\begin{document}");
		return false;
	}

	private void skipBlanks() throws IOException {
		while (token.getType()==TokenType.SPACE || token.isCS("par")) {
			mouth.getToken();
		}
	}
	
	private void skipSpaces() throws IOException {
		// Actually, we will never get two space tokens in a row
		while (token.getType()==TokenType.SPACE) {
			mouth.getToken();
		}
	}
	
	private void skipOptional() throws IOException {
		assert token.is('[', TokenType.OTHER);
		
		mouth.getToken(); // skip the [
		while (!token.is(']',TokenType.OTHER) && token.getType()!=TokenType.ENDINPUT) {
			if (token.getType()==TokenType.BEGIN_GROUP) {
				skipGroup();
			}
			else {
				mouth.getToken(); // skip this token
			}
		}
		mouth.getToken(); // skip the ]
	}
	
	private void skipGroup() throws IOException {
		assert token.getType()==TokenType.BEGIN_GROUP;

		mouth.getToken(); // skip the {
		while (token.getType()!=TokenType.END_GROUP && token.getType()!=TokenType.ENDINPUT) {
			if (token.getType()==TokenType.BEGIN_GROUP) {
				skipGroup();
			}
			else {
				mouth.getToken(); // skip this token
			}
		}
		mouth.getToken(); // skip the }
	}
	
	private String parseArgumentAsString() throws IOException {
		if (token.getType()==TokenType.BEGIN_GROUP) {
			// Argument is contained in a group
			mouth.getToken(); // skip the {
			StringBuilder sb = new StringBuilder();
			while (token.getType()!=TokenType.END_GROUP && token.getType()!=TokenType.ENDINPUT) {
				if (token.getType()!=TokenType.COMMAND_SEQUENCE) { 
					// should not include cs, ignore if it happens
					sb.append(token.getChar());
				}
				mouth.getToken();
			}
			mouth.getToken(); // skip the }
			return sb.toString();
		}
		else {
			// Argument is a single token
			String s = "";
			if (token.getType()!=TokenType.COMMAND_SEQUENCE) { 
				// should not include cs, ignore if it happens
				s = token.getString();
			}
			mouth.getToken();
			return s;
		}
	}


}
