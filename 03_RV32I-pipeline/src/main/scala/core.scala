package core_tile

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import Assignment02.{ALU, ALUOp}
import uopc._
import RV32I._

class PipelinedRV32Icore(BinaryFile: String, useBTB: Boolean = true) extends Module {
  val io = IO(new Bundle {
    val result    = Output(UInt(32.W))
    val exception = Output(Bool())

    // Performance statistics (Task 4.4)
    val perf_cycles         = Output(UInt(32.W))
    val perf_instructions   = Output(UInt(32.W))
    val perf_branches       = Output(UInt(32.W))
    val perf_branch_correct = Output(UInt(32.W))
    val perf_btb_accuracy   = Output(UInt(32.W))
  })

  // ================================================================
  // Instruction Memory
  // ================================================================
  val imem = Mem(4096, UInt(32.W))
  loadMemoryFromFile(imem, BinaryFile)

  val pc = RegInit(0.U(32.W))

  // ================================================================
  // Submodules
  // ================================================================
  val rf             = Module(new regFile())
  val ifStage        = Module(new IFstage())
  val ifBarrier      = Module(new IFbarrier())
  val idStage        = Module(new IDstage())
  val idBarrier      = Module(new IDbarrier())
  val exStage        = Module(new EXstage())
  val exBarrier      = Module(new EXbarrier())
  val memStage       = Module(new MEM())
  val memBarrier     = Module(new MEMbarrier())
  val wbStage        = Module(new WBstage())
  val wbBarrier      = Module(new WBbarrier())
  val forwardingUnit = Module(new ForwardingUnit())
  val btb            = Module(new BTB())

  // ================================================================
  // Performance Counters
  // ================================================================
  val cycle_count       = RegInit(0.U(32.W))
  val instr_count       = RegInit(0.U(32.W))
  val branch_count      = RegInit(0.U(32.W))
  val branch_correct    = RegInit(0.U(32.W))
  val branch_mispredict = RegInit(0.U(32.W))

  // Cycle counter always increments
  cycle_count := cycle_count + 1.U

  // Count non-exception WB commits (but not x0 writes which are NOPs)
  when(!wbBarrier.io.outException && (wbBarrier.io.outRD =/= 0.U)) {
    instr_count := instr_count + 1.U
  }

  // ================================================================
  // Control Hazard Wires
  // ================================================================
  val flush         = Wire(Bool())
  val branchTaken   = Wire(Bool())
  val branchTarget  = Wire(UInt(32.W))
  val jumpTaken     = Wire(Bool())
  val jumpTarget    = Wire(UInt(32.W))
  val controlHazard = Wire(Bool())
  val newPC         = Wire(UInt(32.W))

  // BTB speculation wires
  val use_btb_prediction   = Wire(Bool())
  val btb_predicted_target = Wire(UInt(32.W))

  // ================================================================
  // BTB Lookup — at IF stage using current PC
  // Only for conditional branches (JAL/JALR bypass BTB per spec)
  // ================================================================
  btb.io.PC            := pc
  btb_predicted_target := btb.io.target

  // useBTB is a Scala Boolean — must use if/else, NOT .B
  use_btb_prediction := {
    if (useBTB) btb.io.valid && btb.io.predictTaken
    else false.B
  }

  // ================================================================
  // IF STAGE
  // ================================================================
  ifStage.io.inPC    := pc
  ifStage.io.inInstr := imem(pc(13, 2))

  ifBarrier.io.inInstr := ifStage.io.outInstr
  ifBarrier.io.inPC    := ifStage.io.outPC
  ifBarrier.io.flush   := flush

  // ================================================================
  // ID STAGE
  // ================================================================
  idStage.io.inInstr := ifBarrier.io.outInstr
  idStage.io.inPC    := ifBarrier.io.outPC

  rf.io.req_1.addr            := idStage.io.rfReadReq1.addr
  rf.io.req_2.addr            := idStage.io.rfReadReq2.addr
  idStage.io.rfReadResp1.data := rf.io.resp_1.data
  idStage.io.rfReadResp2.data := rf.io.resp_2.data

