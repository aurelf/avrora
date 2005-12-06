; @Harness: simulator
; @Format: atmel
; @Purpose: "Test the LDI (load immediate) instruction"
; @Result: "r16 = 42"

start:
    ldi r16, 42

end:
    break
