#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-REGABS.bash <instr> <ABS> <A> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_REGABS() {
    gen_header $1    
    echo "; @Init: \"r4 = $A, data[$ABS] = $B, C = $C\"" >> $1
    echo "; @Result: \"data[$ABS] = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-REGABS.bash $1 $ABS $A $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

ABS=$2
A=$3
B=$4
C=$5
Bp=$6
Cp=$7
Np=$8
Zp=$9
Vp=${10}

get_fname $1_REGABS
INSTR=`./gen-instr.bash 4000 "$1 r4, &$ABS"`
gen_REGABS $fname "$INSTR"
