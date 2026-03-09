package core_tile

import chisel3._
import chisel3.util._
import uopc._
import Assignment02.ALUOp
import RV32I._

class IDstage extends Module {
  val io = IO(new Bundle {
    val inInstr = Input(UInt(32.W))
    val inPC    = Input(UInt(32.W))

    val rfReadReq1  = Output(new regFileReadReq)
    val rfReadResp1 = Input(new regFileReadResp)
    val rfReadReq2  = Output(new regFileReadReq)
    val rfReadResp2 = Input(new regFileReadResp)

    val outUOP         = Output(uopc())
    val outRD          = Output(UInt(5.W))
    val outOperandA    = Output(UInt(32.W))
    val outOperandB    = Output(UInt(32.W))
    val outXcptInvalid = Output(Bool())
    val outRS1         = Output(UInt(5.W))
    val outRS2         = Output(UInt(5.W))
    val outUseRS2      = Output(Bool())

    val outALUOp = Output(ALUOp())
    
    val outBranchOp     = Output(BranchOp())
    val outBranchOffset = Output(UInt(32.W))
    val outPC           = Output(UInt(32.W))
    val outLinkAddr     = Output(UInt(32.W))
  })

  val instr  = io.inInstr
  val opcode = instr(6, 0)
  val rd     = instr(11, 7)
  val funct3 = instr(14, 12)
  val rs1    = instr(19, 15)
  val rs2    = instr(24, 20)
  val funct7 = instr(31, 25)

  val immI = Cat(Fill(20, instr(31)), instr(31, 20))
  
  val immB = Cat(
    Fill(20, instr(31)),
    instr(7),
    instr(30, 25),
    instr(11, 8),
    0.U(1.W)
  )
  
  val immJ = Cat(
    Fill(12, instr(31)),
    instr(19, 12),
    instr(20),
    instr(30, 21),
    0.U(1.W)
  )

  io.rfReadReq1.addr := rs1
  io.rfReadReq2.addr := rs2

  val rs1Data = io.rfReadResp1.data
  val rs2Data = io.rfReadResp2.data

  io.outUOP         := uopc.NOP
  io.outRD          := 0.U
  io.outOperandA    := 0.U
  io.outOperandB    := 0.U
  io.outXcptInvalid := false.B
  io.outALUOp       := ALUOp.ADD
  io.outRS1         := rs1
  io.outRS2         := rs2
  io.outUseRS2      := false.B
  io.outBranchOp    := BranchOp.BEQ
  io.outBranchOffset := 0.U
  io.outPC          := io.inPC
  io.outLinkAddr    := io.inPC + 4.U

  val isRType = opcode === OPCODE_OP
  val isIType = opcode === OPCODE_OP_IMM
  val isBType = opcode === OPCODE_BRANCH
  val isJAL   = opcode === OPCODE_JAL
  val isJALR  = opcode === OPCODE_JALR

  // TRUE NOP or all zeros (0x00000000 or 0x00000013)
  val isTrueNOP = (instr === "h00000013".U) || (instr === "h00000000".U)

  val isADD  = isRType && (funct3 === FUNCT3_ADD_SUB) && (funct7 === FUNCT7_ADD_SRL)
  val isADDI = isIType && (funct3 === FUNCT3_ADD_SUB)
  val isSUB  = isRType && (funct3 === FUNCT3_ADD_SUB) && (funct7 === FUNCT7_SUB_SRA)
  val isAND  = (isRType || isIType) && (funct3 === FUNCT3_AND)
  val isOR   = (isRType || isIType) && (funct3 === FUNCT3_OR)
  val isXOR  = (isRType || isIType) && (funct3 === FUNCT3_XOR)
  val isSLL  = (isRType || isIType) && (funct3 === FUNCT3_SLL)
  val isSRL  = (isRType || isIType) && (funct3 === FUNCT3_SRL_SRA) && (funct7 === FUNCT7_ADD_SRL)
  val isSRA  = (isRType || isIType) && (funct3 === FUNCT3_SRL_SRA) && (funct7 === FUNCT7_SUB_SRA)
  val isSLT  = (isRType || isIType) && (funct3 === FUNCT3_SLT)
  val isSLTU = (isRType || isIType) && (funct3 === FUNCT3_SLTU)

