#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-REG.bash <instr> <A> <C> <A'> <C'> <N'> <Z'> <V'>"
    exit
fi

. ./msp430init.bash

gen_REGREG() {
    gen_header $1    
    echo "; @Init: \"r4 = $A, C = $C\"" >> $1
    echo "; @Result: \"r4 = $Ap, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-REG.bash $1 $A $C $Ap $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

A=$2
C=$3
Ap=$4
Cp=$5
Np=$6
Zp=$7
Vp=$8

get_fname $1_REG
INSTR=`./gen-instr.bash 4000 "$1 r4"`
gen_REGREG $fname "$INSTR"
