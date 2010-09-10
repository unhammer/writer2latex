This project contains a patched version of Writer2LaTeX that should be
able to handle Zotero references in OpenOffice.org Writer documents,
in order to import them into LyX for use with Zotero and LyZ.


Relevant links:
===============
- [the unpatched Writer2LaTeX](http://writer2latex.sourceforge.net/)
- [Zotero](http://www.zotero.org/)
- [LyX](http://www.lyx.org/)
- [LyZ (LyX extension for Zotero)](https://addons.mozilla.org/en-US/firefox/addon/56806/)


Compilation:
============

    $ cd trunk
    $ edit build.xml # set OFFICE_CLASSES and URE_CLASSES
    $ ant oxt

Also be sure to use [this patched version of LyZ](http://github.com/unhammer/lyz), 
in order to get the correct bibtex keys from Zotero. Compile it with:

    $ zip -r lyz.xpi ./*


Usage:
======

    $ cd trunk/target
    $ java -jar lib/writer2latex.jar -config=classes/writer2latex/latex/config/zotero.xml -latex in.odt out.tex

Now import the document as LaTeX from LyX, and change the citekey
format in LyZ to "writer2latex".

Issues:
=======

LyZ at the moment does not notice references that haven't been used
before in the document, so you'll have to first make one citation of
your whole library (which you can then delete).

There is a small chance of name-collisions if you cite from several
libraries, since we delete the library ID from the bibtex
key. However, I don't use this with more than one library myself, and
don't see a way to include the library ID in the bibtex key unless the
Zotero OOo plugin changes its output format.
