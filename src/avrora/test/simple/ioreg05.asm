; @Target: avr-sim
; @Purpose: "Test instructions for reading/writing to IO registers"
; @Result: "r17 = 106, r18 = 42, $(32) = 42, $(0) = 0"

start:
    ldi r17, 106
    out 0, r17
    cbi 0, 6
    in r18, 0

end:
    break
