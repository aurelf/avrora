#
# Makefile for avrora source code and documentation
# --> Probably the dumbest makefile ever.
#

all:
	javac -d bin `find src/avrora -name '*.java'`

clean:
	rm -rf `find bin -name '*.class'` `find doc -name '*.html'`

doc: doc/index.html

doc/index.html:
	javadoc -breakiterator -sourcepath src -d doc `find src/avrora -name '*.java'`
