#include "hardware.h"
#include "flash.h"

/**
 * Write to CC1K radio register
 * The primary use of this function is to turn off radio.
 */
void cc1k_write(uint8_t addr, uint8_t data){
  char cnt = 0;
                                                                
  // address cycle starts here
  addr <<= 1;
  cbi(PORTD, 4);
  for (cnt=0;cnt<7;cnt++)  // send addr PDATA msb first
    {
      if (addr&0x80)
	sbi(PORTD,7);
      else
	cbi(PORTD,7);
      cbi(PORTD,6);
      sbi(PORTD,6);
      addr <<= 1;
    }
  sbi(PORTD,7);
  cbi(PORTD,6);
  sbi(PORTD,6);
  sbi(PORTD, 4);
  // data cycle starts here
  for (cnt=0;cnt<8;cnt++)  // send data PDATA msb first
    {
      if (data&0x80)
	sbi(PORTD,7);
      else
	cbi(PORTD,7);
      cbi(PORTD,6);
      sbi(PORTD,6);
      data <<= 1;
    }
  sbi(PORTD, 4);
  sbi(PORTD, 7);
  sbi(PORTD, 6);
}

/**
 * Timer initialization
 */
void timer_init(uint8_t interval, uint8_t scale)
{
    HAS_CRITICAL_SECTION;
    ENTER_CRITICAL_SECTION();
                                                                               
    scale &= 0x7;
    scale |= 0x8;
                                                                               
    cbi(TIMSK, TOIE0);
    cbi(TIMSK, OCIE0);
    //!< Disable TC0 interrupt
                                                                               
    /**
     *  set Timer/Counter0 to be asynchronous
     *  from the CPU clock with a second external
     *  clock(32,768kHz)driving it
     */
    sbi(ASSR, AS0);
    outp(scale, TCCR0);    //!< prescale the timer to be clock/128 to make it
                                                                               
    outp(0, TCNT0);
    outp(interval, OCR0);
    sbi(TIMSK, OCIE0);
    LEAVE_CRITICAL_SECTION();
}

//! buffer to be used for flashing
uint8_t somedata[256];
static uint8_t cnt = 0;

SIGNAL(SIG_OUTPUT_COMPARE0)
{
	cnt++;
	if(cnt % 2) {
		sbi(PORTC, 1);
		//! flash data into program flash
		FlashPage(50, somedata, 256);
		cbi(PORTC, 1);	
	} else {
		uint16_t i;
		//! make up data...
		for(i = 0; i < 256; i++) {
			somedata[i] = (i + cnt) % 256;
		}
		
	}
}


int main()
{
	uint16_t i;
	register uint8_t tmp = MCUCR;
	//! set interrupt vectors to bootloader
	MCUCR = tmp | (1 << IVCE);
	MCUCR = tmp | (1 << IVSEL);

	DISABLE_INTERRUPT();
	//! setup led...
	led_init();
	

	//! turn off Radio
	sbi(DDRD, 4);
	sbi(DDRD, 6);
	sbi(DDRD, 7);
	cc1k_write(0x0B, 0);
	cc1k_write(0, (1 << 5) | (1<<4) | (1<<3) | (1<<2) | (1<<1) | (1<<0));
	//! setup triggler pins
	sbi(DDRC, 1);
	//led_red_on();
	ENABLE_INTERRUPT();
	//! initialize data
	//FlashBufferLoad(100, 0);
	for(i = 0; i < 256; i++) {
		somedata[i] = i;
	}
	timer_init(128, 3);
	for(;;){ }
}
