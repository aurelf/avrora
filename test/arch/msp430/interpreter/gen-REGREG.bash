#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-REGREG.bash <instr> <A> <B> <C> <A'> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_REGREG() {
    gen_header $1    
    echo "; @Init: \"r4 = $A, r5 = $B, C = $C\"" >> $1
    echo "; @Result: \"r4 = $Ap, r5 = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

A=$2
B=$3
C=$4
Ap=$5
Bp=$6
Cp=$7
Np=$8
Zp=$9
Vp=${10}

get_fname $1_REGREG
INSTR=`./gen-instr.bash 4000 "$1 r4, r5"`
gen_REGREG $fname "$INSTR"

