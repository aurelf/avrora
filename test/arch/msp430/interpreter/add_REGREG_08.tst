; @Harness: simulator
; @Arch: msp430
; @Format: raw
; @Init: "r4 = 1, r5 = 2"
; @Result: "r5 = 3, C = 0, N = 0, Z = 0, V = 0"

; code
    0x4000: 05 54                "add r4, r5"
    0x4002: ff ff                               ; stop
