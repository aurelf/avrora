#!/bin/bash

for file in $*; do

	echo $file
	avr-as -mmcu=atmega128 -o /tmp/$file.elf $file

	if [ "$?" = 0 ]; then
		avr-objdump -zhD /tmp/$file.elf > /tmp/$file.od
		echo '; @Harness: disassembler' > $file.tst
		echo '; @Result: PASS' >> $file.tst
		java avrora.Main -banner=false -action=odpp /tmp/$file.od >> $file.tst
	fi
done
