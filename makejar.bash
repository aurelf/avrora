#!/bin/bash

if [ "$#" -lt 1 ]; then
	echo "Usage: makejar.bash <release>"
	exit 1
fi


RELEASE=$1
JARFILE=avrora-$RELEASE.jar

rm -rf bin/* $JARFILE

echo "# Running make clean..."
make clean
echo "  --> done."

echo "# Compiling source code for release $RELEASE..."
make avrora
echo "  --> done."

cd bin

JARFILE=avrora-$RELEASE.jar
echo "# Creating release jar $JARFILE..."
jar cmf MANIFEST.MF ../$JARFILE avrora cck
echo "  --> done."
