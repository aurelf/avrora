; Test 2: Learn to more about the board: Input from a port
; What to learn here:
; - to read input from a port
; - call subroutines and setup the stack
; - Binary math operations like AND, OR, ROL, etc.
; - Conditional branches (commands SBIx, BRxx)
; This is the replacement for the malfunctioning .DEVICE directive again:
.NOLIST
.INCLUDE "8515def.inc"
.LIST
; Define a universal register:
.DEF	mp = R16
; The jump-command on adress 0 again:
	rjmp	main
; The main program starts here
main:
	ldi	mp,LOW(RAMEND) ;Initiate Stackpointer
	out	SPL,mp
	ldi	mp,HIGH(RAMEND)
	out	SPH,mp
; These commands initiate the stack in the build in SRAM. Stack operations
; are always necessary when subroutines or interrupts are called.
; By calling the subroutine or interrupt handling routine the actual adress
; is written to the stack in order to later jump back to the code where the
; interrupt or call occurred. The stack is located at the upper end of the
; build in SRAM. The upper end of the SRAM is called RAMEND and is defined
; in the file "xxxxdef.inc" for the respective processor type, so we do not
; have to care about its real value.
; If a byte is disposed on the stack it is written to the SRAM location and the
; stack pointer at adress SPH:SPL (a 16 bit value) is decremented to the next
; lower stack location. Further disposing bytes brings this pointer nearer
; to the beginning of the SRAM. If a byte is taken from the stack then the
; stackpointer is incremented first and then the value is read.
; The last value put on the stack is read first when the stack is read, called
; a Last-In-First-Out structure.
; As the program counter and the adress structure requires 16 bits and
; all registers and the SRAM are 8 bits wide, every adress on stack requires
; two write/read operations to process the 16 bits. The SRAM adress is 16
; bits wide, so the port SPL holds the lower 8 bits and the port SPH holds
; the upper eight bits of the stack adress. Togeter we get the pointer SPH:SPL
; as a 16 bit pointer to the stack adress.
; The operations LOW and HIGH provide the opportunity to commincate to
; the assembler that the lower or upper byte of RAMEND is meant when
; we set up the stack pointer ports with the RAMEND value.
; Port D is connected to the eight switches on the board. In order to
; read these switches these pins have to have a zero in their data direction
; register
	ldi	mp,0x00 ; 8 zeros in universal register
	out	DDRD,mp ; to data direction register
; The switches connect the inputs of port D with GND. In order to provide
; a clear logical 1 when the key is open pull-up resistors have to be added.
; This is already done on the STK200 by external resistors, so we wouldn't
; need to use the internal resistors.Those internal resistors are build on
; the chip, so we can switch them on by software action. This is done by
; writing ones to the port register:
	ldi	mp,0xFF ; 8 Ones into the universal register
	out	PORTD,mp ; and to port D (these are the pull-ups now!)
; Port B connected to the LEDs is again output, so we need to set its direction
; register. On startup we want the LEDs to be all off, so we need to write ones
; to the port output registers, too.
	ldi	mp,0xFF ; 8 Ones to the universal register
	out	DDRB,mp ; and to the data direction register
	out	PORTB,mp ; and to the outputregisters.
; Clicking the keys 0 and 1 should switch on the corresponding LEDs,
; the keys 2 to 6 all the other LEDs. Clicking key 7 swiches all LEDs off.
; Within the main loop the switches are read and, if the different conditions
; are met, branched to the different subroutines.
loop:
; Reading switch 0 (very easy first)
; The first command (SBIS) reads port D (PIND) and tests if the bit 0 is
; one. If so, the next command is skipped. This is the case, if the switch
; is open and the input pin is pulled to one by the pull-up. If the switch
; is on, the pin reports zero and the condition for branching is not fulfilled.
; So the next command after SBIS must be a single byte command that
; branches to the routine that sets LED 0 on. This must be a subroutine,
; as it has to come back after execution, because we have to process the
; other switches as well.
; This subroutine is further down in the source code, the assembler cares
; about the displacement for the RCALL command. The RCALL pushes the
; current adress on stack, so the subroutine can come back to the next
; byte to be processed. The RCALL is used here, because it is a single
; byte command, while a normal CALL command, also implemented in the
; AVRs, is a 2-byte-command and would not fit.
	sbis	PIND,0 ; Jump if bit 0 in port D input is one
	rcall	Lampe0 ; Relative call to the subroutine named Lampe0
; After processing the subroutine and by jumping over that call command
; the next command is processed.
; Reading switch 1 (a little bit exotic)
; The ports are mirrored into the adress space of the SRAM. The SRAM
; adress is 32 bytes higher than the respective port adress (add hex 20).
; So we can use SRAM read commands to access the port. For our
; convenience we give that adress a new name:
.EQU	d_in_spiegel=PIND + $20
; With the register pair R27:R26 we define a pointer that points to that input
; port. With the LoaD-command we read the port input to a register as if it
; were a SRAM byte.
	ldi	R26,LOW(d_in_spiegel) ; define lower pointer in R26
	ldi	R27,HIGH(d_in_spiegel) ; define upper pointer in R27
	ld	mp,X ; Laad register mp from pointer adress (PIND)
; Isolate Pin1 (the switch 1) using mit AND-command and test for all zeros
	andi	mp,0b00000010 ; AND Bit 1
; Branch over all following commands if the result of the AND command is
; not zero (switch was off, input was one). The jump command BRNE (branch
; if not equal) branches to a lable up- or downwards and is not limited to
; a single byte command to follow. Use it for bigger jumps (here we don't).
	brne	weiter ; branch to lablel weiter, if not zero
	rcall	Lampe1 ; Relative call to subroutine Lampe1
; Switches 2 to 6
; Read the ports D into a register, mask the switches 0, 1 and 7 with the
; OR command and isolate the switches 2 to 6, if all are ones skip the next
; commands with the BREQ command to the label sw7, otherwise read the
; current status of the LEDs on port B (PINB), set all pins from 2 to 7 to zeros
; and send this to the port B output.
weiter:
	in	mp,PIND ; Read port D
	ori	mp,0b10000011 ; mask switches 0, 1 und 7
	cpi	mp,0b11111111 ; any one switch on?
	breq	sw7 ; branch to label sw7, if not (= $FF)
	in	mp,PINB ; read current LED-status
	andi	mp,0b00000011 ; switch on lamps 2 bis 7
	out	PORTB,mp ; to LED-port
sw7:
	in	mp,PIND ; read port with the switches
	rol	mp ; shift the seventh bit into the carry-flag
	brcs	endloop ; 7th bit is 1 (BRanch if carry is set)
	ldi	mp,0xFF ; All LEDs off
	out	PORTB,mp
endloop:
	rjmp	loop
; Subroutine Lampe0
; switches LED 0 on.
Lampe0:
	in	mp,PINB ; Read current status on port B
	andi	mp,0b11111110 ; Whatever the other LEDs might be, this is 0
	out	PORTB,mp ; write back the result to the LED port
	ret ; get the return adress from the stack and return where you came from.
; Subroutine Lampe1
; switches LED1 on (a little bit different than Lampe0)
Lampe1:
	in	mp,PINB ; read status of port B
	cbr	mp,0b00000010 ; set bit 2 to zero with the CBR command
	out	PORTB,mp ; write back the result to the LEDs
	ret ; return adress from stack and return
