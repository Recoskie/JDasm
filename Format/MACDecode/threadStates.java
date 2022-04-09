package Format.MACDecode;

public class threadStates
{
  //Processor-specific save state command. Allows the program to resume execution at any part in the code by setting the value of each register.
  //Note the smaller version of this command 0x28 which defines that starting point of the program should be used instead.

  public static final String[][] x86regs = new String[][]
  {
    new String[]
    {
      "x86_THREAD_STATE32",
      "EAX", "EBX", "ECX", "EDX", "EDI", "ESI", "EBP", "ESP", "SS", "EFLAGS", "EIP", "CS", "DS", "ES", "FS", "GS"
    },
    new String[]
    {
      "x86_FLOAT_STATE32",
      "Reserved", "Reserved", "Control", "Status", "FTW", "Reserved", "FOP", "IP",
	  "CS", "Reserved", "DP", "DS", "Reserved", "MXCSR", "MXCSRMASK",
      "MM0/ST0", "Reserved", "MM1/ST1", "Reserved", "MM2/ST2", "Reserved", "MM3/ST3", "Reserved",
      "MM4/ST4", "Reserved", "MM5/ST5", "Reserved", "MM6/ST6", "Reserved", "MM7/ST7", "Reserved",
      "XMM0", "XMM1", "XMM2", "XMM3", "XMM4", "XMM5", "XMM6", "XMM7",
      "Reserved", "Reserved"
    },
    new String[] { "x86_EXCEPTION_STATE32", "TRAPNO", "CPU", "ERR", "FAULTVADDR" },
    new String[]
    {
      "x86_THREAD_STATE64",
      "RAX", "RBX", "RCX", "RDX", "RDI", "RSI", "RBP", "RSP", "R8",
      "R9", "R10", "R11", "R12", "R13", "R14", "R15", "RIP", "RFLAGS",
      "CS", "FS", "GS"
    },
    new String[]
    {
      "x86_FLOAT_STATE64",
      "Reserved", "Reserved", "Control", "Status", "FTW", "Reserved", "FOP", "IP",
	  "CS", "Reserved", "DP", "DS", "Reserved", "MXCSR", "MXCSRMASK",
      "MM0/ST0", "Reserved", "MM1/ST1", "Reserved", "MM2/ST2", "Reserved", "MM3/ST3", "Reserved",
      "MM4/ST4", "Reserved", "MM5/ST5", "Reserved", "MM6/ST6", "Reserved", "MM7/ST7", "Reserved",
      "XMM0", "XMM1", "XMM2", "XMM3", "XMM4", "XMM5", "XMM6", "XMM7",
      "XMM8", "XMM9", "XMM10", "XMM11", "XMM12", "XMM13", "XMM14", "XMM15",
      "Reserved", "Reserved"
    },
    new String[] { "x86_EXCEPTION_STATE64", "TRAPNO", "CPU", "ERR", "FAULTVADDR" },
    new String[] { "x86_THREAD_STATE" },
    new String[] { "x86_FLOAT_STATE" },
    new String[] { "x86_EXCEPTION_STATE" },
    new String[] { "x86_DEBUG_STATE32", "DR0", "DR1", "DR2", "DR3", "DR4", "DR5", "DR6", "DR7" },
    new String[] { "x86_DEBUG_STATE64", "DR0", "DR1", "DR2", "DR3", "DR4", "DR5", "DR6", "DR7" },
    new String[] { "x86_DEBUG_STATE" },
    new String[] { "THREAD_STATE_NONE" }, new String[] { "x86_SAVED_STATE32" }, new String[] { "x86_SAVED_STATE64" },
    new String[]
    {
      "x86_AVX_STATE32",
      "Reserved", "Reserved", "Control", "Status", "FTW", "Reserved", "FOP", "IP",
      "CS", "Reserved", "DP", "DS", "Reserved", "MXCSR", "MXCSRMASK",
      "MM0/ST0", "Reserved", "MM1/ST1", "Reserved", "MM2/ST2", "Reserved", "MM3/ST3", "Reserved",
      "MM4/ST4", "Reserved", "MM5/ST5", "Reserved", "MM6/ST6", "Reserved", "MM7/ST7", "Reserved",
      "XMM0", "XMM1", "XMM2", "XMM3", "XMM4", "XMM5", "XMM6", "XMM7",
      "Reserved", "Reserved", "Reserved",
      "YMMH0", "YMMH1", "YMMH2", "YMMH3", "YMMH4", "YMMH5", "YMMH6", "YMMH7"
    },
    new String[]
    {
      "x86_AVX_STATE64",
      "Reserved", "Reserved", "Control", "Status", "FTW", "Reserved", "FOP", "IP",
      "CS", "Reserved", "DP", "DS", "Reserved", "MXCSR", "MXCSRMASK",
      "MM0/ST0", "Reserved", "MM1/ST1", "Reserved", "MM2/ST2", "Reserved", "MM3/ST3", "Reserved",
      "MM4/ST4", "Reserved", "MM5/ST5", "Reserved", "MM6/ST6", "Reserved", "MM7/ST7", "Reserved",
      "XMM0", "XMM1", "XMM2", "XMM3", "XMM4", "XMM5", "XMM6", "XMM7",
      "XMM8", "XMM9", "XMM10", "XMM11", "XMM12", "XMM13", "XMM14", "XMM15",
      "Reserved", "Reserved", "Reserved",
      "YMMH0", "YMMH1", "YMMH2", "YMMH3", "YMMH4", "YMMH5", "YMMH6", "YMMH7",
      "YMMH8", "YMMH9", "YMMH10", "YMMH11", "YMMH12", "YMMH13", "YMMH14", "YMMH15"
    },
    new String[] { "x86_AVX_STATE" },
    new String[]
    {
      "x86_AVX512_STATE32",
      "Reserved", "Reserved", "Control", "Status", "FTW", "Reserved", "FOP", "IP",
      "CS", "Reserved", "DP", "DS", "Reserved", "MXCSR", "MXCSRMASK",
      "MM0/ST0", "Reserved", "MM1/ST1", "Reserved", "MM2/ST2", "Reserved", "MM3/ST3", "Reserved",
      "MM4/ST4", "Reserved", "MM5/ST5", "Reserved", "MM6/ST6", "Reserved", "MM7/ST7", "Reserved",
      "XMM0", "XMM1", "XMM2", "XMM3", "XMM4", "XMM5", "XMM6", "XMM7",
      "Reserved", "Reserved", "Reserved",
      "YMMH0", "YMMH1", "YMMH2", "YMMH3", "YMMH4", "YMMH5", "YMMH6", "YMMH7",
      "K0", "K1", "K2", "K3", "K4", "K5", "K6", "K7",
      "ZMMH0", "ZMMH1", "ZMMH2", "ZMMH3", "ZMMH4", "ZMMH5", "ZMMH6", "ZMMH7",
    },
    new String[]
    {
      "x86_AVX512_STATE64",
      "Reserved", "Reserved", "Control", "Status", "FTW", "Reserved", "FOP", "IP",
      "CS", "Reserved", "DP", "DS", "Reserved", "MXCSR", "MXCSRMASK",
      "MM0/ST0", "Reserved", "MM1/ST1", "Reserved", "MM2/ST2", "Reserved", "MM3/ST3", "Reserved",
      "MM4/ST4", "Reserved", "MM5/ST5", "Reserved", "MM6/ST6", "Reserved", "MM7/ST7", "Reserved",
      "XMM0", "XMM1", "XMM2", "XMM3", "XMM4", "XMM5", "XMM6", "XMM7",
      "XMM8", "XMM9", "XMM10", "XMM11", "XMM12", "XMM13", "XMM14", "XMM15",
      "Reserved", "Reserved", "Reserved",
      "YMMH0", "YMMH1", "YMMH2", "YMMH3", "YMMH4", "YMMH5", "YMMH6", "YMMH7",
      "YMMH8", "YMMH9", "YMMH10", "YMMH11", "YMMH12", "YMMH13", "YMMH14", "YMMH15",
      "K0", "K1", "K2", "K3", "K4", "K5", "K6", "K7",
      "ZMMH0", "ZMMH1", "ZMMH2", "ZMMH3", "ZMMH4", "ZMMH5", "ZMMH6", "ZMMH7",
      "ZMMH8", "ZMMH9", "ZMMH10", "ZMMH11", "ZMMH12", "ZMMH13", "ZMMH14", "ZMMH15",
      "ZMM16", "ZMM17", "ZMM18", "ZMM19", "ZMM20", "ZMM21", "ZMM22", "ZMM23",
      "ZMM24", "ZMM25", "ZMM26", "ZMM27", "ZMM28", "ZMM29", "ZMM30", "ZMM31",
    },
    new String[] { "x86_AVX512_STATE" },
    new String[] { "x86_PAGEIN_STATE", "PAGEIN ERROR" },
    new String[]
    {
      "x86_THREAD_FULL_STATE64",
      "RAX", "RBX", "RCX", "RDX", "RDI", "RSI", "RBP", "RSP", "R8",
      "R9", "R10", "R11", "R12", "R13", "R14", "R15", "RIP", "RFLAGS",
      "CS", "FS", "GS", "DS", "ES", "SS", "GS_BASE"
    },
    new String[]
    {
      "x86_INSTRUCTION_STATE",
      "VALID BYTES", "OFFSET", "OUT OF SYNC"
    },
    new String[] { "x86_LAST_BRANCH_STATE" }
  };

