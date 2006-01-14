#!/bin/bash

if [ "$#" -lt 7 ]; then
    echo "Usage: gen-ABS.bash <instr> <ABS> <C> <Cp> <Np> <Zp> <Vp>"
    exit
fi

. ./msp430init.bash

gen_ABS() {
    gen_header $1    
    echo "; @Init: \"C = $C\"" >> $1
    echo "; @Result: \"C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-ABS.bash $1 $ABS $C $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

ABS=$2
C=$3
Cp=$4
Np=$5
Zp=$6
Vp=$7

get_fname $1_ABS
INSTR=`./gen-instr.bash 4000 "$1 &$ABS"`
gen_ABS $fname "$INSTR"
