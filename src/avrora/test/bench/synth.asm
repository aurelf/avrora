forever:
inner:
	ldi r16, 45
	ldi r17, 12
	mov r0, r16
	mov r1, r17
	add r0, r1
	sub r0, r1
	mul r16, r17
	and r0, r1
	or r0, r1

	inc r5
	brne inner
	inc r4
	brne inner

	break
	