  public static final int[][] x86size = new int[][]
  {
    new int[] { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, -4, 4, 4, 4, 4, 4 },
    new int[]
    {
      4, 4, 4, 2, 2, 1, 1, 2, -4, 2, 2, 4, 2, 2, 4, 4,
      10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6,
      16, 16, 16, 16, 16, 16, 16, 16,
      224, 4
    },
    new int[] { 4, 2, 2, 4, 4 },
    new int[] { 4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, -8, 8, 8, 8, 8 },
    new int[]
    {
      4, 4, 4, 2, 2, 1, 1, 2, -4, 2, 2, 4, 2, 2, 4, 4,
      10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6,
      16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
      96, 4
    },
    new int[] { 4, 2, 2, 4, 8 },
    new int[] { 4, 0, 3 }, //32=0, 64=3
    new int[] { 4, 1, 4 }, //32=1, 64=4
    new int[] { 4, 2, 5 }, //32=2, 64=5
    new int[] { 4, 4, 4, 4, 4, 4, 4, 4, 4 },
    new int[] { 4, 8, 8, 8, 8, 8, 8, 8, 8 },
    new int[] { 4 }, //32=9, 64=10
    new int[] { 4 }, new int[] { 4 }, new int[] { 4 },
    new int[]
    {
      4, 4, 4, 2, 2, 1, 1, 2, -4, 2, 2, 4, 2, 2, 4, 4,
      10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6,
      16, 16, 16, 16, 16, 16, 16, 16,
      224, 4, 64,
      16, 16, 16, 16, 16, 16, 16, 16
    },
    new int[]
    {
      4, 4, 4, 2, 2, 1, 1, 2, -4, 2, 2, 4, 2, 2, 4, 4,
      10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6,
      16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
      96, 4, 64,
      16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16
    },
    new int[] { 4, 15, 16 }, //32=15, 64=16
    new int[]
    {
      4, 4, 4, 2, 2, 1, 1, 2, -4, 2, 2, 4, 2, 2, 4, 4,
      10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6,
      16, 16, 16, 16, 16, 16, 16, 16,
      224, 4, 64,
      16, 16, 16, 16, 16, 16, 16, 16,
      8, 8, 8, 8, 8, 8, 8, 8,
      32, 32, 32, 32 ,32 ,32 ,32 ,32
    },
    new int[]
    {
      4, 4, 4, 2, 2, 1, 1, 2, -4, 2, 2, 4, 2, 2, 4, 4,
      10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6, 10, 6,
      16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
      96, 4, 64,
      16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
      8, 8, 8, 8, 8, 8, 8, 8,
      32, 32, 32, 32 ,32 ,32 ,32 ,32, 32, 32, 32, 32 ,32 ,32 ,32 ,32,
      64, 64, 64, 64 ,64 ,64 ,64 ,64, 64, 64, 64, 64 ,64 ,64 ,64 ,64
    },
    new int[] { 4, 18, 19 }, //32=18, 64=19
    new int[] { 4, 4 },
    new int[] { 4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, -8, 8, 8, 8, 8, 8, 8, 8, 8 },
    new int[] { 4, 4, 4, 4 },
    new int[] { 4 }
  };

