#!/bin/bash

if [ "$#" -lt 7 ]; then
    echo "Usage: gen-IREG.bash <instr> <ADDR> <A> <C> <Cp> <Np> <Zp> <Vp>"
    exit
fi

. ./msp430init.bash

gen_ADDR() {
    gen_header $1    
    echo "; @Init: \"r4 = $ADDR, data[$ADDR] = $A, C = $C\"" >> $1
    echo "; @Result: \"C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-IREG.bash $1 $ADDR $A $C $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

ADDR=$2
A=$3
C=$4
Cp=$5
Np=$6
Zp=$7
Vp=$8

get_fname $1_IREG
INSTR=`./gen-instr.bash 4000 "$1 @r4"`
gen_ADDR $fname "$INSTR"
