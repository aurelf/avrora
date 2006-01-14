#!/bin/bash

if [ "$#" -lt 8 ]; then
    echo "Usage: gen-ABSABS.bash <instr> <ABSA> <ABSB> <A> <B> <C> <B'> <C'> <N'> <Z'> <V'>"
    exit
fi


. ./msp430init.bash

gen_ABSABS() {
    gen_header $1    
    echo "; @Init: \"data[$ABSA] = $A, data[$ABSB] = $B, C = $C\"" >> $1
    echo "; @Result: \"data[$ABSB] = $Bp, C = $Cp, N = $Np, Z = $Zp, V = $Vp\" " >> $1
    echo "; ./gen-ABSABS.bash $1 $ABSA $ABSB $A $B $C $Bp $Cp $Np $Zp $Vp"
    echo >> $1
    echo "; code" >> $1
    echo $2 >> $1
}

ABSA=$2
ABSB=$3
A=$4
B=$5
C=$6
Bp=$7
Cp=$8
Np=$9
Zp=${10}
Vp=${11}

get_fname $1_ABSABS
INSTR=`./gen-instr.bash 4000 "$1 &$ABSA, &$ABSB"`
gen_ABSABS $fname "$INSTR"
