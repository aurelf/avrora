; ******************************************************************
;    L C D    D I S P L A Y    D E M O
; ******************************************************************
;
;   Provides the entry point for a simple "Hello World" impelmentation
;   using my LCD routines.
;
;   Author:   Adam Swann <adam@velocity2.com>
;   Homepage: http://www.velocity2.com/~adam/
;
;   http://www.adamswann.com/projects/avr-lcd/
;
;   See LCD.asm for more information.
;
; ******************************************************************

.def 	Temp      = r16
.def 	Temp2 	  = r17

; Define generic port names (change the 'A' to whatever you're using)
.equ	LCD_PORT  = PORTC
.equ	LCD_DDR	  = DDRC
.equ	LCD_PIN	  = PINC

; Define the pin numbers
.equ	LCD_E		= 1
.equ	LCD_RW		= 2
.equ	LCD_RS		= 3

.def	DelayTime = r20
.equ	DelayCount1 = 128


		rjmp	RESET

RESET:	ldi     Temp, low(RAMEND)
    	out   	SPL, Temp
    	ldi   	Temp, high(RAMEND)
    	out   	SPH, temp		; Initialize Stackpointer

		ldi     Temp, 0x00
		out     LCD_PORT, Temp   	; Clear the outputs

		ldi     Temp, 0xFF
		out     LCD_DDR, Temp 		; Set the direction to output

		ldi     DelayTime, 255		; Set the default delay length
						; (see my delay.asm)

		rcall	LCD_Init

		rcall 	DELAY

		ldi     r30, low(strInit1*2)
		ldi     r31, high(strInit1*2)
		ldi     Temp2,	16
		rcall	LCD_PrintPM

		; *** Send the cursor to beginning of second line
		ldi     Temp, 0b11000000
		rcall 	LCD_SendCmd

		ldi     r30, low(strInit2*2)
		ldi     r31, high(strInit2*2)
		ldi     Temp2,	16
		rcall	LCD_PrintPM


		rcall 	DELAY



FOREVER:	rjmp 	FOREVER

strInit1:	.db	" Hello World!   "
strInit2:	.db	"It worked!!!    "

; ******************************************************************
;    L C D    D I S P L A Y    R O U T I N E S
; ******************************************************************
;
;   Interfaces the AVR '8515 microcontroller with LCDs controlled
;   by the Samsung KS0066U (and similiar) LCD driver.
;
;   Author:   Adam Swann <adam@velocity2.com>
;   Homepage: http://www.velocity2.com/~adam/
;
;   The code below is fairly straightforward and well-documented.
;   See my Web site or e-mail me if you need further instructions.
;
;   I used an 8515 at 4 MHz.  My LCD is Jameco Part #171715.
;
;   I wired the LCD display as follows (onto Port C)
;     AVR   LCD
;      0 --> no connection
;      1 --> Enable on LCD
;      2 --> R/W on LCD
;      3 --> RS on LCD
;      4 --> Data4
;      5 --> Data5
;      6 --> Data6
;      7 --> Data7
;
;   References: (URLs may be wrapped)
;    o KS0066U Datasheet <http://www.usa.samsungsemi.com/
;                          products/summary/charlcd/ks0066u.htm>
;
; ******************************************************************

; *** LCD_Init: Routine to initialize the LCD.
LCD_Init:	push	Temp

		ldi	DelayTime, 255

		; Put the LCD in 8-bit mode.  Even though we want the display to
		; operate in 4-bit mode, the only way to guarantee that our commands
		; are aligned properly is to initialize in 8-bit.  (The user might have
		; hit reset between nibbles of a dual 4-bit cycle.)
		ldi	Temp, 0b00110000
		out	LCD_PORT, Temp
		rcall	LCD_PulseE

		rcall	DELAY
		rcall	DELAY

		; Now it's safe to go into 4-bit mode.
		ldi	Temp, 0b00100000
		out	LCD_PORT, Temp
		rcall	LCD_PulseE

		rcall 	DELAY
		rcall	DELAY

		; *** Send the 'FUNCTION SET' command
		;		   +------ Data:  0 = 4-bit; 1 = 8-bit
		;		   |+----- Lines: 0 = 1; 1 = 2
		;		   ||+---- Font:  0 = 5x8; 1 = 5x11
		ldi	Temp, 0b00101100
		rcall 	LCD_SendCmd

		; *** Send the 'CURSOR/DISPLAY SHIFT' command
		;		    +----- S/C:  0 = cursor; 1 = display
		;		    |+---- R/L:  0 = left; 1 = right
		ldi	Temp, 0b00010100
		rcall 	LCD_SendCmd

		; *** Send the 'DISPLAY ON/OFF' command
		;		     +---- Display: 0 = off; 1 = on
		;		     |+--- Cursor: 0 = off; 1 = on
		;		     ||+-- Blink: 0 = off; 1 = on
		ldi	Temp, 0b00001100
		rcall 	LCD_SendCmd

		; *** Send the 'ENTRY MODE' command
		;		      +--- Direction: 0 = left; 1 = right
		;		      |+-- Shift Dislay: 0 = off; 1 = on
		ldi	Temp, 0b00000110
		rcall 	LCD_SendCmd

		rcall 	LCD_Clear

		pop 	Temp
		ret

; *** LCD_PrintMem: Prints from memory.
;       Put the starting memory location in Z (r31:r30)
;       Put the number of characters to print in Temp2
;	After execution, Z is at the character AFTER the last to be printed
;	                 and Temp2 is zero.
;       This function will not wrap if you the string is bigger than the LCD.

LCD_PrintMem:	push	Temp

   LCD_MemRead: ld	Temp, Z+
		rcall	LCD_SendChar
		dec	Temp2
		brne	LCD_MemRead

		pop	Temp
		ret

; *** LCD_PrintEE: Prints from EEPROM
;LCD_PrintEE:	push	Temp
;
 ;   LCD_EERead: sbic 	EECR, EEWE 	; Wait for EEWE to clear
;		rjmp 	LCD_EERead
;
;		out 	EEARH, r31 	;output address high byte, remove if no high byte exist
;		out 	EEARL, r30 	;output address low byte
;
;		sbi 	EECR, EERE 	;set EEPROM Read strobe
;		in 	Temp, EEDR 	;get data
;
;		rcall	LCD_SendChar
;
;		dec	Temp2
;		brne	LCD_EERead
;
;		pop	Temp
;		ret

; *** LCD_PrintPM: Prints from program memory
LCD_PrintPM:	push	r0
		push	Temp

    LCD_PMRead: lpm
    		mov	Temp, r0
		rcall	LCD_SendChar
		adiw	r30, 1
		dec	Temp2
		brne	LCD_PMRead

		pop	Temp
		pop	r0
		ret



; *** LCD_Clear: Clears the display and sends the cursor home.

LCD_Clear:	push 	Temp

		; *** Clear the display
		ldi	Temp, 0b00000001
		rcall 	LCD_SendCmd

		; *** Send the cursor home
		ldi	Temp, 0b00000010
		rcall 	LCD_SendCmd

		pop	Temp
		ret

; *** LCD_SendCmd: Routine to write a command to the instruction register.
;       The value to be written should be stored in Temp.
;       The value is sent 4 bits at a time.

LCD_SendCmd:	push 	Temp
		push 	Temp2

		rcall	LCD_WaitBusy

		mov 	Temp2, Temp		; Make a backup copy
		andi 	Temp2, 0b11110000	; Only use the upper nibble
		out	LCD_PORT, Temp2		; Send it
		rcall 	LCD_PulseE		; Pulse the enable

		swap	Temp			; Swap upper/lower nibble
		andi 	Temp, 0b11110000	; Only use the upper nibble
		out	LCD_PORT, Temp		; Send it
		rcall 	LCD_PulseE		; Pulse the enable

		pop 	Temp2
		pop 	Temp
		ret
; *** LCD_SendChar: Routine to write a character to the data register.
;       The value to be written should be stored in Temp.
;       The value is sent 4 bits at a time.

LCD_SendChar:	push 	Temp
		push 	Temp2

		mov 	Temp2, Temp		; Make a backup copy
		rcall	LCD_WaitBusy
		mov 	Temp, Temp2		; Make a backup copy

		andi 	Temp2, 0b11110000	; Only use the upper nibble
		ori	Temp2,  0b00001000
		out	LCD_PORT, Temp2		; Send it
		rcall 	LCD_PulseE		; Pulse the enable

		swap	Temp			; Swap upper/lower nibble
		andi 	Temp,  0b11110000	; Only use the upper nibble
		ori	Temp,  0b00001000
		out	LCD_PORT, Temp		; Send it
		rcall 	LCD_PulseE		; Pulse the enable

		pop 	Temp2
		pop 	Temp
		ret

; *** LCD_WaitBusy: Wait for the busy flag to go low.
;       Waits for the busy flag to go low.  Since we're in 4-bit mode,
;       the register has to be read twice (for a total of 8 bits).  The
;       second read is never used.
;       If you need more code space, this function could be replaced with
;       a simple delay.

LCD_WaitBusy:	push 	Temp

		ldi	Temp, 0b00001111	; Disable data bit outputs
		out	LCD_DDR, Temp

		ldi	Temp, 0x00		; Clear all outputs
		out	LCD_PORT, Temp

LCDWaitLoop:	ldi	Temp, 0b00000100	; Enable only read bit
		out	LCD_PORT, Temp
		sbi	LCD_PORT, LCD_E		; Raise the Enable signal.
		nop
		nop
		in	Temp, LCD_PIN		; Read the current values
		cbi	LCD_PORT, LCD_E		; Disable the enable signal.
		rcall 	LCD_PulseE		; Pulse the enable (the second nibble is discarded)
		sbrc	Temp, 7			; Check busy flag
		rjmp	LCDWaitLoop

		ldi	Temp, 0b11111111	; Enable all outputs
		out	LCD_DDR, Temp

		pop	Temp
		ret

LCD_PulseE:	sbi	LCD_PORT, LCD_E
		nop
		nop
		cbi	LCD_PORT, LCD_E
		ret

; *** Provide millisecond delay (DelayTicks specifies number of ms)
DELAY:	push DelayTime
	push r25

	ldi	r25, DelayCount1

DELAY1:	dec r25
	brne DELAY1
	dec DelayTime
	brne DELAY1

	pop r25
	pop DelayTime
	ret
