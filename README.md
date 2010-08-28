This project contains a patched version of Writer2LaTeX that should be
able to handle Zotero references in OpenOffice.org Writer documents,
in order to import them into LyX for use with Zotero and LyZ.


Relevant links:
===============
- http://writer2latex.sourceforge.net/ the unpatched Writer2LaTeX
- http://www.zotero.org/ Zotero
- http://www.lyx.org/ LyX
- https://addons.mozilla.org/en-US/firefox/addon/56806/ LyZ (LyX extension for Zotero)


Compilation:
============
$ cd trunk
$ edit build.xml # set OFFICE_CLASSES and URE_CLASSES
$ ant oxt

Also be sure to use the patch for LyZ found at
http://gist.github.com/553037 with LyZ, in order to get the correct
bibtex keys from Zotero:

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
