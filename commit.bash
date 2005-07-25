#!/bin/bash

##########################################################################
#               N O T   F O R   P U B L I C   U S E
##########################################################################
#
#  This bash script is used by the Avrora developers to commit changes
#  to CVS and is not intended to be used by users. It checks multiple
#  correctness criteria, runs the tests, and increments the build number.
#
##########################################################################

if [ "$1" = "" ]; then
    echo "Usage: commit.bash <log message>"
    exit
fi

JAVA_FILES=`find src -name '*.java'`
JJ_FILES=`find src -name '*.jj'`

CLASSBIN=/tmp/commit.bin/
OLDCLASSPATH=$CLASSPATH

ROOTPATH=`pwd`

# routine to check for successful CVS commit conditions
checkSuccess() {

    if [ "$?" = 0 ]; then
	echo " -> $1"
    else
	echo "*** STOP: $2 ***"
	$3
	cat /tmp/commit.reason
	if `test -e /tmp/oldVersion.java`; then
	    cp /tmp/oldVersion.java $ROOTPATH/Version.java
	    rm -f /tmp/oldVersion.java
	fi
	exit 1
    fi
}

assembleCommitErrors() {
    cp /tmp/commit.log /tmp/commit.reason
}

assembleMissing() {
    echo '*** The following files are not in CVS: ***' > /tmp/commit.reason
    echo `grep cvs\ log:\ nothing\ known\ about /tmp/commit.new | awk '{ print $6 }'` >> /tmp/commit.reason
}

assembleCompileErrors() {
    cp /tmp/commit.log /tmp/commit.reason
}

assembleTestErrors() {
    cp /tmp/commit.log /tmp/commit.reason
}

assembleCheckinList() {
    echo '*** The following files are in CVS but missing here: ***'
    grep cvs\ diff:\ cannot\ find /tmp/commit.log | awk '{ print $5}' >> /tmp/commit.reason
}

echo > /tmp/commit.reason

echo 'Checking for bash mode "avrora"'
test "$BASH_MODE" = "avrora"
checkSuccess 'OK' 'Please convert shell to avrora mode first.' 'echo'

echo 'Checking that all Java files are added to CVS...'
cvs log $JAVA_FILES &> /tmp/commit.new
checkSuccess 'All Java files here are in CVS.' 'There are Java files missing from CVS.' 'assembleMissing'

echo 'Checking that any changes need to be committed...'
cvs diff &> /tmp/commit.log
if `test "$?" = 0`; then
    echo " -> No changes to commit."
    exit 0
else
    test `grep cvs\ diff:\ cannot\ find /tmp/commit.log | wc -l` = 0
    checkSuccess 'No files are missing from CVS.' 'Files are missing here that are in CVS.' 'assembleCheckinList'
fi

VERSION_JAVA='src/avrora/Version.java'

echo Incrementing build number...
test -e $VERSION_JAVA
checkSuccess 'Version.java exists.' 'Version.java does not exist' 'echo'

cp $VERSION_JAVA /tmp/oldVersion.java
awk '{ if ( $1 == "public" && $3 == "int" && $4 == "commit" ) printf("    public final int commit = %d;\n",($6+1)); else print }' /tmp/oldVersion.java > $VERSION_JAVA


echo Attempting to compile complete project...

if `test -e $CLASSBIN`; then
    rm -rf $CLASSBIN
fi
mkdir $CLASSBIN
CLASSPATH=
javac -d $CLASSBIN -classpath $CLASSBIN $JAVA_FILES &> /tmp/commit.log
checkSuccess 'Compiled successfully.' 'There were compilation errors building the project.' 'assembleCompileErrors'

TESTS='interpreter probes disassembler interrupts timers'
for t in $TESTS; do

    echo Running tests in test/$t...
    test -d test/$t
    checkSuccess "test/$t exists." "test/$t does not exist." 'cat > /tmp/commit.reason'

    cd test/$t
    java -cp $CLASSBIN avrora.Main -action=test *.tst &> /tmp/commit.log
    checkSuccess 'All tests passed.' 'There were test case failures.' 'assembleTestErrors'
    cd $ROOTPATH
done

CLASSPATH=$OLDCLASSPATH

echo Attempting to commit to CVS...

# check that all Java files are added and nothing is missing, etc
cvs commit -m "$1" &> /tmp/commit.log
checkSuccess 'Commit completed successfully.' 'There were errors committing to CVS.' 'assembleCommitErrors'

cat /tmp/commit.log

echo Copying class files to $OLDCLASSPATH...
cp -r $CLASSBIN/* $OLDCLASSPATH/  &> /tmp/commit.log

checkSuccess 'Copy completed successfully.' 'There were errors copying the classes.' 'assembleCompileErrors'

rm -f /tmp/oldVersion.java