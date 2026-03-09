package core_tile

import chisel3._
import uopc._
import Assignment02.ALUOp
import RV32I._

class IDbarrier extends Module {
  val io = IO(new Bundle {
    // Control
    val flush = Input(Bool())

    // ALU signals
    val inUOP          = Input(uopc())
    val inRD           = Input(UInt(5.W))
    val inOperandA     = Input(UInt(32.W))
    val inOperandB     = Input(UInt(32.W))
    val inXcptInvalid  = Input(Bool())
    val inRS1          = Input(UInt(5.W))
    val inRS2          = Input(UInt(5.W))
    val inUseRS2       = Input(Bool())
    val inALUOp        = Input(ALUOp())

    // Branch/Jump signals
    val inBranchOp     = Input(BranchOp())
    val inBranchOffset = Input(UInt(32.W))
    val inPC           = Input(UInt(32.W))
    val inLinkAddr     = Input(UInt(32.W))

    // BTB prediction captured at IF stage — carried forward with instruction
    val inBTBPrediction = Input(Bool())

    // Outputs
    val outUOP          = Output(uopc())
    val outRD           = Output(UInt(5.W))
    val outOperandA     = Output(UInt(32.W))
    val outOperandB     = Output(UInt(32.W))
    val outXcptInvalid  = Output(Bool())
    val outRS1          = Output(UInt(5.W))
    val outRS2          = Output(UInt(5.W))
    val outUseRS2       = Output(Bool())
    val outALUOp        = Output(ALUOp())
    val outBranchOp     = Output(BranchOp())
    val outBranchOffset = Output(UInt(32.W))
    val outPC           = Output(UInt(32.W))
    val outLinkAddr     = Output(UInt(32.W))
    val outBTBPrediction = Output(Bool())
  })

  // ── Pipeline Registers ───────────────────────────────────────────────
  val uopReg          = RegInit(uopc.NOP)
  val rdReg           = RegInit(0.U(5.W))
  val opAReg          = RegInit(0.U(32.W))
  val opBReg          = RegInit(0.U(32.W))
  val xcptReg         = RegInit(false.B)
  val rs1Reg          = RegInit(0.U(5.W))
  val rs2Reg          = RegInit(0.U(5.W))
  val useRS2Reg       = RegInit(false.B)
  val aluOpReg        = RegInit(ALUOp.ADD)
  val branchOpReg     = RegInit(BranchOp.BEQ)   // BEQ=0 is safe reset; no NOP in this enum
  val branchOffsetReg = RegInit(0.U(32.W))
  val pcReg           = RegInit(0.U(32.W))
  val linkAddrReg     = RegInit(0.U(32.W))
  val btbPredReg      = RegInit(false.B)         // NEW: carry BTB prediction

  // ── Register Updates ─────────────────────────────────────────────────
  when(io.flush) {
    // Insert NOP bubble on control hazard
    uopReg          := uopc.NOP
    rdReg           := 0.U
    opAReg          := 0.U
    opBReg          := 0.U
    xcptReg         := false.B
    rs1Reg          := 0.U
    rs2Reg          := 0.U
    useRS2Reg       := false.B
    aluOpReg        := ALUOp.ADD
    branchOpReg     := BranchOp.BEQ
    branchOffsetReg := 0.U
    pcReg           := 0.U
    linkAddrReg     := 0.U
    btbPredReg      := false.B
  }.otherwise {
    uopReg          := io.inUOP
    rdReg           := io.inRD
    opAReg          := io.inOperandA
    opBReg          := io.inOperandB
    xcptReg         := io.inXcptInvalid
    rs1Reg          := io.inRS1
    rs2Reg          := io.inRS2
    useRS2Reg       := io.inUseRS2
    aluOpReg        := io.inALUOp
    branchOpReg     := io.inBranchOp
    branchOffsetReg := io.inBranchOffset
    pcReg           := io.inPC
    linkAddrReg     := io.inLinkAddr
    btbPredReg      := io.inBTBPrediction
  }

  // ── Output Assignments ───────────────────────────────────────────────
  io.outUOP           := uopReg
  io.outRD            := rdReg
  io.outOperandA      := opAReg
  io.outOperandB      := opBReg
  io.outXcptInvalid   := xcptReg
  io.outRS1           := rs1Reg
  io.outRS2           := rs2Reg
  io.outUseRS2        := useRS2Reg
  io.outALUOp         := aluOpReg
  io.outBranchOp      := branchOpReg
  io.outBranchOffset  := branchOffsetReg
  io.outPC            := pcReg
  io.outLinkAddr      := linkAddrReg
  io.outBTBPrediction := btbPredReg
}