#
# Makefile for avrora source code and documentation
# --> Probably the dumbest makefile ever.
#

all: cck avrora jintgen

cck:
	javac -d bin `find src/cck -name '*.java'`

avrora:
	javac -d bin `find src/avrora src/cck -name '*.java'`

jintgen:
	javac5 -d bin `find src/jintgen src/cck -name '*.java'`

clean:
	rm -rf `find bin -name '*.class'` `find doc -name '*.html'`

doc: doc/index.html

doc/index.html:
	javadoc -breakiterator -sourcepath src -d doc `find src/avrora -name '*.java'`
