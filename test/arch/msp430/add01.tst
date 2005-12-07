; @Harness: simulator
; @Purpose: "Test the ADC (add carry flag) instruction"
; @Arch: msp430
; @Format: raw
; @Init: "r4 = 3, r5 = 7"
; @Result: "r5 = 10"

; code
    0x4000: 05 54 		"add r4, r5"    ; ADD R4, R5
    0x4002: ff ff				; stop
