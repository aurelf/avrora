#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-IREGREG.bash <instr> <ADDR> <A> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_IREGREG() {
    gen_header $1    
    echo "; @Init: \"data[$ADDR] = $A, r4 = $ADDR, r5 = $B, C = $C\"" >> $1
    echo "; @Result: \"r5 = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-IREGREG.bash $1 $ADDR $A $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

ADDR=$2
A=$3
B=$4
C=$5
Bp=$6
Cp=$7
Np=$8
Zp=$9
Vp=${10}

get_fname $1_IREGREG
INSTR=`./gen-instr.bash 4000 "$1 @r4, r5"`
gen_IREGREG $fname "$INSTR"
