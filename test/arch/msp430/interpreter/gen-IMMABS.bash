#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-IMMABS.bash <instr> <IMM> <ABS> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_IMMABS() {
    gen_header $1    
    echo "; @Init: \"data[$ABS] = $B, C = $C\"" >> $1
    echo "; @Result: \"data[$ABS] = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-IMMABS.bash $1 $IMM $ABS $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

IMM=$2
ABS=$3
B=$4
C=$5
Bp=$6
Cp=$7
Np=$8
Zp=$9
Vp=${10}

get_fname $1_IMMABS
INSTR=`./gen-instr.bash 4000 "$1 #$IMM, &$ABS"`
gen_IMMABS $fname "$INSTR"
