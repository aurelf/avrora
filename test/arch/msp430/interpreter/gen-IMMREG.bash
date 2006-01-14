#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-IMMREG.bash <instr> <IMM> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_IMMREG() {
    gen_header $1    
    echo "; @Init: \"r5 = $B, C = $C\"" >> $1
    echo "; @Result: \"r5 = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-IMMREG.bash $1 $IMM $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

IMM=$2
B=$3
C=$4
Bp=$5
Cp=$6
Np=$7
Zp=$8
Vp=$9

get_fname $1_IMMREG
INSTR=`./gen-instr.bash 4000 "$1 #$IMM, r5"`
gen_IMMREG $fname "$INSTR"

