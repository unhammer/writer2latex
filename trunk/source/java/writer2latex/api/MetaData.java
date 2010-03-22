/************************************************************************
 *
 *  MetaData.java
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
 *  Copyright: 2002-2010 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 1.2 (2010-03-15)
 *
 */

package writer2latex.api;

/** This interface provides access to the predefined meta data of the
 *  source document (currently incomplete)
 */
public interface MetaData {
	/** Get the title of the source document
	 * 
	 * @return the title (may return an empty string)
	 */
	public String getTitle();
	
	/** Get the (main) language of the document
	 * 
	 * @return the language
	 */
	public String getLanguage();

}
