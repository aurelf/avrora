; @Harness: simulator
; @Purpose: "Test instructions for reading/writing to IO registers"
; @Result: "r17 = 42, r18 = 106, $(59) = 106, $(0) = 0"

start:
    ldi r17, 42
    out 0x1b, r17
    sbi 0x1b, 6
    in r18, 0x1b

end:
    break
