package core_tile

import chisel3._
import chisel3.util._
import uopc._
import Assignment02.ALUOp
import RV32I._

class IDstage extends Module {
  val io = IO(new Bundle {
    // From IF/IF-barrier
    val inInstr = Input(UInt(32.W))
    val inPC    = Input(UInt(32.W))

    // Register file ports
    val rfReadReq1  = Output(new regFileReadReq)
    val rfReadResp1 = Input(new regFileReadResp)
    val rfReadReq2  = Output(new regFileReadReq)
    val rfReadResp2 = Input(new regFileReadResp)

    // To ID-barrier
    val outUOP         = Output(uopc())
    val outRD          = Output(UInt(5.W))
    val outOperandA    = Output(UInt(32.W))
    val outOperandB    = Output(UInt(32.W))
    val outXcptInvalid = Output(Bool())
    val outRS1         = Output(UInt(5.W))
    val outRS2         = Output(UInt(5.W))
    val outIsRType     = Output(Bool())

    // To EX for ALU control
    val outALUOp = Output(ALUOp())
  })

  val instr  = io.inInstr
  val opcode = instr(6, 0)
  val rd     = instr(11, 7)
  val funct3 = instr(14, 12)
  val rs1    = instr(19, 15)
  val rs2    = instr(24, 20)
  val funct7 = instr(31, 25)

  // I-type immediate (sign-extended)
  val immI = Cat(Fill(20, instr(31)), instr(31, 20))

  // Register file read addresses
  io.rfReadReq1.addr := rs1
  io.rfReadReq2.addr := rs2

  val rs1Data = io.rfReadResp1.data
  val rs2Data = io.rfReadResp2.data

  // Default outputs
  io.outUOP         := uopc.NOP
  io.outRD          := 0.U
  io.outOperandA    := 0.U
  io.outOperandB    := 0.U
  io.outXcptInvalid := false.B
  io.outALUOp       := ALUOp.ADD
  io.outRS1         := rs1
  io.outRS2         := rs2
  io.outIsRType     := false.B

  val isRType = opcode === OPCODE_OP
  val isIType = opcode === OPCODE_OP_IMM

  // True NOP: ADDI x0,x0,0
  val isTrueNOP = instr === "h00000013".U

  // ALU instruction decodes
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

  val isALUInstr =
    (isADD || isADDI || isSUB || isAND || isOR || isXOR ||
     isSLL || isSRL || isSRA || isSLT || isSLTU) && !isTrueNOP

  when(isALUInstr) {
    io.outUOP      := uopc.ALU
    io.outRD       := rd
    io.outOperandA := rs1Data
    io.outOperandB := Mux(isRType, rs2Data, immI)
    io.outIsRType  := isRType

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
  }.elsewhen(isTrueNOP) {
    io.outUOP         := uopc.NOP
    io.outRD          := 0.U
    io.outOperandA    := 0.U
    io.outOperandB    := 0.U
    io.outXcptInvalid := false.B
    io.outALUOp       := ALUOp.ADD
    io.outIsRType     := false.B
  }.otherwise {
    io.outXcptInvalid := true.B
  }
}