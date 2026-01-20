package core_tile

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// Micro-op codes for the pipeline
object uopc extends ChiselEnum {
  val NOP = Value(0.U)
  val ALU = Value(1.U)
}

// RV32I decode constants (subset: R-type and I-type ALU)
object RV32I {
  val OPCODE_OP     = "b0110011".U(7.W) // R-type
  val OPCODE_OP_IMM = "b0010011".U(7.W) // I-type

  // funct3 values
  val FUNCT3_ADD_SUB  = "b000".U(3.W)
  val FUNCT3_SLL      = "b001".U(3.W)
  val FUNCT3_SLT      = "b010".U(3.W)
  val FUNCT3_SLTU     = "b011".U(3.W)
  val FUNCT3_XOR      = "b100".U(3.W)
  val FUNCT3_SRL_SRA  = "b101".U(3.W)
  val FUNCT3_OR       = "b110".U(3.W)
  val FUNCT3_AND      = "b111".U(3.W)

  val FUNCT7_ADD_SRL  = "b0000000".U(7.W)
  val FUNCT7_SUB_SRA  = "b0100000".U(7.W)
}