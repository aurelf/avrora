#!/bin/bash

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
