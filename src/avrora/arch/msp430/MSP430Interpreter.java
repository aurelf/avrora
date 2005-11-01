package avrora.arch.msp430;
import avrora.arch.*;
public abstract class MSP430Interpreter extends MSP430State implements MSP430InstrVisitor {
    boolean bit_get(int v, int bit) {
        return (v & (1 << bit)) != 0;
    }
    int bit_set(int v, int bit, boolean value) {
        if ( value ) return v | (1 << bit);
        else return v & ~(1 << bit);
    }
    int bit_update(int v, int mask, int e) {
        return (v & ~mask) | (e & mask);
    }
    int b2i(boolean v, int val) {
        if ( v ) return val;
        else return 0;
    }
    int $read_poly_int8(MSP430Operand o) {
        switch ( o.op_type ) {
            case MSP430Operand.AIREG_B_val: return $read_int8((MSP430Operand.AIREG_B)o);
            case MSP430Operand.IMM_val: return $read_int8((MSP430Operand.IMM)o);
            case MSP430Operand.INDX_val: return $read_int8((MSP430Operand.INDX)o);
            case MSP430Operand.ABSO_val: return $read_int8((MSP430Operand.ABSO)o);
            case MSP430Operand.SYM_val: return $read_int8((MSP430Operand.SYM)o);
            case MSP430Operand.IREG_val: return $read_int8((MSP430Operand.IREG)o);
            case MSP430Operand.SREG_val: return $read_int8((MSP430Operand.SREG)o);
        }
        throw cck.util.Util.failure("invalid operand type in read");
    }
    int $read_poly_uint16(MSP430Operand o) {
        switch ( o.op_type ) {
            case MSP430Operand.ABSO_val: return $read_uint16((MSP430Operand.ABSO)o);
            case MSP430Operand.AIREG_W_val: return $read_uint16((MSP430Operand.AIREG_W)o);
            case MSP430Operand.SREG_val: return $read_uint16((MSP430Operand.SREG)o);
            case MSP430Operand.IMM_val: return $read_uint16((MSP430Operand.IMM)o);
            case MSP430Operand.SYM_val: return $read_uint16((MSP430Operand.SYM)o);
            case MSP430Operand.IREG_val: return $read_uint16((MSP430Operand.IREG)o);
            case MSP430Operand.INDX_val: return $read_uint16((MSP430Operand.INDX)o);
        }
        throw cck.util.Util.failure("invalid operand type in read");
    }
    void $write_poly_int8(MSP430Operand o, int value) {
        switch ( o.op_type ) {
            case MSP430Operand.ABSO_val: $write_int8((MSP430Operand.ABSO)o, value); return;
            case MSP430Operand.AIREG_B_val: $write_int8((MSP430Operand.AIREG_B)o, value); return;
            case MSP430Operand.INDX_val: $write_int8((MSP430Operand.INDX)o, value); return;
            case MSP430Operand.SREG_val: $write_int8((MSP430Operand.SREG)o, value); return;
            case MSP430Operand.IMM_val: $write_int8((MSP430Operand.IMM)o, value); return;
            case MSP430Operand.SYM_val: $write_int8((MSP430Operand.SYM)o, value); return;
            case MSP430Operand.IREG_val: $write_int8((MSP430Operand.IREG)o, value); return;
        }
        throw cck.util.Util.failure("invalid operand type in write");
    }
    void $write_poly_uint16(MSP430Operand o, int value) {
        switch ( o.op_type ) {
            case MSP430Operand.ABSO_val: $write_uint16((MSP430Operand.ABSO)o, value); return;
            case MSP430Operand.SREG_val: $write_uint16((MSP430Operand.SREG)o, value); return;
            case MSP430Operand.IMM_val: $write_uint16((MSP430Operand.IMM)o, value); return;
            case MSP430Operand.AIREG_W_val: $write_uint16((MSP430Operand.AIREG_W)o, value); return;
            case MSP430Operand.IREG_val: $write_uint16((MSP430Operand.IREG)o, value); return;
            case MSP430Operand.SYM_val: $write_uint16((MSP430Operand.SYM)o, value); return;
            case MSP430Operand.INDX_val: $write_uint16((MSP430Operand.INDX)o, value); return;
        }
        throw cck.util.Util.failure("invalid operand type in write");
    }
    public int get_word(int addr) {
        return uword(map_get(sram, addr), map_get(sram, addr + 1));
    }
    public void set_word(int addr, int value) {
        map_set(sram, addr, low(value));
        map_set(sram, addr + 1, high(value));
    }
    public int performAddition(int r1, int r2, int carry) {
        int result = r1 + r2 + carry;
        boolean Rd7 = bit_get(r1, 7);
        boolean Rr7 = bit_get(r2, 7);
        boolean R7 = bit_get(result, 7);
        C = bit_get(result, 8);
        N = bit_get(result, 7);
        Z = low(result) == 0;
        V = Rd7 && Rr7 && !R7 || !Rd7 && !Rr7 && R7;
        return low(result);
    }
    public int performAdditionW(int r1, int r2, int carry) {
        int result = r1 + r2 + carry;
        boolean Rd15 = bit_get(r1, 15);
        boolean Rr15 = bit_get(r2, 15);
        boolean R15 = bit_get(result, 15);
        C = bit_get(result, 16);
        N = bit_get(result, 15);
        Z = result == 0;
        V = Rd15 && Rr15 && !R15 || !Rd15 && !Rr15 && R15;
        return result;
    }
    public int performSubtraction(int r1, int r2, int carry) {
        int result = r1 - r2 - carry;
        boolean Rd7 = bit_get(r1, 7);
        boolean Rr7 = bit_get(r2, 7);
        boolean R7 = bit_get(result, 7);
        C = !Rd7 && Rr7 || Rr7 && R7 || R7 && !Rd7;
        N = R7;
        Z = low(result) == 0;
        V = Rd7 && !Rr7 && !R7 || !Rd7 && Rr7 && R7;
        return low(result);
    }
    public int performSubtractionW(int r1, int r2, int carry) {
        int result = r1 - r2 - carry;
        boolean Rd15 = bit_get(r1, 15);
        boolean Rr15 = bit_get(r2, 15);
        boolean R15 = bit_get(result, 15);
        C = !Rd15 && Rr15 || Rr15 && R15 || R15 && !Rd15;
        N = R15;
        Z = low(result) == 0 && high(result) == 0;
        V = Rd15 && !Rr15 && !R15 || !Rd15 && Rr15 && R15;
        return result;
    }
    public int performAnd(int r1, int r2) {
        int result = r1 & r2;
        N = bit_get(result, 7);
        Z = low(result) == 0;
        V = false;
        C = !N;
        return low(result);
    }
    public int performAndW(int r1, int r2) {
        int result = r1 & r2;
        N = bit_get(result, 15);
        C = !N;
        Z = result == 0;
        V = false;
        return result;
    }
    public int performDeciAddCW(int r1, int r2, int carry) {
        int reg1 = r1;
        int reg2 = r2;
        int result = 0;
        reg1 = bit_update(reg1, 15, (reg1 & 15) + carry);
        result = bit_update(result, 15, (reg1 & 15) + (reg2 & 15));
        if ( (result & 15) > 10 ) {
            result = bit_update(result, 15, (result & 15) - 10);
            reg1 = bit_update(reg1, 240, (reg1 << 4 & 15) + 1 << 4);
        }
        result = bit_update(result, 240, (reg1 << 4 & 15) + (reg2 << 4 & 15) << 4);
        if ( (result << 4 & 15) > 10 ) {
            result = bit_update(result, 240, (result << 4 & 15) - 10 << 4);
            reg1 = bit_update(reg1, 3840, (reg1 << 8 & 15) + 1 << 8);
        }
        result = bit_update(result, 3840, (reg1 << 8 & 15) + (reg2 << 8 & 15) << 8);
        if ( (result << 8 & 15) > 10 ) {
            result = bit_update(result, 3840, (result << 8 & 15) - 10 << 8);
            reg1 = bit_update(reg1, 61440, (reg1 << 12 & 15) + 1 << 12);
        }
        result = bit_update(result, 61440, (reg1 << 12 & 15) + (reg2 << 12 & 15) << 12);
        if ( (result << 12 & 15) > 10 ) {
            result = bit_update(result, 61440, (result << 12 & 15) - 10 << 12);
            C = true;
        }
        N = bit_get(result, 15);
        Z = result == 0;
        return result;
    }
    public int performDeciAddC(int r1, int r2, int carry) {
        int reg1 = r1;
        int reg2 = r2;
        int result = 0;
        reg1 = bit_update(reg1, 15, (reg1 & 15) + carry);
        result = bit_update(result, 15, (reg1 & 15) + (reg2 & 15));
        if ( (result & 15) > 10 ) {
            result = bit_update(result, 15, (result & 15) - 10);
            reg1 = bit_update(reg1, 240, (reg1 << 4 & 15) + 1 << 4);
        }
        result = bit_update(result, 240, (reg1 << 4 & 15) + (reg2 << 4 & 15) << 4);
        if ( (result << 4 & 15) > 10 ) {
            result = bit_update(result, 240, (result << 4 & 15) - 10 << 4);
            C = true;
        }
        N = bit_get(result, 7);
        Z = result == 0;
        return result & 255;
    }
    public void pushPC(int npc) {
        npc = npc / 2;
        pushByte(low(npc));
        pushByte(high(npc));
    }
    public int popPC() {
        int high = popByte();
        int low = popByte();
        return uword(low, high) * 2;
    }
    public int low(int v) {
        return v << 24 >> 24;
    }
    public int high(int v) {
        return v >> 8 << 24 >> 24;
    }
    public int uword(int low, int high) {
        return (high << 8 | low) & 65535;
    }
    protected abstract int bit(boolean b);
    protected abstract int popByte();
    protected abstract void pushByte(int b);
    protected abstract void disableInterrupts();
    protected abstract void enableInterrupts();
    protected abstract int popWord();
    protected abstract void pushWord(int b);
    public int $read_uint16(MSP430Operand.SREG _this) {
        return map_get(regs, _this.value.value);
    }
    public int $read_int8(MSP430Operand.SREG _this) {
        return map_get(regs, _this.value.value) << 24 >> 24;
    }
    public void $write_uint16(MSP430Operand.SREG _this, int value) {
        map_set(regs, _this.value.value, value);
    }
    public void $write_int8(MSP430Operand.SREG _this, int value) {
        int $tmp_0 = _this.value.value;
        int $tmp_1 = map_get(regs, $tmp_0);
        $tmp_1 = bit_update($tmp_1, 255, value);
        map_set(regs, $tmp_0, $tmp_1);
    }
    public int $read_int8(MSP430Operand.AIREG_B _this) {
        int addr = map_get(regs, _this.value.value);
        map_set(regs, _this.value.value, addr + 1);
        return map_get(sram, addr);
    }
    public void $write_int8(MSP430Operand.AIREG_B _this, int value) {
    }
    public int $read_uint16(MSP430Operand.AIREG_W _this) {
        int addr = map_get(regs, _this.value.value);
        map_set(regs, _this.value.value, addr + 2);
        return map_get(sram, addr);
    }
    public void $write_uint16(MSP430Operand.AIREG_W _this, int value) {
    }
    public int $read_int8(MSP430Operand.IREG _this) {
        int addr = map_get(regs, _this.value.value);
        return map_get(sram, addr);
    }
    public int $read_uint16(MSP430Operand.IREG _this) {
        return get_word(map_get(regs, _this.value.value));
    }
    public void $write_int8(MSP430Operand.IREG _this, int value) {
    }
    public void $write_uint16(MSP430Operand.IREG _this, int value) {
    }
    public int $read_int8(MSP430Operand.IMM _this) {
        return _this.value << 24 >> 24;
    }
    public int $read_uint16(MSP430Operand.IMM _this) {
        return _this.value & 131071;
    }
    public void $write_int8(MSP430Operand.IMM _this, int value) {
    }
    public void $write_uint16(MSP430Operand.IMM _this, int value) {
    }
    public int $read_int8(MSP430Operand.INDX _this) {
        return map_get(sram, $read_uint16(_this.reg) + _this.index.value);
    }
    public int $read_uint16(MSP430Operand.INDX _this) {
        return get_word($read_uint16(_this.reg) + _this.index.value);
    }
    public void $write_int8(MSP430Operand.INDX _this, int value) {
        map_set(sram, $read_uint16(_this.reg) + _this.index.value, value);
    }
    public void $write_uint16(MSP430Operand.INDX _this, int value) {
        set_word($read_uint16(_this.reg) + _this.index.value, value);
    }
    public int $read_int8(MSP430Operand.SYM _this) {
        return map_get(sram, _this.value);
    }
    public int $read_uint16(MSP430Operand.SYM _this) {
        return get_word(_this.value);
    }
    public void $write_int8(MSP430Operand.SYM _this, int value) {
        map_set(sram, _this.value, value);
    }
    public void $write_uint16(MSP430Operand.SYM _this, int value) {
        set_word(_this.value, value);
    }
    public int $read_int8(MSP430Operand.ABSO _this) {
        return map_get(sram, _this.value);
    }
    public int $read_uint16(MSP430Operand.ABSO _this) {
        return get_word(_this.value);
    }
    public void $write_int8(MSP430Operand.ABSO _this, int value) {
        map_set(sram, _this.value, value);
    }
    public void $write_uint16(MSP430Operand.ABSO _this, int value) {
        set_word(_this.value, value);
    }
    public int $read_uint16(MSP430Operand.JUMP _this) {
        return _this.value & 131071;
    }
    public void visit(MSP430Instr.ADC i)  {
        int r1 = $read_poly_uint16(i.source);
        int result = r1 + bit(C);
        boolean R15 = bit_get(result, 15);
        boolean Rdh7 = bit_get(r1, 15);
        C = !R15 && Rdh7;
        N = R15;
        V = !Rdh7 && R15;
        Z = (result & 65535) == 0;
        $write_poly_uint16(i.source, result & 131071);
    }
    public void visit(MSP430Instr.ADC_B i)  {
        int r1 = $read_poly_int8(i.source);
        int result = r1 + bit(C);
        boolean R7 = bit_get(result, 7);
        boolean Rdh1 = bit_get(r1, 7);
        C = !R7 && Rdh1;
        N = R7;
        V = !Rdh1 && R7;
        Z = (result & 255) == 0;
        $write_poly_int8(i.source, result << 24 >> 24);
    }
    public void visit(MSP430Instr.ADD i)  {
        int r1 = $read_poly_uint16(i.source);
        int r2 = $read_poly_uint16(i.dest);
        int result = performAdditionW(r1, r2, 0);
        $write_poly_uint16(i.dest, result);
    }
    public void visit(MSP430Instr.ADD_B i)  {
        int r1 = $read_poly_int8(i.source);
        int r2 = $read_poly_int8(i.dest);
        int result = performAddition(r1, r2, 0);
        $write_poly_int8(i.dest, result);
    }
    public void visit(MSP430Instr.ADDC i)  {
        int r1 = $read_poly_uint16(i.source);
        int r2 = $read_poly_uint16(i.dest);
        int result = performAdditionW(r1, r2, bit(C));
        $write_poly_uint16(i.dest, result);
    }
    public void visit(MSP430Instr.ADDC_B i)  {
        int r1 = $read_poly_int8(i.source);
        int r2 = $read_poly_int8(i.dest);
        int result = performAddition(r1, r2, bit(C));
        $write_poly_int8(i.dest, result);
    }
    public void visit(MSP430Instr.AND i)  {
        int r1 = $read_poly_uint16(i.source);
        int r2 = $read_poly_uint16(i.dest);
        int result = performAndW(r1, r2);
        $write_poly_uint16(i.dest, result);
    }
    public void visit(MSP430Instr.AND_B i)  {
        int r1 = $read_poly_int8(i.source);
        int r2 = $read_poly_int8(i.dest);
        int result = performAnd(r1, r2);
        $write_poly_int8(i.dest, result);
    }
    public void visit(MSP430Instr.BIC i)  {
        $write_poly_uint16(i.dest, ~$read_poly_uint16(i.source) & $read_poly_uint16(i.dest));
    }
    public void visit(MSP430Instr.BIC_B i)  {
        $write_poly_int8(i.dest, ~$read_poly_int8(i.source) & $read_poly_int8(i.dest));
    }
    public void visit(MSP430Instr.BIS i)  {
        $write_poly_uint16(i.dest, $read_poly_uint16(i.source) | $read_poly_uint16(i.dest));
    }
    public void visit(MSP430Instr.BIS_B i)  {
        $write_poly_int8(i.dest, $read_poly_int8(i.source) | $read_poly_int8(i.dest));
    }
    public void visit(MSP430Instr.BIT i)  {
        performAndW($read_poly_uint16(i.source), $read_poly_uint16(i.dest));
    }
    public void visit(MSP430Instr.BIT_B i)  {
        performAndW($read_poly_int8(i.source), $read_poly_int8(i.dest));
    }
    public void visit(MSP430Instr.BR i)  {
        nextpc = $read_poly_uint16(i.source) & 65535;
    }
    public void visit(MSP430Instr.CALL i)  {
        int temp = $read_poly_uint16(i.source) & 65535;
        pushPC(nextpc);
        nextpc = temp;
    }
    public void visit(MSP430Instr.CLR i)  {
        int b = $read_poly_uint16(i.source);
        $write_poly_uint16(i.source, 0);
    }
    public void visit(MSP430Instr.CLR_B i)  {
        int b = $read_poly_int8(i.source);
        $write_poly_int8(i.source, 0);
    }
    public void visit(MSP430Instr.CLRC i)  {
        C = false;
    }
    public void visit(MSP430Instr.CLRN i)  {
        N = false;
    }
    public void visit(MSP430Instr.CLRZ i)  {
        Z = false;
    }
    public void visit(MSP430Instr.CMP i)  {
        performAdditionW($read_poly_uint16(i.source), ~$read_poly_uint16(i.dest), 1);
    }
    public void visit(MSP430Instr.CMP_B i)  {
        performAddition($read_poly_int8(i.source), ~$read_poly_int8(i.dest), 1);
    }
    public void visit(MSP430Instr.DADC i)  {
        $write_poly_uint16(i.source, performDeciAddCW(0, $read_poly_uint16(i.source), bit(C)));
    }
    public void visit(MSP430Instr.DADC_B i)  {
        $write_poly_int8(i.source, performDeciAddC(0, $read_poly_int8(i.source), bit(C)));
    }
    public void visit(MSP430Instr.DADD i)  {
        $write_poly_uint16(i.dest, performDeciAddCW($read_poly_uint16(i.source), $read_poly_uint16(i.dest), bit(C)));
    }
    public void visit(MSP430Instr.DADD_B i)  {
        $write_poly_int8(i.dest, performDeciAddC($read_poly_int8(i.source), $read_poly_int8(i.dest), bit(C)));
    }
    public void visit(MSP430Instr.DEC i)  {
        int original = $read_poly_uint16(i.source);
        $write_poly_uint16(i.source, performSubtractionW(original, 1, 0));
        if ( original == 1 ) {
            N = true;
        }
    }
    public void visit(MSP430Instr.DEC_B i)  {
        int original = $read_poly_int8(i.source);
        $write_poly_int8(i.source, performSubtraction(original, 1, 0));
        if ( original == 1 ) {
            N = true;
        }
    }
    public void visit(MSP430Instr.DECD i)  {
        int original = $read_poly_uint16(i.source);
        $write_poly_uint16(i.source, performSubtractionW(original, 2, 0));
        if ( original == 2 ) {
            N = true;
        }
    }
    public void visit(MSP430Instr.DECD_B i)  {
        int original = $read_poly_int8(i.source);
        $write_poly_int8(i.source, performSubtraction(original, 2, 0));
        if ( original == 2 ) {
            N = true;
        }
    }
    public void visit(MSP430Instr.DINT i)  {
        disableInterrupts();
    }
    public void visit(MSP430Instr.EINT i)  {
        enableInterrupts();
    }
    public void visit(MSP430Instr.INC i)  {
        $write_poly_uint16(i.source, performAdditionW($read_poly_uint16(i.source), 1, 0));
    }
    public void visit(MSP430Instr.INC_B i)  {
        $write_poly_int8(i.source, performAdditionW($read_poly_int8(i.source), 1, 0));
    }
    public void visit(MSP430Instr.INCD i)  {
        $write_poly_uint16(i.source, performAdditionW($read_poly_uint16(i.source), 2, 0));
    }
    public void visit(MSP430Instr.INCD_B i)  {
        $write_poly_int8(i.source, performAdditionW($read_poly_int8(i.source), 2, 0));
    }
    public void visit(MSP430Instr.INV i)  {
        int val = $read_poly_uint16(i.source);
        int result = ~val;
        N = bit_get(result, 15);
        Z = result == 65535;
        C = !(result == 0);
        V = val < 0;
        $write_poly_uint16(i.source, result & 131071);
    }
    public void visit(MSP430Instr.INV_B i)  {
        int val = $read_poly_int8(i.source);
        int result = ~val;
        N = bit_get(result, 7);
        Z = result == 255;
        C = !(result == 0);
        V = val < 0;
        $write_poly_int8(i.source, low(result));
    }
    public void visit(MSP430Instr.JC i)  {
        if ( C ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JHS i)  {
        if ( C ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JEQ i)  {
        if ( Z ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JZ i)  {
        if ( Z ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JGE i)  {
        if ( Z != N ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JL i)  {
        if ( N != V ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JMP i)  {
        nextpc = $read_uint16(i.target);
    }
    public void visit(MSP430Instr.JN i)  {
        if ( N ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JNC i)  {
        if ( !C ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JLO i)  {
        if ( !C ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JNE i)  {
        if ( !Z ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.JNZ i)  {
        if ( !Z ) {
            nextpc = $read_uint16(i.target);
        }
    }
    public void visit(MSP430Instr.MOV i)  {
        $write_poly_uint16(i.dest, $read_poly_uint16(i.source));
    }
    public void visit(MSP430Instr.MOV_B i)  {
        $write_poly_int8(i.dest, $read_poly_int8(i.source));
    }
    public void visit(MSP430Instr.NOP i)  {
    }
    public void visit(MSP430Instr.POP i)  {
        $write_poly_uint16(i.source, popWord());
    }
    public void visit(MSP430Instr.POP_B i)  {
        $write_poly_int8(i.source, popByte());
    }
    public void visit(MSP430Instr.PUSH i)  {
        pushWord($read_poly_uint16(i.source));
    }
    public void visit(MSP430Instr.PUSH_B i)  {
        pushByte($read_poly_int8(i.source));
    }
    public void visit(MSP430Instr.RET i)  {
        nextpc = popWord();
    }
    public void visit(MSP430Instr.RETI i)  {
        sreg = popWord();
        nextpc = popWord();
    }
    public void visit(MSP430Instr.RLA i)  {
        int dest = $read_poly_uint16(i.source);
        C = bit_get(dest, 15);
        V = dest > 16383 && dest < 49152;
        dest = dest << 1;
        N = bit_get(dest, 15);
        Z = dest == 0;
        $write_poly_uint16(i.source, dest);
    }
    public void visit(MSP430Instr.RLA_B i)  {
        int dest = $read_poly_int8(i.source);
        C = bit_get(dest, 7);
        V = dest > 63 && dest < 192;
        dest = dest << 1;
        N = bit_get(dest, 7);
        Z = dest == 0;
        $write_poly_int8(i.source, low(dest));
    }
    public void visit(MSP430Instr.RLC i)  {
        int dest = $read_poly_uint16(i.source);
        int tempC = bit(C);
        C = bit_get(dest, 15);
        V = dest > 16383 && dest < 49152;
        dest = dest << 1 | tempC;
        N = bit_get(dest, 15);
        Z = dest == 0;
        $write_poly_uint16(i.source, dest);
    }
    public void visit(MSP430Instr.RLC_B i)  {
        int dest = $read_poly_int8(i.source);
        int tempC = bit(C);
        C = bit_get(dest, 7);
        V = dest > 63 && dest < 192;
        dest = dest << 1 | tempC;
        N = bit_get(dest, 7);
        Z = dest == 0;
        $write_poly_int8(i.source, low(dest));
    }
    public void visit(MSP430Instr.RRA i)  {
        int temp = $read_poly_uint16(i.source);
        C = bit_get(temp, 0);
        temp = temp >> 1;
        N = bit_get(temp, 15);
        Z = temp == 0;
        V = false;
        $write_poly_uint16(i.source, temp);
    }
    public void visit(MSP430Instr.RRA_B i)  {
        int temp = $read_poly_int8(i.source);
        C = bit_get(temp, 0);
        temp = temp >> 1;
        N = bit_get(temp, 7);
        Z = temp == 0;
        V = false;
        $write_poly_int8(i.source, temp);
    }
    public void visit(MSP430Instr.RRC i)  {
        int temp = $read_poly_uint16(i.source);
        int oldC = bit(C);
        C = bit_get(temp, 0);
        temp = temp >> 1 | oldC << 15;
        N = bit_get(temp, 15);
        Z = temp == 0;
        V = !bit_get(temp, 14) && oldC == 1;
        $write_poly_uint16(i.source, temp);
    }
    public void visit(MSP430Instr.RRC_B i)  {
        int temp = $read_poly_int8(i.source);
        int oldC = bit(C);
        C = bit_get(temp, 0);
        temp = temp >> 1 | oldC << 7;
        N = bit_get(temp, 7);
        Z = temp == 0;
        V = !bit_get(temp, 6) && oldC == 1;
        $write_poly_int8(i.source, temp);
    }
    public void visit(MSP430Instr.SBC i)  {
        int dest = $read_poly_uint16(i.source);
        boolean highbit = bit_get(dest, 15);
        dest = dest + 65535 + bit(C);
        N = bit_get(dest, 15);
        Z = dest == 0;
        C = highbit != bit_get(dest, 15) && bit(C) == 0;
        V = dest > 65535;
        $write_poly_uint16(i.source, dest);
    }
    public void visit(MSP430Instr.SBC_B i)  {
        int dest = $read_poly_int8(i.source);
        boolean highbit = bit_get(dest, 7);
        dest = dest + 255 + bit(C);
        N = bit_get(dest, 7);
        Z = dest == 0;
        C = highbit != bit_get(dest, 7) && bit(C) == 0;
        V = dest > 255;
        $write_poly_int8(i.source, dest);
    }
    public void visit(MSP430Instr.SETC i)  {
        C = true;
    }
    public void visit(MSP430Instr.SETN i)  {
        N = true;
    }
    public void visit(MSP430Instr.SETZ i)  {
        Z = true;
    }
    public void visit(MSP430Instr.SUB i)  {
        int r1 = $read_poly_uint16(i.source);
        int r2 = $read_poly_uint16(i.dest);
        int results = performSubtractionW(r1, r2, 0);
        $write_poly_uint16(i.dest, results & 131071);
    }
    public void visit(MSP430Instr.SUB_B i)  {
        int r1 = $read_poly_int8(i.source);
        int r2 = $read_poly_int8(i.dest);
        int results = performSubtraction(r1, r2, 0);
        $write_poly_int8(i.dest, low(results));
    }
    public void visit(MSP430Instr.SUBC i)  {
        int r1 = $read_poly_uint16(i.source);
        int r2 = $read_poly_uint16(i.dest);
        int results = performSubtractionW(r1, r2, bit(C));
        $write_poly_uint16(i.dest, results & 131071);
    }
    public void visit(MSP430Instr.SUBC_B i)  {
        int r1 = $read_poly_int8(i.source);
        int r2 = $read_poly_int8(i.dest);
        int results = performSubtraction(r1, r2, bit(C));
        $write_poly_int8(i.dest, low(results));
    }
    public void visit(MSP430Instr.SBB i)  {
        int r1 = $read_poly_uint16(i.source);
        int r2 = $read_poly_uint16(i.dest);
        int results = performSubtractionW(r1, r2, bit(C));
        $write_poly_uint16(i.dest, results & 131071);
    }
    public void visit(MSP430Instr.SBB_B i)  {
        int r1 = $read_poly_int8(i.source);
        int r2 = $read_poly_int8(i.dest);
        int results = performSubtraction(r1, r2, bit(C));
        $write_poly_int8(i.dest, low(results));
    }
    public void visit(MSP430Instr.SWPB i)  {
        int temp1 = $read_poly_uint16(i.source);
        int temp2 = temp1;
        temp2 = bit_update(temp2, 65280, temp1 >> 8 & 65280);
        temp2 = bit_update(temp2, 255, temp1 << 8 & 255);
        $write_poly_uint16(i.source, temp2);
    }
    public void visit(MSP430Instr.SXT i)  {
        int r1 = $read_poly_uint16(i.source);
        r1 = r1 << 8 >> 8;
        N = bit_get(r1, 15);
        Z = r1 == 0;
        C = !Z;
        V = false;
        $write_poly_uint16(i.source, r1);
    }
    public void visit(MSP430Instr.TST i)  {
        int r1 = $read_poly_uint16(i.source);
        N = bit_get(r1, 15);
        Z = r1 == 0;
        C = true;
        V = false;
    }
    public void visit(MSP430Instr.TST_B i)  {
        int r1 = $read_poly_int8(i.source);
        N = bit_get(r1, 7);
        Z = r1 == 0;
        C = true;
        V = false;
    }
    public void visit(MSP430Instr.XOR i)  {
        int src = $read_poly_uint16(i.source);
        int res = $read_poly_uint16(i.dest);
        res = res ^ src;
        N = bit_get(res, 15);
        Z = res == 0;
        C = !Z;
        V = src < 0 && res < 0;
        $write_poly_uint16(i.dest, res & 131071);
    }
    public void visit(MSP430Instr.XOR_B i)  {
        int src = $read_poly_int8(i.source);
        int res = $read_poly_int8(i.dest);
        res = res ^ src;
        N = bit_get(res, 7);
        Z = res == 0;
        C = !Z;
        V = src < 0 && res < 0;
        $write_poly_int8(i.dest, low(res));
    }
}
