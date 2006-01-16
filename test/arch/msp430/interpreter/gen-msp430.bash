#!/bin/bash

. ./msp430init.bash

# add                     A     B C =>   A     B C N Z V
#--------------------------------------------------------
./gen-REGREG.bash add     4     4 0      4     8 0 0 0 0
./gen-REGREG.bash add 32000   768 0  32000 32768 0 1 0 1
./gen-REGREG.bash add 65000   536 0  65000     0 1 0 1 0

# addc                     A     B C =>   A     B C N Z V
#--------------------------------------------------------
./gen-REGREG.bash addc     4     4 1      4     9 0 0 0 0
./gen-REGREG.bash addc 32000   767 1  32000 32768 0 1 0 1
./gen-REGREG.bash addc 65000   535 1  65000     0 1 0 1 0

# and                      A     B C =>   A     B C N Z V
#--------------------------------------------------------
./gen-REGREG.bash and      4     4 0      4     4 0 0 0 0
./gen-REGREG.bash and   0xf0   0xf 0   0xff     0 0 0 1 0
./gen-REGREG.bash and  32768 65535 0  32768 32768 0 1 0 0

# bic

# bis

# bit

# cmp                      A     B C =>   A     B C N Z V
#--------------------------------------------------------
./gen-REGREG.bash cmp      4     4 0      4     4 0 0 1 0

# dadd

# mov                      A     B C =>   A     B C N Z V
#--------------------------------------------------------
./gen-REGREG.bash mov      4     7 0      4     4 0 0 0 0
./gen-REGREG.bash mov      1     7 0      1     1 0 0 0 0
./gen-REGREG.bash mov    256     7 0    256   256 0 0 0 0
./gen-REGREG.bash mov.b  256     7 0    256     0 0 0 0 0

# rra

# rrc

# sub

# subc

# swpb

# sxt                A C      A C N Z V
#-------------------------------------------
./gen-REG.bash sxt   4 0      4 0 0 0 0
./gen-REG.bash sxt 128 0  65408 0 0 0 0

# xor

# IMM addressing mode
#-----------------------------------------------------
./gen-IMMREG.bash add     1 4   0     5 0 0 0 0
./gen-IMMREG.bash add     2 4   0     6 0 0 0 0
./gen-IMMREG.bash add     4 4   0     8 0 0 0 0
./gen-IMMREG.bash add     8 4   0    12 0 0 0 0
./gen-IMMREG.bash add    -1 4   0     3 0 0 0 0
./gen-IMMREG.bash add   330 4   0   334 0 0 0 0
./gen-IMMREG.bash mov     1 0   0     1 0 0 0 0
./gen-IMMREG.bash mov     2 0   0     2 0 0 0 0
./gen-IMMREG.bash mov     4 0   0     4 0 0 0 0
./gen-IMMREG.bash mov     8 0   0     8 0 0 0 0
./gen-IMMREG.bash mov    -1 0   0 65536 0 0 0 0
./gen-IMMREG.bash mov   290 0   0   290 0 0 0 0
./gen-IMMREG.bash mov.b 290 0   0    34 0 0 0 0


# ABS addressing mode
#-----------------------------------------------------
./gen-ABSREG.bash add 0x400 4 4 0 8 0 0 0 0
./gen-ABSREG.bash mov 0x400 2 0 0 2 0 0 0 0

./gen-ABSABS.bash add 0x400 0x500 4 4 0 8 0 0 0 0
./gen-ABSABS.bash mov 0x400 0x500 2 0 0 2 0 0 0 0

./gen-IMMABS.bash add  1 0x400 4 0  5 0 0 0 0
./gen-IMMABS.bash add  8 0x400 4 0 12 0 0 0 0
./gen-IMMABS.bash add 30 0x400 4 0 34 0 0 0 0
./gen-IMMABS.bash mov  1 0x400 4 0  1 0 0 0 0
./gen-IMMABS.bash mov 30 0x400 4 0 30 0 0 0 0

./gen-REGABS.bash add  1 0x400 4 0  5 0 0 0 0
./gen-REGABS.bash add  8 0x400 4 0 12 0 0 0 0
./gen-REGABS.bash add 30 0x400 4 0 34 0 0 0 0
./gen-REGABS.bash mov 30 0x400 4 0 30 0 0 0 0


# SYM (relative) addressing mode
#--------------------------------------------------
./gen-SYMSYM.bash add 0x400 0x500 4 4 0 8 0 0 0 0
./gen-SYMSYM.bash mov 0x400 0x500 2 0 0 2 0 0 0 0

./gen-SYMABS.bash add 0x400 0x500 4 4 0 8 0 0 0 0
./gen-SYMABS.bash mov 0x400 0x500 2 0 0 2 0 0 0 0

./gen-SYMREG.bash add 0x400 4 4 0 8 0 0 0 0
./gen-SYMREG.bash mov 0x400 2 0 0 2 0 0 0 0

./gen-REGSYM.bash add  1 0x400 4 0  5 0 0 0 0
./gen-REGSYM.bash add  8 0x400 4 0 12 0 0 0 0
./gen-REGSYM.bash add 30 0x400 4 0 34 0 0 0 0
./gen-REGSYM.bash mov 30 0x400 4 0 30 0 0 0 0

# IND (indexed) addressing mode
#--------------------------------------------------
./gen-INDIND.bash add 4 4 0 8 0 0 0 0
./gen-INDIND.bash mov 2 0 0 2 0 0 0 0

./gen-INDABS.bash add 0x500 4 4 0 8 0 0 0 0
./gen-INDABS.bash mov 0x500 2 0 0 2 0 0 0 0

./gen-INDREG.bash add 4 4 0 8 0 0 0 0
./gen-INDREG.bash mov 2 0 0 2 0 0 0 0

./gen-REGIND.bash add  1 4 0  5 0 0 0 0
./gen-REGIND.bash add  8 4 0 12 0 0 0 0
./gen-REGIND.bash add 30 4 0 34 0 0 0 0
./gen-REGIND.bash mov 30 4 0 30 0 0 0 0

# IREG (indirect register) addressing mode

# AUTO_W (auto-increment) addressing mode

# AUTO_B (auto-increment) addressing mode

# calls

# ret, reti

# branches (jc, jeq, jge, jl, jn, jnc, jne)

# jmp

# push, pop

