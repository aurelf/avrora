; @Target: avr-sim
; @Purpose: "Test the LDS (load direct from SRAM) instruction"
; @Initial: "[memory] = 42"
; @Result: "r16 = 42"

start:
    ldi r17, 42
    sts memory, r17
    lds r16, memory

end:
    break

data:

.dseg

memory:
    .byte 2
