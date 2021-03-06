Writer2LaTeX source version 1.1.4
=================================

Writer2LaTeX is (c) 2002-2010 by Henrik Just.
The source is available under the terms and conditions of the
GNU LESSER GENERAL PUBLIC LICENSE, version 2.1.
Please see the file COPYING.TXT for details.


Overview
--------

The source of Writer2LaTeX consists of three major parts:

* A general purpose java library for converting OpenDocument files into LaTeX,
  BibTeX, xhtml, xhtml+MathML and EPUB
  This is to be found in the packages writer2latex.* and should only be used
  through the provided api writer2latex.api.*
* A command line utility writer2latex.Application
* A collection of components for OpenOffice.org
  These are to be found in the packages org.openoffice.da.comp.*
  
Currently parts of the source for Writer2LaTeX are somewhat messy and
undocumented. This situation is improving from time to time :-)


Third-party software
--------------------

Writer2LaTeX includes some classes from the OpenOffice.org project:
writer2latex.xmerge.* contains some classes which are part of the xmerge
project within OOo (some of the classes are slightly modified)
See copyright notices within the source files

Also, writer2latex.util.Base64 is Harald Harders public domain Base64 class


Building Writer2LaTeX
---------------------

Writer2LaTeX uses Ant version 1.5 or later (http://ant.apache.org) to build.


Some java libraries from OOo are needed to build the filter part of Writer2LaTeX,
these are jurt.jar, unoil.jar, ridl.jar and juh.jar.

To make these files available for the compiler, edit the file build.xml
as follows:

The lines
	<property name="OFFICE_CLASSES" location="/usr/share/java/openoffice" />
	<property name="URE_CLASSES" location="/usr/share/java/openoffice" />
should be modified to the directories where your OOo installation keeps these files

To build, open a command shell, navigate to the source directory and type

ant oxt

(this assumes, that ant is in your path; otherwise specifify the full path.)

In addition to oxt, the build file supports the following targets:
    all
        Build nearly everything
    compile
        Compile all file except the tests.        
    jar
        Create the standalone jar file.
    javadoc
        Create the javadoc documentation in target/javadoc.
    distro
	    Create distribution files 
    clean


Henrik Just, May 2010


Thanks to Michael Niedermair for writing the original ant build file
