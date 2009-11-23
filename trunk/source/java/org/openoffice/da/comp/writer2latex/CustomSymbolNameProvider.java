/**
 *  CustomSymbolNameProvider.java
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
 *  Version 1.2 (2009-11-09)
 * 
 */
package org.openoffice.da.comp.writer2latex;

import org.openoffice.da.comp.w2lcommon.helper.RegistryHelper;
import org.openoffice.da.comp.w2lcommon.helper.XPropertySetHelper;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** This class provides access to the names of all user defined symbols in math
 * 
 */
public class CustomSymbolNameProvider {
	Set<String> names;
	
	/** Construct a new <code>CustomSymbolNameProvider</code>
	 * 
	 * @param xContext the component context providing access to the api
	 */
	public CustomSymbolNameProvider(XComponentContext xContext) {
		names = new HashSet<String>();
		
		RegistryHelper registry = new RegistryHelper(xContext);
		try {
			// Prepare registry view
			Object view = registry.getRegistryView("/org.openoffice.Office.Math/",false);
			XPropertySet xProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,view);

			// Get the list of symbols
			Object symbols = XPropertySetHelper.getPropertyValue(xProps,"SymbolList");
	        XNameAccess xSymbols = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class,symbols);
	        String[] sNames = xSymbols.getElementNames();
	        int nCount = sNames.length;
	        for (int i=0; i<nCount; i++) {
                Object config = xSymbols.getByName(sNames[i]);
                XPropertySet xSymbolProps = (XPropertySet)
                    UnoRuntime.queryInterface(XPropertySet.class,config);
                if (!XPropertySetHelper.getPropertyValueAsBoolean(xSymbolProps,"Predefined")) {
                	names.add(sNames[i]);
                }
	        }

		}
		catch (Exception e) {
			// failed to get registry view, ignore
		}
	}
	
	/** Return the names of all user defined symbols (excluding the predefined symbols such as ALPHA etc)
	 * 
	 * @return a read only string set of symbols names
	 */
	public Set<String> getNames() {
		return Collections.unmodifiableSet(names);
	}
	
}
