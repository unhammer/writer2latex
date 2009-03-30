/************************************************************************
 *
 *  XhtmlStyleMap.java
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
 *  Copyright: 2002-2003 by Henrik Just
 *
 *  All Rights Reserved.
 * 
 *  Version 0.3.2 (2003-11-25)
 *
 */

package writer2latex.xhtml;

import java.util.Hashtable;
import java.util.Enumeration;

public class XhtmlStyleMap {
    private Hashtable<String, String> blockElement = new Hashtable<String, String>();
    private Hashtable<String, String> blockCss = new Hashtable<String, String>();
    private Hashtable<String, String> element = new Hashtable<String, String>();
    private Hashtable<String, String> css = new Hashtable<String, String>();
	
    public void put(String sName, String sBlockElement, String sBlockCss, String sElement, String sCss) {
        blockElement.put(sName,sBlockElement);
        blockCss.put(sName,sBlockCss);
        element.put(sName,sElement);
        css.put(sName,sCss);
    }
	
    public boolean contains(String sName) {
        return sName!=null && element.containsKey(sName);
    }
	
    public String getBlockElement(String sName) {
        return blockElement.get(sName);
    }

    public String getBlockCss(String sName) {
        return blockCss.get(sName);
    }
	
    public String getElement(String sName) {
        return element.get(sName);
    }

    public String getCss(String sName) {
        return css.get(sName);
    }
	
    public Enumeration<String> getNames() {
        return element.keys();
    }
	
}