  idBarrier.io.inUOP          := idStage.io.outUOP
  idBarrier.io.inRD           := idStage.io.outRD
  idBarrier.io.inOperandA     := idStage.io.outOperandA
  idBarrier.io.inOperandB     := idStage.io.outOperandB
  idBarrier.io.inXcptInvalid  := idStage.io.outXcptInvalid
  idBarrier.io.inRS1          := idStage.io.outRS1
  idBarrier.io.inRS2          := idStage.io.outRS2
  idBarrier.io.inUseRS2       := idStage.io.outUseRS2
  idBarrier.io.inALUOp        := idStage.io.outALUOp
  idBarrier.io.inBranchOp     := idStage.io.outBranchOp
  idBarrier.io.inBranchOffset := idStage.io.outBranchOffset
  idBarrier.io.inPC           := idStage.io.outPC
  idBarrier.io.inLinkAddr     := idStage.io.outLinkAddr
  idBarrier.io.flush          := flush

  // CORRECT BTB PREDICTION PIPELINE ALIGNMENT:
  // Cycle N:   pc=BADDR, btb.io.predictTaken=P_BADDR (combinational)
  //            ifBarrier latches: branch instruction + BADDR
  // Cycle N+1: pc=BADDR+4, btb.io.predictTaken=P_(BADDR+4)  WRONG if used directly
  //            idBarrier needs P_BADDR
  // Fix: register btb.io.predictTaken by one cycle, cleared on flush
  val btbPredReg = RegInit(false.B)
  btbPredReg := Mux(flush, false.B, btb.io.predictTaken)
  idBarrier.io.inBTBPrediction := btbPredReg

  // ================================================================
  // FORWARDING UNIT
  // ================================================================
  forwardingUnit.io.rs1_ex := idBarrier.io.outRS1
  forwardingUnit.io.rs2_ex := idBarrier.io.outRS2
  forwardingUnit.io.useRS2 := idBarrier.io.outUseRS2

  forwardingUnit.io.rd_mem  := exBarrier.io.outRD
  forwardingUnit.io.rd_wb1  := memBarrier.io.outRD
  forwardingUnit.io.rd_wb2  := wbBarrier.io.outRD

  forwardingUnit.io.wr_en_mem := (exBarrier.io.outRD =/= 0.U) && !exBarrier.io.outXcpt
  forwardingUnit.io.wr_en_wb1 := (memBarrier.io.outRD =/= 0.U) && !memBarrier.io.outException
  forwardingUnit.io.wr_en_wb2 := (wbBarrier.io.outRD =/= 0.U) && !wbBarrier.io.outException

  forwardingUnit.io.data_mem := exBarrier.io.outAluValue
  forwardingUnit.io.data_wb1 := memBarrier.io.outAluResult
  forwardingUnit.io.data_wb2 := wbBarrier.io.outAluResult

  forwardingUnit.io.operandA_orig := idBarrier.io.outOperandA
  forwardingUnit.io.operandB_orig := idBarrier.io.outOperandB

  val exOpA = forwardingUnit.io.operandA_fwd
  val exOpB = forwardingUnit.io.operandB_fwd

  // ================================================================
  // EX STAGE
  // ================================================================
  exStage.io.inUOP          := idBarrier.io.outUOP
  exStage.io.inOperandA     := exOpA
  exStage.io.inOperandB     := exOpB
  exStage.io.inALUOp        := idBarrier.io.outALUOp
  exStage.io.inBranchOp     := idBarrier.io.outBranchOp
  exStage.io.inPC           := idBarrier.io.outPC
  exStage.io.inBranchOffset := idBarrier.io.outBranchOffset

  exBarrier.io.inUOP           := exStage.io.outUOP
  exBarrier.io.inRD            := idBarrier.io.outRD
  exBarrier.io.inAluValue      := exStage.io.outAluResult
  exBarrier.io.inXcpt          := idBarrier.io.outXcptInvalid
  exBarrier.io.inBranchTaken   := exStage.io.outBranchTaken
  exBarrier.io.inBranchTarget  := exStage.io.outBranchTarget
  exBarrier.io.inLinkAddr      := idBarrier.io.outLinkAddr
  exBarrier.io.inPC            := idBarrier.io.outPC

  // Carry the BTB prediction that was made when THIS instruction was in IF
  exBarrier.io.inBTBPrediction := idBarrier.io.outBTBPrediction

  // ================================================================
  // BRANCH / JUMP DETECTION
  // Decisions are made once instruction has passed through EX stage
  // ================================================================
  val isBranch = exBarrier.io.outUOP === uopc.BRANCH
  val isJAL    = exBarrier.io.outUOP === uopc.JAL
  val isJALR   = exBarrier.io.outUOP === uopc.JALR

