; @Harness: simulator
; @Purpose: "Test the ADC (add carry flag) instruction"
; @Arch: msp430
; @Format: raw
; @Init: "r5 = 7"
; @Result: "r5 = 7"

; code
    0x4000: 05 53		"add #0, r5"
    0x4002: ff ff				; stop
