; @Target: avr-sim
; @Purpose: "Test the SER (set register) instruction"
; @Result: "r16 = -1"

start:
    ser r16

end:
    break
