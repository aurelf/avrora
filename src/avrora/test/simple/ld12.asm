; @Target: avr-sim
; @Purpose: "Test the LDD (load from SRAM with displacement) instruction"
; @Initial: "[memory + 2] = 42"
; @Result: "r16 = 42, y = memory"

start:
    ldi r17, 42
    sts memory + 2, r17
    ldi r28, memory
    ldd r16, y + 2

end:
    break

data:

.dseg

memory:
    .byte 2
    .byte 2
