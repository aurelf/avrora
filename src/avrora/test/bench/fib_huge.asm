
	ldi r17, 0xff
	out 0x3d, r17
	ldi r16, 0

loop:		
	push r16
	call fib
	pop r16
	inc r16
	cpi r16, 100
	brne loop
	break
	
fib:

	push r28
	push r29
	in r28,0x3d
	in r29,0x3e
	sbiw r28,8
	in r0,0x3f
	cli
	out 0x3e,r29
	out 0x3f,r0
	out 0x3d,r28

	ldi r24,lo8(1)
	ldi r25,hi8(1)
	std Y+1,r24
	std Y+2,r25
	ldi r24,lo8(2)
	ldi r25,hi8(2)
	std Y+3,r24
	std Y+4,r25
	std Y+5,r1
	std Y+6,r1
L2:
	ldd r24,Y+5
	ldd r25,Y+6
	ldi r18,hi8(20000)
	cpi r24,lo8(20000)
	cpc r25,r18
	brlt L5
	rjmp L3
L5:
	ldd r18,Y+1
	ldd r19,Y+2
	ldd r24,Y+3
	ldd r25,Y+4
	add r24,r18
	adc r25,r19
	std Y+7,r24
	std Y+8,r25
	ldd r24,Y+3
	ldd r25,Y+4
	std Y+1,r24
	std Y+2,r25
	ldd r24,Y+7
	ldd r25,Y+8
	std Y+3,r24
	std Y+4,r25
	ldd r24,Y+5
	ldd r25,Y+6
	adiw r24,1
	std Y+5,r24
	std Y+6,r25
	rjmp L2
L3:

	adiw r28,8
	in r0, 0x3f
	cli
	out 0x3e,r29
	out 0x3f,r0
	out 0x3d,r28
	pop r29
	pop r28

	ret
	
