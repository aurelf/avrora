;
; $Id: test_eeprom.asm,v 1.2 2004/03/23 07:47:32 titzer Exp $
;
;;; Test writing to and reading from the eeprom memory space.

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

	;; write a byte to eeprom
		ldi		r16, 0x00		; set up low addr
		ldi		r17, 0x00		; set up high addr
		ldi		r18, 0x55		; set up the data
		rcall   SUB_EE_WR		; call subroutine to write data

		ldi		r18, 0x00		; clear r18, so we'll know if we read back correctly

	;; read a byte from eeprom
		rcall	SUB_EE_RD
		mov		r0, r18			; move result from r18 to r0

	;; jmp to done
		rjmp	DONE

;;; Subroutine for writing a byte to eeprom
;;;   r16 -> EEARL
;;;   r17 -> EEARH
;;;   r18 -> data to be written
SUB_EE_WR:
		sbic	EECR, EEWE		; poll the EEWE bit to see if we can write
		rjmp	SUB_EE_WR		; if EEWE is cleared, skip the rjmp

		out		EEARL, r16		; write low addr to EEARL
		out		EEARH, r17		; write high addr to EEARH
		out		EEDR,  r18		; write data to EEDR

		sbi		EECR, EEMWE		; write 1 to EEMWE
		sbi		EECR, EEWE		; write 1 to EEWE

WR_POLL:
		sbic	EECR, EEWE		; poll the EEWE bit until write has completed
		rjmp	WR_POLL			; rjmp is skipped once EEWE is cleared

		ret						; return from subroutine

;;; Subroutine for reading a byte from eeprom
;;;   r16 -> EEARL
;;;   r17 -> EEARH
;;;   result is placed in r18
SUB_EE_RD:
		sbic	EECR, EEWE		; poll the EEWE bit to see if we can write
		rjmp	SUB_EE_WR		; if EEWE is cleared, skip the rjmp

		out		EEARL, r16		; write low addr to EEARL
		out		EEARH, r17		; write high addr to EEARH

		sbi		EECR, EERE		; strobe the eeprom
		in		r18, EEDR		; move data read from eedr to r18

		ret

DONE:	nop
