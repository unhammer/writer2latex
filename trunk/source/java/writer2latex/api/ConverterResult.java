/************************************************************************
 *
 *  ConverterResult.java
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
 *  Version 1.2 (2010-03-28)
 *
 */
 
package writer2latex.api;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/** A <code>ConverterResult</code> represent a document, which is the result
 *  of a conversion performed by a <code>Converter</code>implementation.
 */
public interface ConverterResult {
    
    /** Get the master document
     *  Deprecated as of Writer2LaTeX 1.2: The master document is always the first document
     *  returned by the <code>iterator</code> 
     * 
     *  @return <code>OutputFile</code> the master document
     */
    @Deprecated public OutputFile getMasterDocument();

    /** Gets an <code>Iterator</code> to access all files in the
     *  <code>ConverterResult</code>. The iterator will return the master documents first
     *  in logical order (starting with the primary master document)
     *  @return  an <code>Iterator</code> of all files
     */
    public Iterator<OutputFile> iterator();
    
    /** Get the meta data associated with the source document
     *  @return the meta data
     */
    public MetaData getMetaData();
    
    /** Get the content table (based on headings) for this <code>ConverterResult</code>
     * 
     *  @return list view of the content
     */
    public List<ContentEntry> getContent();
    
    /** Get the entry which contains the table of contents
     * 
     *  @return the entry
     */
    public ContentEntry getTocFile();
    
    
    /** Get the entry which contains the list of tables
     * 
     *  @return the entry
     */
    public ContentEntry getLotFile();
    
    /** Get the entry which contains the list of figures
     * 
     *  @return the entry
     */
    public ContentEntry getLofFile();
    
    /** Get the entry which contains the alphabetical index
     * 
     *  @return the entry
     */
    public ContentEntry getIndexFile();
    
    /** Write all files of the <code>ConverterResult</code> to a directory.
     *  Subdirectories are created as required by the individual
     *  <code>OutputFile</code>s.
     *  @param dir the directory to write to (this directory must exist).
               If the parameter is null, the default directory is used
     *  @throws IOException if the directory does not exist or one or more files
     *  		could not be written
     */
    public void write(File dir) throws IOException;

}
