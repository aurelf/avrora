; @Target: avr-sim
; @Purpose: "Test the skip instructions for correct conditions and target"
; @Result: "$(32) = 2, r18 = 0, r19 = 2"

start:
    ldi r17, 2
    out 0, r17
    sbic 0, 0   ; should not skip next instruction
    ldi r18, 1
    ldi r19, 2

end:
    break
