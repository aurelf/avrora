; @Target: avr-sim
; @Purpose: "Test the LD (load from SRAM) instruction"
; @Initial: "[memory] = 42"
; @Result: "r16 = 42, y = memory"

start:
    ldi r17, 42
    sts memory, r17
    ldi r28, memory
    ld r16, y

end:
    break

data:

.dseg

memory:
    .byte 2
