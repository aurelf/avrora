; @Target: avr-sim
; @Purpose: "Test the variants of the ST instruction"
; @Result: "$(memory) = 42, z = memory"

start:
    ldi r16, 42
    ldi r30, memory
    st z, r16

end:
    break

data:

.dseg

memory:
    .byte 2
    .byte 2
