; @Harness: simulator
; @Purpose: "Test the ADC (add carry flag) instruction"
; @Arch: msp430
; @Format: raw
; @Init: "r5 = 4"
; @Result: "r5 = 11"

; code
    0x4000: 35 50 07 00		"add #7, r5"
    0x4004: ff ff				; stop
