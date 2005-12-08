; @Harness: simulator
; @Arch: msp430
; @Format: raw
; @Init: "r4 = A, r5 = B, C = c, N = n, Z = z, V = v"
; @Result: "r5 = BP, C = cp, N = np, Z = zp, V = vp"

; code
    0x4000: 05 54                "add r4, r5"
    0x4002: ff ff                               ; stop
