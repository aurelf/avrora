; @Target: avr-sim
; @Purpose: "Test the LD (load from SRAM) instruction"
; @Initial: "[memory] = 42"
; @Result: "r16 = 42, x = memory"

start:
    ldi r17, 42
    sts memory, r17
    ldi r26, memory
    ld r16, x

end:
    break

data:

.dseg

memory:
    .byte 2
