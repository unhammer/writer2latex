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

Also be sure to use (this patch for LyZ)[http://gist.github.com/553037]
with LyZ, in order to get the correct bibtex keys from Zotero:

    $ bzr checkout http://bazaar.launchpad.net/~petrsimon/lyz/trunk lyz
    $ wget http://gist.github.com/raw/553037/2b644f6e1c54d0db5133535d8ca93f6097c137c1/lyz.writer2latex.patch
    $ cd lyz
    $ patch -p1 < ../lyz.writer2latex.patch
    $ zip -r lyz.xpi ./*


Usage:
======

    $ cd trunk/target
    $ java -jar lib/writer2latex.jar -config=classes/writer2latex/latex/config/zotero.xml -latex in.odt out.tex

Now import the document as LaTeX from LyX, and change the citekey
format in LyZ to "writer2latex".
