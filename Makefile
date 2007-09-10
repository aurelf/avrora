#
# Makefile for avrora source code and documentation
# --> Probably the dumbest makefile ever.
#

all: avrora

cck:
	javac -source 1.4 -d bin `find src/cck -name '*.java'`

avrora:
	javac -source 1.4 -d bin `find src/avrora src/cck -name '*.java'`
	cp -r src/avrora/gui/images bin/avrora/gui

jintgen:
	javac -source 1.5 -d bin `find src/jintgen src/cck -name '*.java'`

clean:
	rm -rf bin/cck bin/avrora bin/jintgen doc/*.html doc/cck doc/avrora doc/jintgen

doc: doc/index.html

doc/index.html:
	javadoc -breakiterator -sourcepath src -d doc `find src/avrora -name '*.java'`
