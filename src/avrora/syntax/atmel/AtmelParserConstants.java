/* Generated By:JavaCC: Do not edit this line. AtmelParserConstants.java */
package vpc.mach.avr.syntax.atmel;

public interface AtmelParserConstants {

  int EOF = 0;
  int SINGLE_LINE_COMMENT = 7;
  int INTEGER_LITERAL = 9;
  int DECIMAL_LITERAL = 10;
  int HEX_LITERAL = 11;
  int BIN_LITERAL = 12;
  int OCTAL_LITERAL = 13;
  int CHARACTER_LITERAL = 14;
  int STRING_LITERAL = 15;
  int LOW = 16;
  int HIGH = 17;
  int BYTE2 = 18;
  int BYTE3 = 19;
  int BYTE4 = 20;
  int LWRD = 21;
  int HWRD = 22;
  int PAGE = 23;
  int EXP2 = 24;
  int LOG2 = 25;
  int ADD = 26;
  int ADC = 27;
  int ADIW = 28;
  int AND = 29;
  int ANDI = 30;
  int ASR = 31;
  int BCLR = 32;
  int BLD = 33;
  int BRBC = 34;
  int BRBS = 35;
  int BRCC = 36;
  int BRCS = 37;
  int BREAK = 38;
  int BREQ = 39;
  int BRGE = 40;
  int BRHC = 41;
  int BRHS = 42;
  int BRID = 43;
  int BRIE = 44;
  int BRLO = 45;
  int BRLT = 46;
  int BRMI = 47;
  int BRNE = 48;
  int BRPL = 49;
  int BRSH = 50;
  int BRTC = 51;
  int BRTS = 52;
  int BRVC = 53;
  int BRVS = 54;
  int BSET = 55;
  int BST = 56;
  int CALL = 57;
  int CBI = 58;
  int CBR = 59;
  int CLC = 60;
  int CLH = 61;
  int CLI = 62;
  int CLN = 63;
  int CLR = 64;
  int CLS = 65;
  int CLT = 66;
  int CLV = 67;
  int CLZ = 68;
  int COM = 69;
  int CP = 70;
  int CPC = 71;
  int CPI = 72;
  int CPSE = 73;
  int DEC = 74;
  int EICALL = 75;
  int EIJMP = 76;
  int ELPM = 77;
  int EOR = 78;
  int FMUL = 79;
  int FMULS = 80;
  int FMULSU = 81;
  int ICALL = 82;
  int IJMP = 83;
  int IN = 84;
  int INC = 85;
  int JMP = 86;
  int LD = 87;
  int LDD = 88;
  int LDI = 89;
  int LDS = 90;
  int LPM = 91;
  int LSL = 92;
  int LSR = 93;
  int MOV = 94;
  int MOVW = 95;
  int MUL = 96;
  int MULS = 97;
  int MULSU = 98;
  int NEG = 99;
  int NOP = 100;
  int OR = 101;
  int ORI = 102;
  int OUT = 103;
  int POP = 104;
  int PUSH = 105;
  int RCALL = 106;
  int RET = 107;
  int RETI = 108;
  int RJMP = 109;
  int ROL = 110;
  int ROR = 111;
  int SBC = 112;
  int SBCI = 113;
  int SBI = 114;
  int SBIC = 115;
  int SBIS = 116;
  int SBIW = 117;
  int SBR = 118;
  int SBRC = 119;
  int SBRS = 120;
  int SEC = 121;
  int SEH = 122;
  int SEI = 123;
  int SEN = 124;
  int SER = 125;
  int SES = 126;
  int SET = 127;
  int SEV = 128;
  int SEZ = 129;
  int SLEEP = 130;
  int SPM = 131;
  int ST = 132;
  int STD = 133;
  int STS = 134;
  int SUB = 135;
  int SUBI = 136;
  int SWAP = 137;
  int TST = 138;
  int WDR = 139;
  int IDENTIFIER = 140;
  int LETTER = 141;
  int DIGIT = 142;

