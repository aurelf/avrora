	.file	"test.c"
	.arch avr2
__SREG__ = 0x3f
__SP_H__ = 0x3e
__SP_L__ = 0x3d
__tmp_reg__ = 0
__zero_reg__ = 1
	.global __do_copy_data
	.global __do_clear_bss
	.text
.global	main
	.type	main, @function
main:
/* prologue: frame size=66 */
	ldi r28,lo8(__stack - 66)
	ldi r29,hi8(__stack - 66)
	out __SP_H__,r29
	out __SP_L__,r28
/* prologue end (size=4) */
	std Y+1,__zero_reg__
	std Y+2,__zero_reg__
.L2:
	ldd r24,Y+1
	ldd r25,Y+2
	cpi r24,32
	cpc r25,__zero_reg__
	brlt .L5
	rjmp .L3
.L5:
	ldd r18,Y+1
	ldd r19,Y+2
	mov r25,r19
	mov r24,r18
	add r18,r24
	adc r19,r25
	mov r24,r28
	mov r25,r29
	adiw r24,1
	add r24,r18
	adc r25,r19
	mov r16,r24
	mov r17,r25
	subi r16,lo8(-(2))
	sbci r17,hi8(-(2))
	rcall method
	mov r31,r17
	mov r30,r16
	st Z,r24
	std Z+1,r25
	ldd r24,Y+1
	ldd r25,Y+2
	adiw r24,1
	std Y+1,r24
	std Y+2,r25
	rjmp .L2
.L3:
/* epilogue: frame size=66 */
	rjmp exit
/* epilogue end (size=1) */
/* function main size 39 (34) */
	.size	main, .-main
/* File "test.c": code   39 = 0x0027 (  34), prologues   4, epilogues   1 */
