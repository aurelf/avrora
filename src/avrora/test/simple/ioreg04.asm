; @Target: avr-sim
; @Purpose: "Test instructions for reading/writing to IO registers"
; @Result: "r17 = 42, r18 = 106, $(32) = 106, $(0) = 0"

start:
    ldi r17, 42
    out 0, r17
    sbi 0, 6
    in r18, 0

end:
    break
