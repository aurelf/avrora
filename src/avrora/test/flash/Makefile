PROJ = measure
SRCS += flash_api.c $(PROJ).c
OBJS += flash.o
OBJS += $(SRCS:.c=.o)

TRG = avr-
MCU = atmega128

CC  = $(TRG)gcc
AS     = $(TRG)gcc
OBJCOPY = $(TRG)objcopy
OBJDUMP = $(TRG)objdump
SIZE = $(TRG)size -A

TEXT_START = -Wl,--section-start=.text=0x1e000
MCUFLAG = -mmcu=$(MCU)
CFLAGS = $(MCUFLAG) $(STACK_START) -Os -Wall -Wa,-adhlmsn=$(notdir $(<:.c=.lst)) $(DEFS)
LDFLAGS = $(MCUFLAG) -Wl,-Map=$(PROJ).map,--cref $(TEXT_START) $(DEV_START)
SFLAGS = $(MCUFLAG) $(STACK_START)  -Wa,$(MCUFLAG),-gstabs,-ahlms=$(notdir $(<:.S=.lst))
PROG=dapa
INSTALLER_PORG = -dprog=$(PROG)                                                                                
# TODO: fix this to handle alternative parallel ports
PORT= 
ifneq ($(PORT),)
INSTALLER_PORT = -dserial=$(PORT)
endif
                                                                               
IP=
ifneq ($(IP),)
INSTALLER_PORT = -dhost=$(IP)
endif
                                                                               
INSTALLER_PART = -dpart=$(MCU)
INSTALLER = uisp $(INSTALLER_PART) $(INSTALLER_PORG) $(INSTALLER_PORT)


%.o : %.c
	$(CC) -c $(CFLAGS) $(INCDIR) $< -o $@
                                                                               
%.s : %.c
	$(CC) -c $(CFLAGS) $(INCDIR) $< -o $@
                                                                               
%.o : %.S
	$(AS) -c $(SFLAGS) $(INCDIR) $< -o $@
                                                                               
%.elf: $(OBJS)
	$(CC) $(OBJS) $(LIBS) $(LDFLAGS) -o $@
	$(SIZE) $@
                                                                               
%.exe: $(OBJS)
	$(CC) $(OBJS) $(LIBS) $(LDFLAGS) -o $@
                                                                               
%.srec : %.elf
	$(OBJCOPY) -O srec -R .eeprom $< $@
                                                                               
%.cof : %.elf
	$(OBJCOPY) --debugging -O coff-ext-avr \
	--change-section-address .data-0x800000 \
	--change-section-address .bss-0x800000 \
	--change-section-address .noinit-0x800000 \
	--change-section-address .eeprom-0x810000 \
	$< $@
                                                                               
%.elf.lst: %.elf
	$(OBJDUMP) -h -S $< > $@
                                                                               

all: $(PROJ).elf $(PROJ).elf.lst $(PROJ).srec $(OBJS)

install: $(PROJ).srec
	$(INSTALLER) --erase
	sleep 1
	$(INSTALLER) --upload if=$<
	sleep 1
	$(INSTALLER) --verify if=$<
                                                                               
setup:
	$(INSTALLER) --wr_fuse_l=0xff
	sleep 1
	$(INSTALLER) --wr_fuse_h=0x98
	sleep 1
	$(INSTALLER) --wr_fuse_e=0xfe

clean:
	rm -f *.o *.srec *.elf *.lst $(PROJ).map $(PROJ).exe .build.tmp

