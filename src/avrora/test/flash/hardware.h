#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/signal.h>
#include "pgmspace1_16.h"


#define HAS_CRITICAL_SECTION       register uint8_t _prev_
#define ENTER_CRITICAL_SECTION()   _prev_ = SREG & 0x80; cli()
#define LEAVE_CRITICAL_SECTION()   if(_prev_) sei()
#define ENABLE_INTERRUPT()         sei()
#define DISABLE_INTERRUPT()        cli()

#define led_red_on()        cbi(PORTA, 2)
#define led_green_on()      cbi(PORTA, 1)
#define led_yellow_on()     cbi(PORTA, 0)
#define led_red_off()       sbi(PORTA, 2)
#define led_green_off()     sbi(PORTA, 1)
#define led_yellow_off()    sbi(PORTA, 0)
#define led_red_toggle()    PORTA ^= (1 << 2)
#define led_green_toggle()  PORTA ^= (1 << 1)
#define led_yellow_toggle() PORTA ^= 1
#define led_init()     {  DDRA |= 0x7; led_green_off(); led_red_off(); led_yellow_off(); }

