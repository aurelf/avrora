; @Target: avr-sim
; @Purpose: "Test the variants of the ST instruction"
; @Result: "$(memory) = 42, y = memory"

start:
    ldi r16, 42
    ldi r28, memory
    st y, r16

end:
    break

data:

.dseg

memory:
    .byte 2
    .byte 2
