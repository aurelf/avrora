; @Target: avr-sim
; @Purpose: "Test the skip instructions for correct conditions and target"
; @Result: "$(32) = 1, r18 = 0, r19 = 2"

start:
    ldi r17, 1
    out 0, r17
    sbis 0, 0   ; should not skip next instruction
    jmp end
    ldi r19, 2

end:
    break
