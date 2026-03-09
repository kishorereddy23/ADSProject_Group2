package core_tile

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// Micro-op codes for the pipeline
object uopc extends ChiselEnum {
  val NOP    = Value(0.U)
  val ALU    = Value(1.U)
  val BRANCH = Value(2.U)
  val JAL    = Value(3.U)
  val JALR   = Value(4.U)
}

// RV32I Instruction Set Constants
object RV32I {
  // Opcodes
  val OPCODE_OP     = "b0110011".U(7.W)  // R-type
  val OPCODE_OP_IMM = "b0010011".U(7.W)  // I-type
  val OPCODE_BRANCH = "b1100011".U(7.W)  // B-type (branches)
  val OPCODE_JAL    = "b1101111".U(7.W)  // J-type (jump and link)
  val OPCODE_JALR   = "b1100111".U(7.W)  // I-type (jump and link register)

  // funct3 values for ALU operations
  val FUNCT3_ADD_SUB  = "b000".U(3.W)
  val FUNCT3_SLL      = "b001".U(3.W)
  val FUNCT3_SLT      = "b010".U(3.W)
  val FUNCT3_SLTU     = "b011".U(3.W)
  val FUNCT3_XOR      = "b100".U(3.W)
  val FUNCT3_SRL_SRA  = "b101".U(3.W)
  val FUNCT3_OR       = "b110".U(3.W)
  val FUNCT3_AND      = "b111".U(3.W)

  // funct7 values
  val FUNCT7_ADD_SRL  = "b0000000".U(7.W)
  val FUNCT7_SUB_SRA  = "b0100000".U(7.W)

  // funct3 values for Branch operations
  val FUNCT3_BEQ  = "b000".U(3.W)
  val FUNCT3_BNE  = "b001".U(3.W)
  val FUNCT3_BLT  = "b100".U(3.W)
  val FUNCT3_BGE  = "b101".U(3.W)
  val FUNCT3_BLTU = "b110".U(3.W)
  val FUNCT3_BGEU = "b111".U(3.W)

  // funct3 for JALR
  val FUNCT3_JALR = "b000".U(3.W)
}

// Branch Operation Types
object BranchOp extends ChiselEnum {
  val BEQ  = Value(0.U)
  val BNE  = Value(1.U)
  val BLT  = Value(2.U)
  val BGE  = Value(3.U)
  val BLTU = Value(4.U)
  val BGEU = Value(5.U)
  val JAL  = Value(6.U)
  val JALR = Value(7.U)
}