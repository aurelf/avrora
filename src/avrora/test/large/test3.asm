; Test 3: Learning even more about the board: timer in polling mode
; New things to learn:
; - Timer in polling mode
; - MOV-command
; Again my special DEVICE-command
.NOLIST
.INCLUDE "8515def.inc"
.LIST
; universal register definition
.DEF	mp = R16
; Counter for the number of timeouts
.DEF	z1 = R0
; Reset-Vector on adress 0000
	rjmp	main
; Main program starts here
main:
	ldi	mp,LOW(RAMEND) ;Initiate Stackpointer (Subroutines!)
	out	SPL,mp
	ldi	mp,HIGH(RAMEND)
	out	SPH,mp
; Software-Counter-Register to zero
	ldi	mp,0 ; z1 cannot directly be set to zero as it is below R16
	mov	z1,mp ; So set mp to zero and copy to R0
; Prescaler of the timer = 1024, 4 MHz/1024 = 3906,25 Hz
; that equals a timer tick every 256 µs.
	ldi	mp,0x05 ;Initiate Timer/Counter 0 Prescaler
	out	TCCR0,mp ; to Timer 0 Control Register
; Port B is LED-port
	ldi	mp,0xFF ; all bits as output
	out	DDRB,mp ; to data direction register
; Main program loop reads the counter until he reaches zero, increments
; software counter and displays result on the LEDs.
; 256 timer ticks need 65.536 ms, resulting in a frequency of 15.25878906 Hz.
loop:
	in	mp,TCNT0 ; read the 8-bit timer 0
	cpi	mp,0 ; test for zero
	brne	loop ; if not zero branch to loop start
	rcall	IncZ1 ; call subroutine software-timer-increment
	rcall	Display ; call subroutine display-software-counter
warte:
	in	mp,TCNT0 ; again read timer 0
	cpi	mp,0 ; test for zero
	breq	warte ; wait until not zero any more
	rjmp	loop ; start next round
IncZ1:
	inc	z1 ; increment software counter
	ret ; return to the main program
Display:
	mov	mp,z1 ; copy software counter to universal register
	com	mp ; One-complement = XOR(FF) due to reverse LEDs
	out	PORTB,mp ; send to LED port
	ret ; return to the main program
