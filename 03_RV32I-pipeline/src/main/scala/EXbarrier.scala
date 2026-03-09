package core_tile

import chisel3._
import uopc._

class EXbarrier extends Module {
  val io = IO(new Bundle {
    // Standard datapath signals
    val inUOP      = Input(uopc())
    val inRD       = Input(UInt(5.W))
    val inAluValue = Input(UInt(32.W))
    val inXcpt     = Input(Bool())

    // Branch/Jump signals
    val inBranchTaken  = Input(Bool())
    val inBranchTarget = Input(UInt(32.W))
    val inLinkAddr     = Input(UInt(32.W))

    // PC of the branch instruction — carried for correct BTB update
    val inPC = Input(UInt(32.W))

    // BTB prediction carried from ID stage — used for misprediction check
    val inBTBPrediction = Input(Bool())

    // Outputs
    val outUOP          = Output(uopc())
    val outRD           = Output(UInt(5.W))
    val outAluValue     = Output(UInt(32.W))
    val outXcpt         = Output(Bool())
    val outBranchTaken  = Output(Bool())
    val outBranchTarget = Output(UInt(32.W))
    val outLinkAddr     = Output(UInt(32.W))
    val outPC           = Output(UInt(32.W))
    val outBTBPrediction = Output(Bool())
  })

  // ── Pipeline Registers ───────────────────────────────────────────────
  val uopReg          = RegInit(uopc.NOP)
  val rdReg           = RegInit(0.U(5.W))
  val aluReg          = RegInit(0.U(32.W))
  val excReg          = RegInit(false.B)
  val branchTakenReg  = RegInit(false.B)
  val branchTargetReg = RegInit(0.U(32.W))
  val linkAddrReg     = RegInit(0.U(32.W))
  val pcReg           = RegInit(0.U(32.W))
  val btbPredReg      = RegInit(false.B)

  // ── Register Updates ─────────────────────────────────────────────────
  uopReg          := io.inUOP
  rdReg           := io.inRD
  aluReg          := io.inAluValue
  excReg          := io.inXcpt
  branchTakenReg  := io.inBranchTaken
  branchTargetReg := io.inBranchTarget
  linkAddrReg     := io.inLinkAddr
  pcReg           := io.inPC
  btbPredReg      := io.inBTBPrediction

  // ── Output Assignments ───────────────────────────────────────────────
  io.outUOP           := uopReg
  io.outRD            := rdReg
  io.outAluValue      := aluReg
  io.outXcpt          := excReg
  io.outBranchTaken   := branchTakenReg
  io.outBranchTarget  := branchTargetReg
  io.outLinkAddr      := linkAddrReg
  io.outPC            := pcReg
  io.outBTBPrediction := btbPredReg
}