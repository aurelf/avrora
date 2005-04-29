; @Harness: simulator
; @Purpose: "Test instructions for reading/writing to IO registers"
; @Result: "r17 = 106, r18 = 42, $(59) = 42, $(0) = 0"

start:
    ldi r17, 106
    out 0x1b, r17
    cbi 0x1b, 6
    in r18, 0x1b

end:
    break
