Compilation:

$ cd trunk
$ edit build.xml # set OFFICE_CLASSES and URE_CLASSES
$ ant oxt

Usage:

$ cd trunk/target
$ java -jar lib/writer2latex.jar -config=classes/writer2latex/latex/config/zotero.xml -latex in.odt out.tex
