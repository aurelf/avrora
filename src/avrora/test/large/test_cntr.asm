;
; $Id: test_cntr.asm,v 1.1 2004/03/20 23:05:26 titzer Exp $
;
; Use simple opcodes to test the instruction decoder.
;
;;; .device         AT90S8515

.include        "8515def.inc"

;;; .cseg
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
		ldi		r16, 0x5f		; low byte of end of int sram
		out		SPL, r16
		ldi		r16, 0x02		; high byte of end of int sram
		out		SPH, r16

		ldi		r16, 0xff		; initialize outer loop counter

LOOP1:
		cpi		r16, 0x00
		breq	DONE
		dec		r16
		rcall	INNER
		rjmp	LOOP1

INNER:
		ldi		r17, 0xff		; initialize inner loop counter

LOOP2:
		cpi		r17, 0x00
		breq	REPEAT
		dec		r17
		rjmp	LOOP2
REPEAT:
		ret
	
DONE:	
		nop
