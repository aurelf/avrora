; @Target: avr-simplify
; @Purpose: "Test generation of InstructionCannotBeInSegment error"
; @Result: "InstructionCannotBeInSegment @ 6:5"

.eseg
    break
