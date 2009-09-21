/************************************************************************
 *
 *  StreamGobbler.java
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
 *  Version 1.2 (2009-03-30)
 *
 */ 
 
package org.openoffice.da.comp.writer4latex;

import java.io.*;

class StreamGobbler extends Thread {
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }
    
    public void run() {
        try  {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            //String line=null;
            //while ( (line = br.readLine()) != null) {
            while ( br.readLine() != null) {
                // Do nothing...
                //System.out.println(type + ">" + line);
            }    
        }
        catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
}