  //The ARM processor core registers and save states.

  public static final String[][] ARMregs = new String[][]
  {
    new String[]
    {
      "ARM_THREAD_STATE",
      "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "SP", "LR", "PC", "CPSR"
    },
    new String[]
    {
      "ARM_VFP_STATE",
      "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15",
      "R16", "R17", "R18", "R19", "R20", "R21", "R22", "R23", "R24", "R25", "R26", "R27", "R28", "R29", "R30", "R31",
      "R32", "R33", "R34", "R35", "R36", "R37", "R38", "R39", "R40", "R41", "R42", "R43", "R44", "R45", "R46", "R47",
      "R48", "R49", "R50", "R51", "R52", "R53", "R54", "R55", "R56", "R57", "R58", "R59", "R60", "R61", "R62", "R63",
      "FPSCR"
    },
    new String[] { "ARM_EXCEPTION_STATE", "EXCEPTION", "FSR", "FAR" },
    new String[]
    {
      "ARM_DEBUG_STATE",
      "BVR0", "BVR1", "BVR2", "BVR3", "BVR4", "BVR5", "BVR6", "BVR7", "BVR8", "BVR9", "BVR10", "BVR11", "BVR12", "BVR13", "BVR14", "BVR15",
      "BCR0", "BCR1", "BCR2", "BCR3", "BCR4", "BCR5", "BCR6", "BCR7", "BCR8", "BCR9", "BCR10", "BCR11", "BCR12", "BCR13", "BCR14", "BCR15",
      "WVR0", "WVR1", "WVR2", "WVR3", "WVR4", "WVR5", "WVR6", "WVR7", "WVR8", "WVR9", "WVR10", "WVR11", "WVR12", "WVR13", "WVR14", "WVR15",
      "WCR0", "WCR1", "WCR2", "WCR3", "WCR4", "WCR5", "WCR6", "WCR7", "WCR8", "WCR9", "WCR10", "WCR11", "WCR12", "WCR13", "WCR14", "WCR15"
    },
    new String[] { "THREAD_STATE_NONE" },
    new String[]
    {
      "ARM_THREAD_STATE64",
      "X0", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "X8", "X9", "X10", "X11", "X12", "X13", "X14", "X15",
      "X16", "X17", "X18", "X19", "X20", "X21", "X22", "X23", "X24", "X25", "X26", "X27", "X28",
      "FP", "LR", "SP", "PC", "CPSR", "FLAGS"
    },
    new String[] { "ARM_EXCEPTION_STATE64", "FAR", "ESR", "EXCEPTION" },
    new String[] { "ARM_THREAD_STATE_LAST" },
    new String[]
    {
      "ARM_THREAD_STATE32",
      "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "SP", "LR", "PC", "CPSR"
    },
    new String[] { "BAD Thread Type" }, new String[] { "BAD Thread Type" }, new String[] { "BAD Thread Type" },
    new String[] { "X86_THREAD_STATE_NONE" },
    new String[]
    {
      "ARM_DEBUG_STATE32",
      "BVR0", "BVR1", "BVR2", "BVR3", "BVR4", "BVR5", "BVR6", "BVR7", "BVR8", "BVR9", "BVR10", "BVR11", "BVR12", "BVR13", "BVR14", "BVR15",
      "BCR0", "BCR1", "BCR2", "BCR3", "BCR4", "BCR5", "BCR6", "BCR7", "BCR8", "BCR9", "BCR10", "BCR11", "BCR12", "BCR13", "BCR14", "BCR15",
      "WVR0", "WVR1", "WVR2", "WVR3", "WVR4", "WVR5", "WVR6", "WVR7", "WVR8", "WVR9", "WVR10", "WVR11", "WVR12", "WVR13", "WVR14", "WVR15",
      "WCR0", "WCR1", "WCR2", "WCR3", "WCR4", "WCR5", "WCR6", "WCR7", "WCR8", "WCR9", "WCR10", "WCR11", "WCR12", "WCR13", "WCR14", "WCR15",
      "MDSCR"
    },
    new String[]
    {
      "ARM_DEBUG_STATE64",
      "BVR0", "BVR1", "BVR2", "BVR3", "BVR4", "BVR5", "BVR6", "BVR7", "BVR8", "BVR9", "BVR10", "BVR11", "BVR12", "BVR13", "BVR14", "BVR15",
      "BCR0", "BCR1", "BCR2", "BCR3", "BCR4", "BCR5", "BCR6", "BCR7", "BCR8", "BCR9", "BCR10", "BCR11", "BCR12", "BCR13", "BCR14", "BCR15",
      "WVR0", "WVR1", "WVR2", "WVR3", "WVR4", "WVR5", "WVR6", "WVR7", "WVR8", "WVR9", "WVR10", "WVR11", "WVR12", "WVR13", "WVR14", "WVR15",
      "WCR0", "WCR1", "WCR2", "WCR3", "WCR4", "WCR5", "WCR6", "WCR7", "WCR8", "WCR9", "WCR10", "WCR11", "WCR12", "WCR13", "WCR14", "WCR15",
      "MDSCR"
    },
    new String[]
    {
      "ARM_NEON_STATE",
      "V0", "V1", "V2", "V3", "V4", "V5", "V6", "V7", "FPSR", "FPCR"
    },
    new String[]
    {
      "ARM_NEON_STATE64",
      "V0", "V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8", "V9", "V10", "V11", "V12", "V13", "V14", "V15", "FPSR", "FPCR"
    },
    new String[]
    {
      "ARM_CPMU_STATE64",
      "CTRS0", "CTRS1", "CTRS2", "CTRS3", "CTRS4", "CTRS5", "CTRS6", "CTRS7", "CTRS8", "CTRS9", "CTRS10", "CTRS11", "CTRS12", "CTRS13", "CTRS14", "CTRS15"
    },
    new String[] { "BAD Thread Type" },
    new String[] { "ARM_SAVED_STATE32" }, new String[] { "ARM_SAVED_STATE64" },
    new String[] { "ARM_NEON_SAVED_STATE32" }, new String[] { "ARM_NEON_SAVED_STATE64" },
    new String[] { "BAD Thread Type" }, new String[] { "BAD Thread Type" }, new String[] { "BAD Thread Type" },
    new String[]
    {
      "ARM_PAGEIN_STATE", "PAGEIN ERROR"
    }
  };

  public static final int[][] ARMsize = new int[][]
  {
    new int[] { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, -4, 4 },
    new int[]
    {
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    },
    new int[] { 4, 4, 4, 4 },
    new int[]
    {
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    },
    new int[] { 4 },
    new int[]
    {
      4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
      8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, -8,
      4, 4
    },
    new int[] { 4, 8, 4, 4 },
    new int[] { 4 },
    new int[] { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, -4, 4 },
    new int[] { 4 }, new int[] { 4 }, new int[] { 4 },
    new int[] { 4 },
    new int[]
    {
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 8
    },
    new int[]
    {
      4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
      8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
      8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
      8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8
    },
    new int[] { 4, 16, 16, 16, 16, 16, 16, 16, 16, 4, 4 },
    new int[] { 4, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 4, 4 },
    new int[] { 4, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 },
    new int[] { 4 },
    new int[] { 4 }, new int[] { 4 }, new int[] { 4 }, new int[] { 4 },
    new int[] { 4 }, new int[] { 4 }, new int[] { 4 },
    new int[] { 4, 4 }
  };
}
