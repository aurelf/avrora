; @Target: avr-sim
; @Purpose: "Test the INC (increment register) instruction"
; @Result: "r16 = 0, flags.z = 1, flags.v = 0, flags.n = 0, flags.s = 0"

start:
    ser r17
    out sreg, r17

    ldi r16, 0b11111111
    inc r16

end:
    break
