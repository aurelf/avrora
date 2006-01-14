#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-IREGSYM.bash <instr> <ADDR> <SYMB> <A> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_IREGSYM() {
    gen_header $1    
    echo "; @Init: \"data[$ADDR] = $A, r4 = $ADDR, data[$SYMB] = $B, C = $C\"" >> $1
    echo "; @Result: \"data[$SYMB] = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-IREGSYM.bash $1 $ADDR $SYMB $A $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

ADDR=$2
SYMB=$3
A=$4
B=$5
C=$6
Bp=$7
Cp=$8
Np=$9
Zp=${10}
Vp=${11}

get_fname $1_IREGSYM
INSTR=`./gen-instr.bash 4000 "$1 @r4, $SYMB"`
gen_IREGSYM $fname "$INSTR"