  branchTaken  := isBranch && exBarrier.io.outBranchTaken
  branchTarget := exBarrier.io.outBranchTarget

  jumpTaken  := isJAL || isJALR
  jumpTarget := exBarrier.io.outBranchTarget

  controlHazard := branchTaken || jumpTaken
  flush         := controlHazard

  // ================================================================
  // BTB UPDATE
  // KEY FIX: Use exBarrier.io.outBTBPrediction (the prediction that was
  // made for THIS branch when it was in IF), NOT btb.io.predictTaken
  // (which is a live lookup for the current PC — completely different!).
  // ================================================================
  val mispredicted = isBranch && (branchTaken =/= exBarrier.io.outBTBPrediction)

  btb.io.update       := isBranch
  btb.io.updatePC     := exBarrier.io.outPC       // branch's actual PC
  btb.io.updateTarget := exBarrier.io.outBranchTarget
  btb.io.mispredicted := mispredicted
  btb.io.actualTaken  := branchTaken

  // ================================================================
  // BRANCH ACCURACY TRACKING
  // ================================================================
  when(isBranch) {
    branch_count := branch_count + 1.U
    when(!mispredicted) {
      branch_correct    := branch_correct + 1.U
    }.otherwise {
      branch_mispredict := branch_mispredict + 1.U
    }
  }

  // ================================================================
  // PC UPDATE LOGIC
  // Priority: actual resolution (control hazard) > BTB speculation > PC+4
  // ================================================================
  when(controlHazard) {
    newPC := Mux(branchTaken, branchTarget, jumpTarget)
  }.elsewhen(use_btb_prediction) {
    newPC := btb_predicted_target
  }.otherwise {
    newPC := pc + 4.U
  }

  pc := newPC

  // ================================================================
  // MEM STAGE — no memory operations in this implementation
  // ================================================================
  memStage.io := DontCare

  // For JAL/JALR: write link address (PC+4) to rd; otherwise ALU result
  val memResult = Mux(
    (exBarrier.io.outUOP === uopc.JAL) || (exBarrier.io.outUOP === uopc.JALR),
    exBarrier.io.outLinkAddr,
    exBarrier.io.outAluValue
  )

  memBarrier.io.inAluResult := memResult
  memBarrier.io.inRD        := exBarrier.io.outRD
  memBarrier.io.inException := exBarrier.io.outXcpt

  // ================================================================
  // WB STAGE
  // ================================================================
  wbStage.io.inAluResult := memBarrier.io.outAluResult
  wbStage.io.inRD        := memBarrier.io.outRD
  rf.io.req_3 <> wbStage.io.regFileReq

  wbBarrier.io.inAluResult := wbStage.io.check_res
  wbBarrier.io.inRD        := memBarrier.io.outRD
  wbBarrier.io.inException := memBarrier.io.outException

  // ================================================================
  // PRIMARY OUTPUTS
  // ================================================================
  io.result    := wbBarrier.io.outAluResult
  io.exception := wbBarrier.io.outException

  io.perf_cycles         := cycle_count
  io.perf_instructions   := instr_count
  io.perf_branches       := branch_count
  io.perf_branch_correct := branch_correct
  io.perf_btb_accuracy   := Mux(
    branch_count > 0.U,
    (branch_correct * 100.U) / branch_count,
    0.U
  )

  // ================================================================
  // DEBUG PRINTF
  // ================================================================
  when(controlHazard) {
    printf(p"[CORE] Control hazard! newPC=0x${Hexadecimal(newPC)} " +
           p"branchTaken=${branchTaken} jumpTaken=${jumpTaken}\n")
  }

  when(isBranch && mispredicted) {
    printf(p"[CORE] BTB misprediction! PC=0x${Hexadecimal(exBarrier.io.outPC)} " +
           p"actual=${branchTaken} predicted=${exBarrier.io.outBTBPrediction}\n")
  }

  when(isBranch && !mispredicted && (if (useBTB) true.B else false.B)) {
    printf(p"[BTB] Correct prediction! PC=0x${Hexadecimal(exBarrier.io.outPC)} " +
           p"predicted=${exBarrier.io.outBTBPrediction} actual=${branchTaken}\n")
  }
}