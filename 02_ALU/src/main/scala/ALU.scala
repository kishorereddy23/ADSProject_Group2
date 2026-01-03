// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU University Kaiserslautern-Landau

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

  // Default: output zero (all tests will fail initially)
  io.aluResult := 0.U

  // =============================================================================
  // TDD CYCLE 1: ADD Operation Implementation
  // =============================================================================
  switch(io.operation) {
    is(ALUOp.ADD) {
      // Addition with wraparound (modulo 2^32)
      io.aluResult := io.operandA + io.operandB
    }
    // =============================================================================
    // TDD CYCLE 2: SUB Operation Implementation
    // =============================================================================
    is(ALUOp.SUB) {
      // Subtraction with wraparound
      io.aluResult := io.operandA - io.operandB
    }
    // =============================================================================
    // TDD CYCLE 3: AND Operation Implementation
    // =============================================================================
    is(ALUOp.AND) {
      // Bitwise AND
      io.aluResult := io.operandA & io.operandB
    }
    // =============================================================================
    // TDD CYCLE 4: OR Operation Implementation
    // =============================================================================
    is(ALUOp.OR) {
      // Bitwise OR
      io.aluResult := io.operandA | io.operandB
    }
    // =============================================================================
    // TDD CYCLE 5: XOR Operation Implementation
    // =============================================================================
    is(ALUOp.XOR) {
      // Bitwise XOR
      io.aluResult := io.operandA ^ io.operandB
    }
    // =============================================================================
    // TDD CYCLE 6: SLL (Shift Left Logical) Operation Implementation
    // =============================================================================
    is(ALUOp.SLL) {
      // Shift Left Logical - use only lower 5 bits of operandB
      io.aluResult := io.operandA << io.operandB(4, 0)
    }
    // =============================================================================
    // TDD CYCLE 7: SRL (Shift Right Logical) Operation Implementation
    // =============================================================================
    is(ALUOp.SRL) {
      // Shift Right Logical - use only lower 5 bits of operandB
      io.aluResult := io.operandA >> io.operandB(4, 0)
    }
    // =============================================================================
    // TDD CYCLE 8: SRA (Shift Right Arithmetic) Operation Implementation
    // =============================================================================
    is(ALUOp.SRA) {
      // Shift Right Arithmetic - treat operandA as signed
      io.aluResult := (io.operandA.asSInt >> io.operandB(4, 0)).asUInt
    }
    // =============================================================================
    // TDD CYCLE 9: SLT (Set Less Than - Signed) Operation Implementation
    // =============================================================================
    is(ALUOp.SLT) {
      // Set Less Than - signed comparison
      io.aluResult := Mux(io.operandA.asSInt < io.operandB.asSInt, 1.U, 0.U)
    }
    // =============================================================================
    // TDD CYCLE 10: SLTU (Set Less Than Unsigned) Operation Implementation
    // =============================================================================
    is(ALUOp.SLTU) {
      // Set Less Than Unsigned
      io.aluResult := Mux(io.operandA < io.operandB, 1.U, 0.U)
    }
  }

}