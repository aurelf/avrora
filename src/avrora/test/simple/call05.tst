; @Harness: simulator
; @Purpose: "Test the branch instructions for correct conditions and target"
; @Result: "target = 12, r17 = 0, r18 = 3, sp = 253, $(sp+2) = 9, $(sp+1) = 0"

setstack:
    ldi r21, 255
    out spl, r21

genesis:
    jmp start
    break

before:
    ldi r17, 2
target:
    ldi r18, 3
    break

start:
    rcall target

end:
    break
