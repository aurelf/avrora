;
; $Id: test_stack.asm,v 1.2 2004/03/25 03:23:47 titzer Exp $
;
; Test pushing 4 bytes onto the stack. When done, expect to see:
	;; last 4 bytes of sram with 00, 01, 02, 03
	;; SPH, SPL with 0x02, 0x5b
	;; SREG -> 0x02
;.device         AT90S8515

.include        "8515def.inc"

        rjmp    MAIN            ; reset
        nop                     ; int0
        nop                     ; int1
        nop                     ; timer1 capt
        nop                     ; timer1 compa
        nop                     ; timer1 compb
        nop                     ; timer1 ovf
        nop                     ; timer0 ovf
        nop                     ; spi, stc
        nop                     ; uart, rx
        nop                     ; uart, udre
        nop                     ; uart, tx
        nop                     ; ana_comp

MAIN:
	;; init stack pointer to 0x025f (the last byte of int sram)
		ldi		r16, low(RAMEND); low byte of end of int sram
		out		SPL, r16
		ldi		r16, high(RAMEND); high byte of end of int sram
		out		SPH, r16

	;; push 4 numbers onto the stack (use r16 as a counter)
		ldi		r16, 0x03
PUSH_EM:
		push	r16				; push r16 onto the stack, should inc SP
		cpi		r16, 0x00		; compare r16 with $00, should set sreg
		breq	PUSHED_ALL		; branch to end if sreg['Z'] == 1
		dec		r16				; decrment r16
		rjmp	PUSH_EM			; repeat
PUSHED_ALL:	
		nop
