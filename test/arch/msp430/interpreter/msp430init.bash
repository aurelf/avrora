#!/bin/bash

export DINSTRS='add addc sub subc and xor bic bis cmp mov'
export SRINSTRS='call push rra rrc swpb sxt'
export SDINSTRS='sbc tst pop clr'
export NINSTRS='ret nop'

export REG_s='r0 r1 r2 r4 r15'
export IREG_s='@r4 @r5 @r7 @r15'
export ABSO_s='&0x0000 &0x0200 &0x21E &0x3000 &0x8000'
export SYMB_s='0x0000 0x0200 0x21E 0x3000 0x8000'
export IMM_s='#0 #1 #2 #3 #4 #7 #8 #-1 #0xff00 #0xf0f0 #0x0ff0 #0xaaaa #0xcccc'
export AIREG_s='@r0+ @r1+ @r4+ @r5+ @r7+ @r11+ @r15+'
export INDX_s='0(r1) 0(r4) 0(r5) 0(r7) 0(r15) 4(r0) 4(r1) 4(r4) 4(r5) 4(r7) 4(r15) -4(r0) -4(r1) -4(r4) -4(r5) -4(r7) -4(r15)'

export ALL_s="$REG_s $IREG_s $ABSO_s $SYMB_s $IMM_s $AIREG_s $INDX_s"

export REG_d=$REG_s
export ABSO_d=$ABSO_d
export SYMB_d=$SYMB_d
export IREG_d='r0 r1 r4 r5 r15'

export ALL_d="$REG_d $ABSO_d $SYMB_d $IREG_d"

gen_header() {
    echo '; @Harness: simulator' > $1
    echo '; @Arch: msp430' >> $1
    echo '; @Format: raw' >> $1
}

get_fname() {
number=1
export fname="${1}_${number}.tst"
while [ -f $fname ]; do
    number=`expr $number + 1`
    export fname="${1}_${number}.tst"
done
}
