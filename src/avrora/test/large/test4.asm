; Test 4: Even more about the board: timer in interupt mode
; New things to learn here:
; - Timer in interrupt mode
; - Interrupts, Interrupt-vektor
; - BCD-arithmetic
; Here is my substitute for the DEVICE-command
.NOLIST
.INCLUDE "8515def.inc"
.LIST
; Universal register definition
.DEF	mp = R16
; Counter for timer timeouts, MSB timer driven by software
.DEF	z1 = R0
; Working register for the Interrupt-Service-Routine
; Note that any registers used during an interrupt, including the
; status-register with all the flags, must either be
; reserved for that purpose or they have to reset to their initial
; value at the end of the service routine! Otherwise nice and
; nearly unpredictable effects will occur.
.DEF	ri = R1
; Register for counting the seconds as packed BCD
.DEF	seconds = R2
; Reset-vector to adress 0000
	rjmp	main
; This is the first time we really need this RJMP command, because here
; we have to put interrupt vectors to position 1, 2, 3 and so on.
; Interrupt-vector definitions not used here (all but the timer overflow vector)
; are dummied by the return-from-interrupt command RETI,
; RETI is a special return command for interrupt service routines as it
; preserves the interrupt-flags in the status-register. Be sure that the jump
; to the interrupt service routine tc0i is exactly at adress 0007, otherwise
; the interrupt fails. The following mechanism goes on: If the timer overflows
; (transition from 255 to 0) the program run is interrupted, the current adress
; in the program counter is pushed to the stack, the command at adress 0007
; is executed (usually a jump instruction). After finishing execution of the
; interrupt service routine the program counter value is restored from the
; stack and program execution maintains at that point.
; The interrupt-vector commands, 1 Byte each:
	reti ; Int0-Interrupt
	reti ; Int1-Interrupt
	reti ; TC1-Capture
	reti ; TC1-Compare A
	reti ; TC1-Compare B
	reti ; TC1-Overflow
	rjmp	tc0i ; Timer/Counter 0 Overflow, my jump-vector!
	reti ; Serial Transfer complete
	reti ; UART Rx complete
	reti ; UART Data register empty
	reti ; UART Tx complete
	reti ; Analog Comparator
; Interrupt-Service-Routine for the counter
tc0i:
	in	ri,SREG ; save the content of the flag register
	inc	z1 ; increment the software counter
	out	SREG,ri ; restore the initial value of the flag register
	reti ; Return from interrupt
; The main program starts here
main:
	ldi	mp,LOW(RAMEND) ;Initiate Stackpointer
	out	SPL,mp ; for the use by interrupts and subroutines
	ldi	mp,HIGH(RAMEND)
	out	SPH,mp
; Software-Counter-Register reset to zero
	ldi	mp,0 ; z1 cannot be set to a constant value, so we set mp
	mov	z1,mp ; to zero and copy that to R0=z1
	mov	seconds,mp ; and set the seconds to zero
; Prescaler of the counter/timer = 256, that is 4 MHz/256 = 15625 Hz = $3D09
	ldi	mp,0x04 ;Initiate Timer/Counter 0 Prescaler
	out	TCCR0,mp ; to Timer 0 Control Register
; Port B is LED-port
	ldi	mp,0xFF ; all bits are output
	out	DDRB,mp ; to data direction register
; enable interrupts for timer 0
	ldi	mp,$02 ; set Bit 1
	out	TIMSK,mp ; in the Timer Interupt Mask Register
; enable all interrupts generally
	sei ; enable all interrupts by setting the flag in the status-register
; The 8-bit counter overflows from time to time and the interrupt service
; routine increments a counter in a register. The main program loop reads this
; counter register and waits until it reaches hex 3D. Then the timer is read until
; he reaches 09 (one second = dez 15625 = hex 3D09 timer pulses). The timer
; and the register are set to zero and one second is incremented. The seconds
; are handled as packed BCD-digits (one digit = four bits, 1 Byte represents
; two digits). The seconds are reset to zero if 60 is reached. The seconds
; are displayed on the LEDs.
loop:
	ldi	mp,$3D ; compare value for register counter
loop1:
	cp	z1,mp ; compare with the register
	brlt	loop1 ; z1 < mp, wait
loop2:
	in	mp,TCNT0 ; read LSB in the hardware counter
	cpi	mp,$09 ; compare with the target value
	brlt	loop2 ; TCNT0 < 09, wait
	ldi	mp,0 ; set register zero and ...
	out	TCNT0,mp ; reset hardware-counter LSB
	mov	z1,mp ; and software-counter MSB
	rcall	IncSec ; call the subroutine to increment the seconds
	rcall	Display ; call subroutine to display the seconds
	rjmp	loop ; once again the same
; subroutine increment second counter
; in BCD-arithmetic! Lower nibble = Bit 0..3, upper nibble = 4..7
IncSec:
	sec ; Setze Carry-Flag for adding an additional one to the seconds
	ldi	mp,6 ; povoke overflow of the lower nibble by adding 6
	adc	seconds,mp ; add 6 + 1 (Carry)
	brhs	Chk60 ; if overflow of the lower nibble occurred go to 60 check
	sub	seconds,mp ; subtract the additional 6 as no overflow occurred
Chk60:
	ldi	mp,$60 ; 60 seconds already reached?
	cp	seconds,mp
	brlt	SecRet ; jump if less than 60
	ldi	mp,256-$60 ; Load mp to add sec to zero
	add	seconds,mp ; Add mp to reset sec to zero
SecRet:
	ret ; return to the main program loop
; subrountine for displaying the sonds on the LEDs
Display:
	mov	mp,seconds ; copy seconds to mp
	com	mp ; One-complement = XOR(FF) to invert the bits
	out	PORTB,mp ; send to the LED port
	ret ; return to main program loop
