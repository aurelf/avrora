#!/bin/bash

if [ "$#" -lt 7 ]; then
    echo "Usage: gen-IMM.bash <instr> <IMM> <C> <Cp> <Np> <Zp> <Vp>"
    exit
fi

. ./msp430init.bash

gen_IMM() {
    gen_header $1    
    echo "; @Init: \"C = $C\"" >> $1
    echo "; @Result: \"C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

IMM=$2
C=$3
Cp=$4
Np=$5
Zp=$6
Vp=$7

get_fname $1_IMM
INSTR=`./gen-instr.bash 4000 "$1 #$IMM"`
gen_IMM $fname "$INSTR"
