/* -*- Mode: C; tab-width:4 -*- */
/* ex: set ts=4: */
/*
 * Copyright (C) 2002 by egnite Software GmbH. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgement:
 *
 *    This product includes software developed by egnite Software GmbH
 *    and its contributors.
 *
 * THIS SOFTWARE IS PROVIDED BY EGNITE SOFTWARE GMBH AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL EGNITE
 * SOFTWARE GMBH OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * For additional information see http://www.ethernut.de/
 */

#include <avr/io.h>
#include <inttypes.h>
#include "hardware.h"
#include <avr/delay.h>

#include "flash.h"

static uint8_t flash_buf[256];

/*!
 * \brief Erase and program a page in the flash ROM.
 *
 * \param page The page number to program, 0..479.
 * \param data Pointer to the new page contents.
 * \param len  Number of bytes to program. If this is
 *             less than 256, then the remaining bytes
 *             will be filled with 0xFF.
 * takes 9.6ms, 168 mV, 
 */
void FlashPage(uint16_t page, void *data, uint16_t len)
{
	HAS_CRITICAL_SECTION;
	uint16_t i;
	uint16_t *wp = data;

	ENTER_CRITICAL_SECTION();
	if(len > 256)
		len = 256;

	if(page >= 256) {
		if(page >= 480){
			LEAVE_CRITICAL_SECTION();
			return;
		}
		outp(1, RAMPZ);
	}
	else{
		outp(0, RAMPZ);
	}
	page <<= 8;

	SpmCommand(page, (1 << PGERS) | (1 << SPMEN));
	SpmCommand(0, (1 << RWWSRE) | (1 << SPMEN));

	for(i = 0; i < len; i += 2, wp++)
		SpmBufferFill(i, *wp);
	for(; i < 256; i += 2)
		SpmBufferFill(i, 0xFFFF);

	SpmCommand(page, (1 << PGWRT) | (1 << SPMEN)); 
	SpmCommand(0, (1 << RWWSRE) | (1 << SPMEN));               
	LEAVE_CRITICAL_SECTION();
}

/**
 * @brief Copy data into SPM buffer
 * @param start starting byte address
 * @param data  data pointer
 * @param len   data length
 * Note that this routine assumes length is even
 * takes 350 us, at 428.1 mV
 */
void FlashCopy(uint16_t start, void *data, uint16_t len) 
{
	uint16_t i;
	uint8_t *wp = data;

	for(i = start; i < start + len; i++, wp++){
		flash_buf[i] = *wp;
	}
}

void FlashBufferErase()
{
	uint16_t *wp = (uint16_t*)flash_buf;
	uint8_t i;
	for(i = 0; i < 128; i++) {
		wp[i] = 0xffff;
	}
}

/**
 * @brief load flash buffer from flash
 * @param page the page number in the flash
 * @param section the seciton number in the flash
 * 
 * We devide the page into 4 section.  Each section is 64 bytes long
 * This takes 173 us at 421.9 mV
 */
void FlashBufferLoad(uint16_t page, uint16_t section)
{
	HAS_CRITICAL_SECTION;
	uint16_t j;
	ENTER_CRITICAL_SECTION();
	for(j = (section * 32); j < ((section+1) * 32); j++){
		flash_buf[j] = pgm_read_word_far(((uint32_t)page * 256) + 2*j);
	}
	LEAVE_CRITICAL_SECTION();
}

/**
 * @brief Flash SPM buffer into flash
 * @param The page number to program, 0..479.
 */
int8_t FlashBuffer(uint16_t page)
{
	FlashPage(page, flash_buf, 256);
	return 0;
}

/*
void FlashErase(uint16_t page){
	HAS_CRITICAL_SECTION;

	ENTER_CRITICAL_SECTION();
	if(page >= 256) {
		if(page >= 480){
			LEAVE_CRITICAL_SECTION();
			return;
		}
		outp(1, RAMPZ);
	}
	else{
		outp(0, RAMPZ);
	}
	page <<= 8;

	SpmCommand(page, (1 << PGERS) | (1 << SPMEN));
	SpmCommand(0, (1 << RWWSRE) | (1 << SPMEN));
	LEAVE_CRITICAL_SECTION();
}
*/