  val isALUInstr = (isADD || isADDI || isSUB || isAND || isOR || isXOR ||
                    isSLL || isSRL || isSRA || isSLT || isSLTU) && !isTrueNOP

  val isBEQ  = isBType && (funct3 === FUNCT3_BEQ)
  val isBNE  = isBType && (funct3 === FUNCT3_BNE)
  val isBLT  = isBType && (funct3 === FUNCT3_BLT)
  val isBGE  = isBType && (funct3 === FUNCT3_BGE)
  val isBLTU = isBType && (funct3 === FUNCT3_BLTU)
  val isBGEU = isBType && (funct3 === FUNCT3_BGEU)

  val isBranchInstr = isBEQ || isBNE || isBLT || isBGE || isBLTU || isBGEU

  when(isALUInstr) {
    io.outUOP      := uopc.ALU
    io.outRD       := rd
    io.outOperandA := rs1Data
    io.outOperandB := Mux(isRType, rs2Data, immI)
    io.outUseRS2   := isRType

    when(isADD || isADDI) {
      io.outALUOp := ALUOp.ADD
    }.elsewhen(isSUB) {
      io.outALUOp := ALUOp.SUB
    }.elsewhen(isAND) {
      io.outALUOp := ALUOp.AND
    }.elsewhen(isOR) {
      io.outALUOp := ALUOp.OR
    }.elsewhen(isXOR) {
      io.outALUOp := ALUOp.XOR
    }.elsewhen(isSLL) {
      io.outALUOp := ALUOp.SLL
    }.elsewhen(isSRL) {
      io.outALUOp := ALUOp.SRL
    }.elsewhen(isSRA) {
      io.outALUOp := ALUOp.SRA
    }.elsewhen(isSLT) {
      io.outALUOp := ALUOp.SLT
    }.elsewhen(isSLTU) {
      io.outALUOp := ALUOp.SLTU
    }
    
  }.elsewhen(isBranchInstr) {
    io.outUOP          := uopc.BRANCH
    io.outRD           := 0.U
    io.outOperandA     := rs1Data
    io.outOperandB     := rs2Data
    io.outBranchOffset := immB
    io.outUseRS2       := true.B

    when(isBEQ) {
      io.outBranchOp := BranchOp.BEQ
    }.elsewhen(isBNE) {
      io.outBranchOp := BranchOp.BNE
    }.elsewhen(isBLT) {
      io.outBranchOp := BranchOp.BLT
    }.elsewhen(isBGE) {
      io.outBranchOp := BranchOp.BGE
    }.elsewhen(isBLTU) {
      io.outBranchOp := BranchOp.BLTU
    }.elsewhen(isBGEU) {
      io.outBranchOp := BranchOp.BGEU
    }
    
  }.elsewhen(isJAL) {
    io.outUOP          := uopc.JAL
    io.outRD           := rd
    io.outOperandA     := io.inPC
    io.outOperandB     := immJ
    io.outBranchOffset := immJ
    io.outBranchOp     := BranchOp.JAL
    io.outUseRS2       := false.B
    
  }.elsewhen(isJALR) {
    io.outUOP          := uopc.JALR
    io.outRD           := rd
    io.outOperandA     := rs1Data
    io.outOperandB     := immI
    io.outBranchOffset := immI
    io.outBranchOp     := BranchOp.JALR
    io.outUseRS2       := false.B
    
  }.elsewhen(isTrueNOP) {
    io.outUOP         := uopc.NOP
    io.outRD          := 0.U
    io.outOperandA    := 0.U
    io.outOperandB    := 0.U
    io.outXcptInvalid := false.B
    io.outALUOp       := ALUOp.ADD
    io.outUseRS2      := false.B
    
  }.otherwise {
    // Treat unknown instructions as NOP instead of exception
    io.outUOP         := uopc.NOP
    io.outRD          := 0.U
    io.outOperandA    := 0.U
    io.outOperandB    := 0.U
    io.outXcptInvalid := false.B  // CHANGED: was true.B
    io.outALUOp       := ALUOp.ADD
    io.outUseRS2      := false.B
  }
}
