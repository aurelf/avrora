package avrora.arch.avr;
import java.util.HashMap;
public abstract class AVRInstrBuilder {
    public abstract AVRInstr build(AVRAddrMode am);
    static final HashMap builders = new HashMap();
    static AVRInstrBuilder add(String name, AVRInstrBuilder b) {
        builders.put(name, b);
        return b;
    }
    public static class ADC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ADC((AVRAddrMode.$adc$)am);
        }
    }
    public static class ADD_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ADD((AVRAddrMode.$add$)am);
        }
    }
    public static class ADIW_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ADIW((AVRAddrMode.$adiw$)am);
        }
    }
    public static class AND_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.AND((AVRAddrMode.$and$)am);
        }
    }
    public static class ANDI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ANDI((AVRAddrMode.$andi$)am);
        }
    }
    public static class ASR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ASR((AVRAddrMode.$asr$)am);
        }
    }
    public static class BCLR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BCLR((AVRAddrMode.$bclr$)am);
        }
    }
    public static class BLD_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BLD((AVRAddrMode.$bld$)am);
        }
    }
    public static class BRBC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRBC((AVRAddrMode.$brbc$)am);
        }
    }
    public static class BRBS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRBS((AVRAddrMode.$brbs$)am);
        }
    }
    public static class BRCC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRCC((AVRAddrMode.$brcc$)am);
        }
    }
    public static class BRCS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRCS((AVRAddrMode.$brcs$)am);
        }
    }
    public static class BREAK_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BREAK((AVRAddrMode.$break$)am);
        }
    }
    public static class BREQ_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BREQ((AVRAddrMode.$breq$)am);
        }
    }
    public static class BRGE_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRGE((AVRAddrMode.$brge$)am);
        }
    }
    public static class BRHC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRHC((AVRAddrMode.$brhc$)am);
        }
    }
    public static class BRHS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRHS((AVRAddrMode.$brhs$)am);
        }
    }
    public static class BRID_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRID((AVRAddrMode.$brid$)am);
        }
    }
    public static class BRIE_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRIE((AVRAddrMode.$brie$)am);
        }
    }
    public static class BRLO_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRLO((AVRAddrMode.$brlo$)am);
        }
    }
    public static class BRLT_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRLT((AVRAddrMode.$brlt$)am);
        }
    }
    public static class BRMI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRMI((AVRAddrMode.$brmi$)am);
        }
    }
    public static class BRNE_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRNE((AVRAddrMode.$brne$)am);
        }
    }
    public static class BRPL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRPL((AVRAddrMode.$brpl$)am);
        }
    }
    public static class BRSH_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRSH((AVRAddrMode.$brsh$)am);
        }
    }
    public static class BRTC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRTC((AVRAddrMode.$brtc$)am);
        }
    }
    public static class BRTS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRTS((AVRAddrMode.$brts$)am);
        }
    }
    public static class BRVC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRVC((AVRAddrMode.$brvc$)am);
        }
    }
    public static class BRVS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BRVS((AVRAddrMode.$brvs$)am);
        }
    }
    public static class BSET_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BSET((AVRAddrMode.$bset$)am);
        }
    }
    public static class BST_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.BST((AVRAddrMode.$bst$)am);
        }
    }
    public static class CALL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CALL((AVRAddrMode.$call$)am);
        }
    }
    public static class CBI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CBI((AVRAddrMode.$cbi$)am);
        }
    }
    public static class CBR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CBR((AVRAddrMode.$cbr$)am);
        }
    }
    public static class CLC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLC((AVRAddrMode.$clc$)am);
        }
    }
    public static class CLH_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLH((AVRAddrMode.$clh$)am);
        }
    }
    public static class CLI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLI((AVRAddrMode.$cli$)am);
        }
    }
    public static class CLN_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLN((AVRAddrMode.$cln$)am);
        }
    }
    public static class CLR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLR((AVRAddrMode.$clr$)am);
        }
    }
    public static class CLS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLS((AVRAddrMode.$cls$)am);
        }
    }
    public static class CLT_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLT((AVRAddrMode.$clt$)am);
        }
    }
    public static class CLV_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLV((AVRAddrMode.$clv$)am);
        }
    }
    public static class CLZ_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CLZ((AVRAddrMode.$clz$)am);
        }
    }
    public static class COM_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.COM((AVRAddrMode.$com$)am);
        }
    }
    public static class CP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CP((AVRAddrMode.$cp$)am);
        }
    }
    public static class CPC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CPC((AVRAddrMode.$cpc$)am);
        }
    }
    public static class CPI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CPI((AVRAddrMode.$cpi$)am);
        }
    }
    public static class CPSE_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.CPSE((AVRAddrMode.$cpse$)am);
        }
    }
    public static class DEC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.DEC((AVRAddrMode.$dec$)am);
        }
    }
    public static class EICALL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.EICALL((AVRAddrMode.$eicall$)am);
        }
    }
    public static class EIJMP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.EIJMP((AVRAddrMode.$eijmp$)am);
        }
    }
    public static class EOR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.EOR((AVRAddrMode.$eor$)am);
        }
    }
    public static class FMUL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.FMUL((AVRAddrMode.$fmul$)am);
        }
    }
    public static class FMULS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.FMULS((AVRAddrMode.$fmuls$)am);
        }
    }
    public static class FMULSU_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.FMULSU((AVRAddrMode.$fmulsu$)am);
        }
    }
    public static class ICALL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ICALL((AVRAddrMode.$icall$)am);
        }
    }
    public static class IJMP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.IJMP((AVRAddrMode.$ijmp$)am);
        }
    }
    public static class IN_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.IN((AVRAddrMode.$in$)am);
        }
    }
    public static class INC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.INC((AVRAddrMode.$inc$)am);
        }
    }
    public static class JMP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.JMP((AVRAddrMode.$jmp$)am);
        }
    }
    public static class LDD_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.LDD((AVRAddrMode.$ldd$)am);
        }
    }
    public static class LDI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.LDI((AVRAddrMode.$ldi$)am);
        }
    }
    public static class LDS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.LDS((AVRAddrMode.$lds$)am);
        }
    }
    public static class LSL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.LSL((AVRAddrMode.$lsl$)am);
        }
    }
    public static class LSR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.LSR((AVRAddrMode.$lsr$)am);
        }
    }
    public static class MOV_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.MOV((AVRAddrMode.$mov$)am);
        }
    }
    public static class MOVW_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.MOVW((AVRAddrMode.$movw$)am);
        }
    }
    public static class MUL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.MUL((AVRAddrMode.$mul$)am);
        }
    }
    public static class MULS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.MULS((AVRAddrMode.$muls$)am);
        }
    }
    public static class MULSU_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.MULSU((AVRAddrMode.$mulsu$)am);
        }
    }
    public static class NEG_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.NEG((AVRAddrMode.$neg$)am);
        }
    }
    public static class NOP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.NOP((AVRAddrMode.$nop$)am);
        }
    }
    public static class OR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.OR((AVRAddrMode.$or$)am);
        }
    }
    public static class ORI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ORI((AVRAddrMode.$ori$)am);
        }
    }
    public static class OUT_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.OUT((AVRAddrMode.$out$)am);
        }
    }
    public static class POP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.POP((AVRAddrMode.$pop$)am);
        }
    }
    public static class PUSH_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.PUSH((AVRAddrMode.$push$)am);
        }
    }
    public static class RCALL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.RCALL((AVRAddrMode.$rcall$)am);
        }
    }
    public static class RET_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.RET((AVRAddrMode.$ret$)am);
        }
    }
    public static class RETI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.RETI((AVRAddrMode.$reti$)am);
        }
    }
    public static class RJMP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.RJMP((AVRAddrMode.$rjmp$)am);
        }
    }
    public static class ROL_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ROL((AVRAddrMode.$rol$)am);
        }
    }
    public static class ROR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ROR((AVRAddrMode.$ror$)am);
        }
    }
    public static class SBC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBC((AVRAddrMode.$sbc$)am);
        }
    }
    public static class SBCI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBCI((AVRAddrMode.$sbci$)am);
        }
    }
    public static class SBI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBI((AVRAddrMode.$sbi$)am);
        }
    }
    public static class SBIC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBIC((AVRAddrMode.$sbic$)am);
        }
    }
    public static class SBIS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBIS((AVRAddrMode.$sbis$)am);
        }
    }
    public static class SBIW_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBIW((AVRAddrMode.$sbiw$)am);
        }
    }
    public static class SBR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBR((AVRAddrMode.$sbr$)am);
        }
    }
    public static class SBRC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBRC((AVRAddrMode.$sbrc$)am);
        }
    }
    public static class SBRS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SBRS((AVRAddrMode.$sbrs$)am);
        }
    }
    public static class SEC_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SEC((AVRAddrMode.$sec$)am);
        }
    }
    public static class SEH_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SEH((AVRAddrMode.$seh$)am);
        }
    }
    public static class SEI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SEI((AVRAddrMode.$sei$)am);
        }
    }
    public static class SEN_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SEN((AVRAddrMode.$sen$)am);
        }
    }
    public static class SER_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SER((AVRAddrMode.$ser$)am);
        }
    }
    public static class SES_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SES((AVRAddrMode.$ses$)am);
        }
    }
    public static class SET_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SET((AVRAddrMode.$set$)am);
        }
    }
    public static class SEV_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SEV((AVRAddrMode.$sev$)am);
        }
    }
    public static class SEZ_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SEZ((AVRAddrMode.$sez$)am);
        }
    }
    public static class SLEEP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SLEEP((AVRAddrMode.$sleep$)am);
        }
    }
    public static class SPM_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SPM((AVRAddrMode.$spm$)am);
        }
    }
    public static class STD_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.STD((AVRAddrMode.$std$)am);
        }
    }
    public static class STS_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.STS((AVRAddrMode.$sts$)am);
        }
    }
    public static class SUB_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SUB((AVRAddrMode.$sub$)am);
        }
    }
    public static class SUBI_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SUBI((AVRAddrMode.$subi$)am);
        }
    }
    public static class SWAP_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.SWAP((AVRAddrMode.$swap$)am);
        }
    }
    public static class TST_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.TST((AVRAddrMode.$tst$)am);
        }
    }
    public static class WDR_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.WDR((AVRAddrMode.$wdr$)am);
        }
    }
    public static class ELPM_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ELPM((AVRAddrMode.XLPM)am);
        }
    }
    public static class LPM_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.LPM((AVRAddrMode.XLPM)am);
        }
    }
    public static class LD_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.LD((AVRAddrMode.LD_ST)am);
        }
    }
    public static class ST_builder extends AVRInstrBuilder {
        public AVRInstr build(AVRAddrMode am) {
            return new AVRInstr.ST((AVRAddrMode.LD_ST)am);
        }
    }
    public static final AVRInstrBuilder ADC = add("adc", new ADC_builder());
    public static final AVRInstrBuilder ADD = add("add", new ADD_builder());
    public static final AVRInstrBuilder ADIW = add("adiw", new ADIW_builder());
    public static final AVRInstrBuilder AND = add("and", new AND_builder());
    public static final AVRInstrBuilder ANDI = add("andi", new ANDI_builder());
    public static final AVRInstrBuilder ASR = add("asr", new ASR_builder());
    public static final AVRInstrBuilder BCLR = add("bclr", new BCLR_builder());
    public static final AVRInstrBuilder BLD = add("bld", new BLD_builder());
    public static final AVRInstrBuilder BRBC = add("brbc", new BRBC_builder());
    public static final AVRInstrBuilder BRBS = add("brbs", new BRBS_builder());
    public static final AVRInstrBuilder BRCC = add("brcc", new BRCC_builder());
    public static final AVRInstrBuilder BRCS = add("brcs", new BRCS_builder());
    public static final AVRInstrBuilder BREAK = add("break", new BREAK_builder());
    public static final AVRInstrBuilder BREQ = add("breq", new BREQ_builder());
    public static final AVRInstrBuilder BRGE = add("brge", new BRGE_builder());
    public static final AVRInstrBuilder BRHC = add("brhc", new BRHC_builder());
    public static final AVRInstrBuilder BRHS = add("brhs", new BRHS_builder());
    public static final AVRInstrBuilder BRID = add("brid", new BRID_builder());
    public static final AVRInstrBuilder BRIE = add("brie", new BRIE_builder());
    public static final AVRInstrBuilder BRLO = add("brlo", new BRLO_builder());
    public static final AVRInstrBuilder BRLT = add("brlt", new BRLT_builder());
    public static final AVRInstrBuilder BRMI = add("brmi", new BRMI_builder());
    public static final AVRInstrBuilder BRNE = add("brne", new BRNE_builder());
    public static final AVRInstrBuilder BRPL = add("brpl", new BRPL_builder());
    public static final AVRInstrBuilder BRSH = add("brsh", new BRSH_builder());
    public static final AVRInstrBuilder BRTC = add("brtc", new BRTC_builder());
    public static final AVRInstrBuilder BRTS = add("brts", new BRTS_builder());
    public static final AVRInstrBuilder BRVC = add("brvc", new BRVC_builder());
    public static final AVRInstrBuilder BRVS = add("brvs", new BRVS_builder());
    public static final AVRInstrBuilder BSET = add("bset", new BSET_builder());
    public static final AVRInstrBuilder BST = add("bst", new BST_builder());
    public static final AVRInstrBuilder CALL = add("call", new CALL_builder());
    public static final AVRInstrBuilder CBI = add("cbi", new CBI_builder());
    public static final AVRInstrBuilder CBR = add("cbr", new CBR_builder());
    public static final AVRInstrBuilder CLC = add("clc", new CLC_builder());
    public static final AVRInstrBuilder CLH = add("clh", new CLH_builder());
    public static final AVRInstrBuilder CLI = add("cli", new CLI_builder());
    public static final AVRInstrBuilder CLN = add("cln", new CLN_builder());
    public static final AVRInstrBuilder CLR = add("clr", new CLR_builder());
    public static final AVRInstrBuilder CLS = add("cls", new CLS_builder());
    public static final AVRInstrBuilder CLT = add("clt", new CLT_builder());
    public static final AVRInstrBuilder CLV = add("clv", new CLV_builder());
    public static final AVRInstrBuilder CLZ = add("clz", new CLZ_builder());
    public static final AVRInstrBuilder COM = add("com", new COM_builder());
    public static final AVRInstrBuilder CP = add("cp", new CP_builder());
    public static final AVRInstrBuilder CPC = add("cpc", new CPC_builder());
    public static final AVRInstrBuilder CPI = add("cpi", new CPI_builder());
    public static final AVRInstrBuilder CPSE = add("cpse", new CPSE_builder());
    public static final AVRInstrBuilder DEC = add("dec", new DEC_builder());
    public static final AVRInstrBuilder EICALL = add("eicall", new EICALL_builder());
    public static final AVRInstrBuilder EIJMP = add("eijmp", new EIJMP_builder());
    public static final AVRInstrBuilder EOR = add("eor", new EOR_builder());
    public static final AVRInstrBuilder FMUL = add("fmul", new FMUL_builder());
    public static final AVRInstrBuilder FMULS = add("fmuls", new FMULS_builder());
    public static final AVRInstrBuilder FMULSU = add("fmulsu", new FMULSU_builder());
    public static final AVRInstrBuilder ICALL = add("icall", new ICALL_builder());
    public static final AVRInstrBuilder IJMP = add("ijmp", new IJMP_builder());
    public static final AVRInstrBuilder IN = add("in", new IN_builder());
    public static final AVRInstrBuilder INC = add("inc", new INC_builder());
    public static final AVRInstrBuilder JMP = add("jmp", new JMP_builder());
    public static final AVRInstrBuilder LDD = add("ldd", new LDD_builder());
    public static final AVRInstrBuilder LDI = add("ldi", new LDI_builder());
    public static final AVRInstrBuilder LDS = add("lds", new LDS_builder());
    public static final AVRInstrBuilder LSL = add("lsl", new LSL_builder());
    public static final AVRInstrBuilder LSR = add("lsr", new LSR_builder());
    public static final AVRInstrBuilder MOV = add("mov", new MOV_builder());
    public static final AVRInstrBuilder MOVW = add("movw", new MOVW_builder());
    public static final AVRInstrBuilder MUL = add("mul", new MUL_builder());
    public static final AVRInstrBuilder MULS = add("muls", new MULS_builder());
    public static final AVRInstrBuilder MULSU = add("mulsu", new MULSU_builder());
    public static final AVRInstrBuilder NEG = add("neg", new NEG_builder());
    public static final AVRInstrBuilder NOP = add("nop", new NOP_builder());
    public static final AVRInstrBuilder OR = add("or", new OR_builder());
    public static final AVRInstrBuilder ORI = add("ori", new ORI_builder());
    public static final AVRInstrBuilder OUT = add("out", new OUT_builder());
    public static final AVRInstrBuilder POP = add("pop", new POP_builder());
    public static final AVRInstrBuilder PUSH = add("push", new PUSH_builder());
    public static final AVRInstrBuilder RCALL = add("rcall", new RCALL_builder());
    public static final AVRInstrBuilder RET = add("ret", new RET_builder());
    public static final AVRInstrBuilder RETI = add("reti", new RETI_builder());
    public static final AVRInstrBuilder RJMP = add("rjmp", new RJMP_builder());
    public static final AVRInstrBuilder ROL = add("rol", new ROL_builder());
    public static final AVRInstrBuilder ROR = add("ror", new ROR_builder());
    public static final AVRInstrBuilder SBC = add("sbc", new SBC_builder());
    public static final AVRInstrBuilder SBCI = add("sbci", new SBCI_builder());
    public static final AVRInstrBuilder SBI = add("sbi", new SBI_builder());
    public static final AVRInstrBuilder SBIC = add("sbic", new SBIC_builder());
    public static final AVRInstrBuilder SBIS = add("sbis", new SBIS_builder());
    public static final AVRInstrBuilder SBIW = add("sbiw", new SBIW_builder());
    public static final AVRInstrBuilder SBR = add("sbr", new SBR_builder());
    public static final AVRInstrBuilder SBRC = add("sbrc", new SBRC_builder());
    public static final AVRInstrBuilder SBRS = add("sbrs", new SBRS_builder());
    public static final AVRInstrBuilder SEC = add("sec", new SEC_builder());
    public static final AVRInstrBuilder SEH = add("seh", new SEH_builder());
    public static final AVRInstrBuilder SEI = add("sei", new SEI_builder());
    public static final AVRInstrBuilder SEN = add("sen", new SEN_builder());
    public static final AVRInstrBuilder SER = add("ser", new SER_builder());
    public static final AVRInstrBuilder SES = add("ses", new SES_builder());
    public static final AVRInstrBuilder SET = add("set", new SET_builder());
    public static final AVRInstrBuilder SEV = add("sev", new SEV_builder());
    public static final AVRInstrBuilder SEZ = add("sez", new SEZ_builder());
    public static final AVRInstrBuilder SLEEP = add("sleep", new SLEEP_builder());
    public static final AVRInstrBuilder SPM = add("spm", new SPM_builder());
    public static final AVRInstrBuilder STD = add("std", new STD_builder());
    public static final AVRInstrBuilder STS = add("sts", new STS_builder());
    public static final AVRInstrBuilder SUB = add("sub", new SUB_builder());
    public static final AVRInstrBuilder SUBI = add("subi", new SUBI_builder());
    public static final AVRInstrBuilder SWAP = add("swap", new SWAP_builder());
    public static final AVRInstrBuilder TST = add("tst", new TST_builder());
    public static final AVRInstrBuilder WDR = add("wdr", new WDR_builder());
    public static final AVRInstrBuilder ELPM = add("elpm", new ELPM_builder());
    public static final AVRInstrBuilder LPM = add("lpm", new LPM_builder());
    public static final AVRInstrBuilder LD = add("ld", new LD_builder());
    public static final AVRInstrBuilder ST = add("st", new ST_builder());
    public static int checkValue(int val, int low, int high) {
        if ( val < low || val > high ) {
            throw new Error();
        }
        return val;
    }
}
