;
; $Id: test_wdr.asm,v 1.1 2004/03/20 23:05:27 titzer Exp $
;
;;; Test the watchdog timer simulator. Should generate a reset about
;;; every 3.8 seconds.

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
		ldi		r16, lo8(RAMEND); low byte of end of int sram
		out		SPL, r16
		ldi		r16, hi8(RAMEND); high byte of end of int sram
		out		SPH, r16

	;; set up the watchdog timer
		ldi		r16, 0x0f		; set WDE, WDP[2,1,0] bits to enable watchdog
		out		WDTCR, r16

DONE:	nop
		rjmp DONE
