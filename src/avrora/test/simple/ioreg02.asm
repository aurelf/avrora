; @Target: avr-sim
; @Purpose: "Test instructions for reading/writing to IO registers"
; @Result: "r17 = 42, r18 = 42, $(32) = 42, $(0) = 0"

start:
    ldi r17, 42
    out 0, r17
    in r18, 0

end:
    break
