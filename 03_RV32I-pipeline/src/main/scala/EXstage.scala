package core_tile

import chisel3._
import chisel3.util._
import uopc._
import Assignment02.{ALU, ALUOp}

class EXstage extends Module {
  val io = IO(new Bundle {
    // Inputs
    val inUOP      = Input(uopc())
    val inOperandA = Input(UInt(32.W))
    val inOperandB = Input(UInt(32.W))
    val inALUOp    = Input(ALUOp())
    
    // Branch/Jump inputs (NEW)
    val inBranchOp     = Input(BranchOp())
    val inPC           = Input(UInt(32.W))
    val inBranchOffset = Input(UInt(32.W))

    // Outputs
    val outUOP       = Output(uopc())
    val outAluResult = Output(UInt(32.W))
    
    // Branch/Jump outputs (NEW)
    val outBranchTaken  = Output(Bool())
    val outBranchTarget = Output(UInt(32.W))
  })

  val alu = Module(new ALU())
  val branchUnit = Module(new BranchUnit())

  // ========================================
  // ALU Connections
  // ========================================
  
  alu.io.operandA := io.inOperandA
  alu.io.operandB := io.inOperandB
  alu.io.operation := io.inALUOp

  // ========================================
  // Branch Unit Connections (NEW)
  // ========================================
  
  branchUnit.io.operandA := io.inOperandA
  branchUnit.io.operandB := io.inOperandB
  branchUnit.io.branchOp := io.inBranchOp
  branchUnit.io.pc := io.inPC
  branchUnit.io.offset := io.inBranchOffset

  // ========================================
  // Outputs
  // ========================================
  
  io.outUOP := io.inUOP
  io.outAluResult := alu.io.aluResult
  io.outBranchTaken := branchUnit.io.branchTaken
  io.outBranchTarget := branchUnit.io.branchTarget
}