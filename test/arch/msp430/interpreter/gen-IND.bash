#!/bin/bash

if [ "$#" -lt 7 ]; then
    echo "Usage: gen-IND.bash <instr> <OFF> <BASE> <A> <C> <A'> <C'> <N'> <Z'> <V'>"
    exit
fi

. ./msp430init.bash

gen_IND() {
    ADDR=`expr $BASE + $OFF`
    gen_header $1    
    echo "; @Init: \"data[$ADDR] = $A, r5 = $BASE, C = $C\"" >> $1
    echo "; @Result: \"data[$ADDR] = $Ap, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-IND.bash $1 $OFF $BASE $A $C $Ap $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

IND=$2
BASE=$3
A=$4
C=$5
Ap=$6
Cp=$7
Np=$8
Zp=$9
Vp=${10}

get_fname $1_IND
INSTR=`./gen-instr.bash 4000 "$1 $OFF(r5)"`
gen_IND $fname "$INSTR"
