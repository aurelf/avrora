;
; $Id: test_blink.asm,v 1.2 2004/03/25 03:23:47 titzer Exp $
;
; Uses an at90s8515 to make the LEDS flicker
;
;.device         AT90S8515

.include        "8515def.inc"

.def    delay =    r16
.def    delay_hi = r17
.def    cntr =     r18
.def    input =    r19

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

MAIN:   ldi     cntr,0xff
        ldi     delay,0xff      ; clear delay
        ldi     delay_hi,0xff   ; clear delay_hi

        out     DDRB,cntr       ; enable portb driver for output

START:  in      input,PIND
        cpi     input,0xff
        breq    START

LIGHTS: out     PORTB,cntr      ; output the contents of cntr to portb

        dec     delay           ; decrement delay
        cpi     delay,0         ; compare delay with 0
        brne    LIGHTS          ; branch to LIGHTS if delay <> 0

        dec     delay_hi        ; decrement delay_hi
        cpi     delay_hi,0      ; compare delay_hi with 0
        brne    LIGHTS          ; branch to LIGHTS if delay_hi <> 0

        dec     cntr            ; decrement cntr
LOOP:   rjmp    LIGHTS          ; go into an infinite loop

