	.file	"test2.c"
	.arch avr2
__SREG__ = 0x3f
__SP_H__ = 0x3e
__SP_L__ = 0x3d
__tmp_reg__ = 0
__zero_reg__ = 1
	.global __do_copy_data
	.global __do_clear_bss
	.text
.global	method
	.type	method, @function
method:
/* prologue: frame size=0 */
	push r28
	push r29
	in r28,__SP_L__
	in r29,__SP_H__
/* prologue end (size=4) */
	ldi r24,lo8(0)
	ldi r25,hi8(0)
/* epilogue: frame size=0 */
	pop r29
	pop r28
	ret
/* epilogue end (size=3) */
/* function method size 9 (2) */
	.size	method, .-method
/* File "test2.c": code    9 = 0x0009 (   2), prologues   4, epilogues   3 */
