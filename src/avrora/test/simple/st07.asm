; @Target: avr-sim
; @Purpose: "Test the STD (store with displacement) instruction"
; @Result: "$(memory + 2) = 42, z = memory"

start:
    ldi r16, 42
    ldi r30, memory
    std z + 2, r16

end:
    break

data:

.dseg

memory:
    .byte 2
    .byte 2
