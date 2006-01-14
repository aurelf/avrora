#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-SYMREG.bash <instr> <SYM> <A> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_SYMREG() {
    gen_header $1    
    echo "; @Init: \"data[$SYM] = $A, r5 = $B, C = $C\"" >> $1
    echo "; @Result: \"r5 = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-SYMREG.bash $1 $SYM $A $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

SYM=$2
A=$3
B=$4
C=$5
Bp=$6
Cp=$7
Np=$8
Zp=$9
Vp=${10}

get_fname $1_SYMREG
INSTR=`./gen-instr.bash 4000 "$1 $SYM, r5"`
gen_SYMREG $fname "$INSTR"
