#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-IMMSYM.bash <instr> <IMM> <SYM> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_IMMSYM() {
    gen_header $1    
    echo "; @Init: \"data[$SYM] = $B, C = $C\"" >> $1
    echo "; @Result: \"data[$SYM] = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-IMMSYM.bash $1 $IMM $SYM $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

IMM=$2
SYM=$3
B=$4
C=$5
Bp=$6
Cp=$7
Np=$8
Zp=$9
Vp=${10}

get_fname $1_IMMSYM
INSTR=`./gen-instr.bash 4000 "$1 #$IMM, $SYM"`
gen_IMMSYM $fname "$INSTR"
