#ifndef _FLASH_H
#define _FLASH_H

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
 *
 */

/*
 * $Log: flash.h,v $
 * Revision 1.1  2005/04/09 01:06:47  titzer
 * added ability to boot from non-zero starting address as well as relocate interrupt table; added flash test case; added storeProgramMemory() method to interpreter that is current unimplemented
 *
 * Revision 1.2  2004/07/01 22:02:42  roy
 * Merged roy-spring-04 branch into the head
 *
 * Revision 1.1.2.1  2004/06/25 22:46:04  roy
 * Whops on my flash placment.  Better now.  This all came from my moving flash.h
 * from $(BASE)/dev
 *
 * Revision 1.1.2.1  2004/06/25 22:34:19  roy
 * Moved flash into device specific location.
 *
 * Revision 1.1.1.1.2.1  2004/06/25 20:56:16  roy
 * Broke the dependency on uart0 (relic from old mica motes) and moved the uart
 * interface into the kernel.
 *
 * Revision 1.1.1.1  2004/05/07 09:16:00  simonhan
 * SOS Public Release
 *
 * Revision 1.1.1.1  2004/04/02 18:08:34  simonhan
 * sos 0.2.3
 *
 * Revision 1.1.1.1  2004/03/23 19:47:19  simonhan
 * SOS 0.2
 *
 * Revision 1.8  2004/02/22 16:57:42  simonhan
 * change radio and uart to new packet buffer interface.
 * please use CMD_PKT_SENDDONE instead of MSG_PKT_SENDDONE
 *
 * Revision 1.7  2004/02/22 01:35:03  simonhan
 * merge VISA to bootloader
 * change MSG_FINAL to CMD_FINAL
 *
 * Revision 1.6  2004/02/12 16:22:08  simonhan
 * update tools.
 *
 * Revision 1.5  2004/02/09 23:33:39  simonhan
 * fix bugs in malloc.c
 * fix bugs in flash_api.c
 * remove 256 bytes buffer for moduled
 *
 * Revision 1.4  2004/02/09 06:15:59  simonhan
 * check in updated bootloader code.
 * Instead of using buffer in RAM, SPM buffer is used to store the fragment of code to be flash.  Note that this code has not yet fully tested.  Use at your own risk.
 *
 * Revision 1.3  2004/02/08 18:20:43  simonhan
 * Add documentation
 *
 * Revision 1.2  2004/02/08 17:59:59  simonhan
 * remove ^M characters
 *
 * Revision 1.1.1.1  2004/01/28 04:33:07  simonhan
 * SensorOS
 *
 * Revision 1.1  2002/08/01 17:34:30  harald
 * First check in
 * 
 * 11/16/2003 Simon Han
 * modified to use inttypes
 *
 */
//#include <sos.h>
#include <inttypes.h>

//#define BOOTLOADER_SECTION    __attribute__ ((section (".bootloader")))

/**
 * @brief fill a byte in SPM buffer
 * @param addr byte address in the buffer
 * @param data data to be stored
 * note that storing data in SPM buffer does not
 * flash the programming memory.
 */
void SpmBufferFill(uint16_t addr, uint16_t data);

/**
 * @brief execute SPM command
 * @param addr flash memory address in terms of page
 * @param function command to be executed
 */
void SpmCommand(uint16_t addr, uint8_t function);

/**
 * @brief Erase and program a page in the flash ROM.
 *
 * @param page The page number to program, 0..479.
 * @param data Pointer to the new page contents.
 * @param len  Number of bytes to program. If this is
 *             less than 256, then the remaining bytes
 *             will be filled with 0xFF.
 */
void FlashPage(uint16_t page, void *data, uint16_t len);

/**
 * @brief Flash SPM buffer into flash
 * @param The page number to program, 0..479.
 * Note that this assumes the data is already in the buffer
 */
int8_t FlashBuffer(uint16_t page);

/**
 * @brief Copy data into SPM buffer
 * @param start starting byte address
 * @param data  data pointer
 * @param len   data length
 * Note that this routine assumes length is even
 */
void FlashCopy(uint16_t start, void *data, uint16_t len);

/**
 * @brief erase the data in flash buffer
 */
void FlashBufferErase();

/**
 * @brief load flash buffer from flash
 * @param page the page number in the flash
 * @param section the seciton number in the flash
 *
 * We devide the page into 4 section.  Each section is 64 bytes long
 */
void FlashBufferLoad(uint16_t page, uint16_t section);

/**
 * @brief Erase a block in Flash
 * @param page the page number
 */
//void FlashErase(uint16_t page);

#endif