  int DEFAULT = 0;
  int IN_SINGLE_LINE_COMMENT = 1;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "\";\"",
    "<SINGLE_LINE_COMMENT>",
    "<token of kind 8>",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<BIN_LITERAL>",
    "<OCTAL_LITERAL>",
    "<CHARACTER_LITERAL>",
    "<STRING_LITERAL>",
    "\"low\"",
    "\"high\"",
    "\"byte2\"",
    "\"byte3\"",
    "\"byte4\"",
    "\"lwrd\"",
    "\"hwrd\"",
    "\"page\"",
    "\"exp2\"",
    "\"log2\"",
    "\"add\"",
    "\"adc\"",
    "\"adiw\"",
    "\"and\"",
    "\"andi\"",
    "\"asr\"",
    "\"bclr\"",
    "\"bld\"",
    "\"brbc\"",
    "\"brbs\"",
    "\"brcc\"",
    "\"brcs\"",
    "\"break\"",
    "\"breq\"",
    "\"brge\"",
    "\"brhc\"",
    "\"brhs\"",
    "\"brid\"",
    "\"brie\"",
    "\"brlo\"",
    "\"brlt\"",
    "\"brmi\"",
    "\"brne\"",
    "\"brpl\"",
    "\"brsh\"",
    "\"brtc\"",
    "\"brts\"",
    "\"brvc\"",
    "\"brvs\"",
    "\"bset\"",
    "\"bst\"",
    "\"call\"",
    "\"cbi\"",
    "\"cbr\"",
    "\"clc\"",
    "\"clh\"",
    "\"cli\"",
    "\"cln\"",
    "\"clr\"",
    "\"cls\"",
    "\"clt\"",
    "\"clv\"",
    "\"clz\"",
    "\"com\"",
    "\"cp\"",
    "\"cpc\"",
    "\"cpi\"",
    "\"cpse\"",
    "\"dec\"",
    "\"eicall\"",
    "\"eijmp\"",
    "\"elpm\"",
    "\"eor\"",
    "\"fmul\"",
    "\"fmuls\"",
    "\"fmulsu\"",
    "\"icall\"",
    "\"ijmp\"",
    "\"in\"",
    "\"inc\"",
    "\"jmp\"",
    "\"ld\"",
    "\"ldd\"",
    "\"ldi\"",
    "\"lds\"",
    "\"lpm\"",
    "\"lsl\"",
    "\"lsr\"",
    "\"mov\"",
    "\"movw\"",
    "\"mul\"",
    "\"muls\"",
    "\"mulsu\"",
    "\"neg\"",
    "\"nop\"",
    "\"or\"",
    "\"ori\"",
    "\"out\"",
    "\"pop\"",
    "\"push\"",
    "\"rcall\"",
    "\"ret\"",
    "\"reti\"",
    "\"rjmp\"",
    "\"rol\"",
    "\"ror\"",
    "\"sbc\"",
    "\"sbci\"",
    "\"sbi\"",
    "\"sbic\"",
    "\"sbis\"",
    "\"sbiw\"",
    "\"sbr\"",
    "\"sbrc\"",
    "\"sbrs\"",
    "\"sec\"",
    "\"seh\"",
    "\"sei\"",
    "\"sen\"",
    "\"ser\"",
    "\"ses\"",
    "\"set\"",
    "\"sev\"",
    "\"sez\"",
    "\"sleep\"",
    "\"spm\"",
    "\"st\"",
    "\"std\"",
    "\"sts\"",
    "\"sub\"",
    "\"subi\"",
    "\"swap\"",
    "\"tst\"",
    "\"wdr\"",
    "<IDENTIFIER>",
    "<LETTER>",
    "<DIGIT>",
    "\",\"",
    "\"+\"",
    "\"-\"",
    "\":\"",
    "\".equ\"",
    "\"=\"",
    "\".org\"",
    "\".byte\"",
    "\".db\"",
    "\".dw\"",
    "\".dd\"",
    "\".def\"",
    "\".include\"",
    "\".exit\"",
    "\".nolist\"",
    "\".list\"",
    "\".dseg\"",
    "\".cseg\"",
    "\".eseg\"",
    "\"||\"",
    "\"&&\"",
    "\"|\"",
    "\"^\"",
    "\"&\"",
    "\"==\"",
    "\"!=\"",
    "\">\"",
    "\">=\"",
    "\"<\"",
    "\"<=\"",
    "\"<<\"",
    "\">>\"",
    "\"*\"",
    "\"/\"",
    "\"!\"",
    "\"~\"",
    "\"(\"",
    "\")\"",
  };

}
