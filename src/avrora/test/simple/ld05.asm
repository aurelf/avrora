; @Target: avr-sim
; @Purpose: "Test the LDS (load direct from SRAM) instruction"
; @Result: "r16 = 0"

start:
    ldi r16, 42
    lds r16, memory

end:
    break

data:

.dseg

memory:
    .byte 2
