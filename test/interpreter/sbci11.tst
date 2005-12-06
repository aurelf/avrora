; @Harness: simulator
; @Format: atmel
; @Purpose: "Test the SBCI (subtract immediate from register with carry) instruction"
; @Result: "flags.h=0, flags.s=0, flags.v=0, flags.n=0, flags.z=1, flags.c=0, r16 = 0"

start:
    sez
    ldi r16, 0b00000000
    sbci r16, 0b00000000

end:
    break
