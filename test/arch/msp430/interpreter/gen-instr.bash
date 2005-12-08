#!/bin/bash

AWK_COMMAND="{ printf(\"%s %s         \\\"$2\\\"\n\", \$1, \$2) }"

echo ".org 0x$1" > /tmp/instr.s
echo "$2" >> /tmp/instr.s

msp430-as -o /tmp/instr.o /tmp/instr.s
msp430-objdump -zD /tmp/instr.o | grep $1 | awk -F"\t" "$AWK_COMMAND"
