; test case for call/return behavior.

ldi r17, 0b0001
call test
ldi r17, 0b0100
break

test:
    ldi r17, 0b010
    ret

