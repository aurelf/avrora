; @Target: avr-simplify
; @Purpose: "Test generation of UnknownRegister error"
; @Result: "UnknownRegister @ 6:9"

start:
    subi foo, 0

end:
    break
