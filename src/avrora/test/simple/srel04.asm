; @Target: avr-simplify
; @Purpose: "Range test for relative jumps"
; @Result: "PASS"

TOOFAR:
.byte 126
	brie TOOFAR

