;
; $Id: test_toie0.asm,v 1.2 2004/04/02 23:06:06 titzer Exp $
;
;;; Test the Timer/Counter 0 overflow interrupt functionality.
;;; This test will stop the counter by setting the clock select
;;; value in TCCR0 to 0 (stop).

;.include        "8515def.inc"

.def	zero =	r17				; r17 preset with 0x00
.def	ones =	r18				; r18 preset with 0xff
.def	disp =	r19				; r19 is output display

;;; Interrupt Jump Table
	    jmp    MAIN             ; reset

        nop                     ; int 1
        reti
        nop                     ; int 2
        reti
        nop                     ; int 3
        reti
        nop                     ; int 4
        reti
        nop                     ; int 5
        reti
        nop                     ; int 6
        reti
        nop                     ; int 7
        reti
        nop                     ; int 8
        reti
        nop                     ; int 9
        reti
        nop                     ; int 10
        reti
        nop                     ; int 11
        reti
        nop                     ; int 12
        reti
        nop                     ; int 13
        reti
        nop                     ; int 14
        reti
        nop                     ; int 15
        reti
        nop                     ; int 16
        reti

        nop                     ; spi, stc
        rjmp	TIMER_OVF       ; timer0 ovf

        nop                     ; uart, rx
        nop                     ; uart, udre
        nop                     ; uart, tx
        nop                     ; ana_comp

TIMER_OVF:
	;; Toggle the leds on Port B so we know that the interrupt
	;; occured and was handled.
		out		PORTB, disp		; set the leds
		dec		disp			; decrement the display counter

	;; if disp has counted down to 0, stop the counter via TCCR,
	;; but leave the interrupt enabled
		brne	OVF_RETURN		; skip to reti if disp is not zero
		ldi		r20, 0x00
		out		TCCR0, r20		; set clock select to 0x00 (stop)

OVF_RETURN:
		reti					; return from interrupt
	
MAIN:
	;; init stack pointer to 0x025f (the last byte of int sram)
		ldi		r16, low(RAMEND); low byte of end of int sram
		out		SPL, r16
		ldi		r16, high(RAMEND); high byte of end of int sram
		out		SPH, r16

	;; preload zero and ones
		ldi		zero,  0x00
		ldi		ones,  0xff
		ldi		disp,  0xff
	
	;; init portb for output
		out		DDRB,  ones
		out		PORTB, disp

	;; Set the clock select for Timer0 to CK/256
		ldi		r20,   0x04		; CS[2,1,0] = [1,0,0]
		out		TCCR0, r20		; write to Timer/Counter Control Register 0

	;; Enable the Timer0 Overflow interrupt
		ldi		r20, 1
		out		TIMSK, r20		; set toie0 bit of timsk I/O register
		sei						; set global interrupt enable (in sreg)

IDLE_LOOP:
	;; Loop until r20 is zero
		tst		r20
		brne	IDLE_LOOP

DONE:
		rjmp DONE
