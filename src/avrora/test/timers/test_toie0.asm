;
; $Id: test_toie0.asm,v 1.1 2004/04/02 23:50:38 titzer Exp $
;
;;; Test the Timer/Counter 0 overflow interrupt functionality.
;;; This test will stop the counter by setting the clock select
;;; value in TCCR0 to 0 (stop).

.def	zero =	r17				; r17 preset with 0x00
.def	ones =	r18				; r18 preset with 0xff
.def	disp =	r19				; r19 is output display

;;; Interrupt Jump Table
	    jmp    MAIN             ; reset

        jmp    IGNORE           ; interrupt #1
        jmp    IGNORE           ; interrupt #2
        jmp    IGNORE           ; interrupt #3
        jmp    IGNORE           ; interrupt #4
        jmp    IGNORE           ; interrupt #5
        jmp    IGNORE           ; interrupt #6
        jmp    IGNORE           ; interrupt #7
        jmp    IGNORE           ; interrupt #8
        jmp    IGNORE           ; interrupt #9
        jmp    IGNORE           ; interrupt #10
        jmp    IGNORE           ; interrupt #11
        jmp    IGNORE           ; interrupt #12
        jmp    IGNORE           ; interrupt #13
        jmp    IGNORE           ; interrupt #14
        jmp    IGNORE           ; interrupt #15
        jmp    IGNORE           ; interrupt #16

        jmp    TIMER_OVF        ; timer 0 overflow

        jmp    IGNORE           ; interrupt #18
        jmp    IGNORE           ; interrupt #19
        jmp    IGNORE           ; interrupt #20
        jmp    IGNORE           ; interrupt #21
        jmp    IGNORE           ; interrupt #22
        jmp    IGNORE           ; interrupt #23
        jmp    IGNORE           ; interrupt #24
        jmp    IGNORE           ; interrupt #25
        jmp    IGNORE           ; interrupt #26
        jmp    IGNORE           ; interrupt #27
        jmp    IGNORE           ; interrupt #28
        jmp    IGNORE           ; interrupt #29
        jmp    IGNORE           ; interrupt #30
        jmp    IGNORE           ; interrupt #31
        jmp    IGNORE           ; interrupt #32
        jmp    IGNORE           ; interrupt #33
        jmp    IGNORE           ; interrupt #34
        jmp    IGNORE           ; interrupt #35

IGNORE:
    reti


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
