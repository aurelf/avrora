;
; $Id: test_port.asm,v 1.1 2004/03/20 23:05:26 titzer Exp $
;
;;; Test basic functionality of reading and writing to
;;; the io ports.
;
;.device         AT90S8515

.include        "8515def.inc"

.equ	zero,	17				; r17 preset with 0x00
.equ	ones,	18				; r18 preset with 0xff

;;; Interrupt Jump Table
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
	;; preload zero and ones
		ldi		zero,  0x00
		ldi		ones,  0xff
	
	;; init portb for output
		out		DDRB,  ones
		out		PORTB, zero

	;; init portd for input
		out		DDRD,  zero

LOOP1:
		in		r16, PIND		; read from PORTD to R17
		out		PORTB, R16		; write R16 to PORTB
  		rjmp	LOOP1
