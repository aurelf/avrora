; @Harness: simulator
; @Purpose: "Test the skip instructions for correct conditions and target"
; @Result: "$(59) = 2, r18 = 0, r19 = 2"

start:
    ldi r17, 2
    out 0x1b, r17  ; write to PORTA
    nop
    sbic 0x1b, 0   ; should not skip next instruction
    jmp end
    ldi r19, 2

end:
    break
