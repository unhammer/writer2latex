/************************************************************************
 *
 *  OutputFile.java
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

import java.io.OutputStream;
import java.io.IOException;

/** An <code>OutputFile</code> represent a single file in a
 *  {@link ConverterResult}, which is output from a {@link Converter}
 *  implementation.
 */
public interface OutputFile {
    
    /** Writes the <code>OutputFile</code> to an <code>OutputStream</code>.
     * 
     *  @param  os  <code>OutputStream</code> to which the content should be written
     *  @throws  IOException  if any I/O error occurs
     */
    public void write(OutputStream os) throws IOException;

    /** Returns the file name of the <code>OutputFile</code>. This includes
     *  the file extension and may also include a relative path, always using
     *  / as separator.
     *
     *  @return  the file name of this <code>OutputFile</code>
     */
    public String getFileName();
    
    /** Get the MIME type of the <code>OutputFile</code>.
     * 
     *  @return string reprensentation of the MIME type
     */
    public String getMIMEType();
    
    /** Get the sequence number of this <code>OutputFile</code>.
     *  The master document has the sequence number 0.
     *  Other files which are part of the main document flow has a unique, positive sequence number.
     *  Auxiliary files like images always has the sequence number -1.
     * 
     *  @return the sequence number
     */
    public int getSequenceNumber();

}
