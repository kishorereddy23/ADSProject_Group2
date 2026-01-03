// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU University Kaiserslautern-Landau
// File created on 09/21/2025 by Tharindu Samarakoon (gug75kex@rptu.de)
// File updated on 10/29/2025 by Tobias Jauch (tobias.jauch@rptu.de)

package Assignment02

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// Define the ALU Operation Enum
object ALUOp extends ChiselEnum {
  val ADD   = Value(0x0.U)
  val SUB   = Value(0x1.U)
  val AND   = Value(0x2.U)
  val OR    = Value(0x3.U)
  val XOR   = Value(0x4.U)
  val SLL   = Value(0x5.U)
  val SRL   = Value(0x6.U)
  val SRA   = Value(0x7.U)
  val SLT   = Value(0x8.U)
  val SLTU  = Value(0x9.U)
  val PASSB = Value(0xA.U)
}

class ALU extends Module {
  
  val io = IO(new Bundle {
    val operandA  = Input(UInt(32.W))
    val operandB  = Input(UInt(32.W))
    val operation = Input(ALUOp())
    val aluResult = Output(UInt(32.W))
  })

io.aluResult := 0.U

// =============================================================================
  // TDD CYCLE 1: ADD Operation Implementation
  // =============================================================================
  switch(io.operation) {
    is(ALUOp.ADD) {
      // Addition with wraparound (modulo 2^32)
      io.aluResult := io.operandA + io.operandB
    }
  }

}