package core_tile

import chisel3._
import chisel3.util._

class BranchUnit extends Module {
  val io = IO(new Bundle {
    // Inputs
    val operandA   = Input(UInt(32.W))  // rs1 value
    val operandB   = Input(UInt(32.W))  // rs2 value or immediate
    val branchOp   = Input(BranchOp())  // Branch operation type
    val pc         = Input(UInt(32.W))  // Current PC
    val offset     = Input(UInt(32.W))  // Branch offset (sign-extended)
    
    // Outputs
    val branchTaken  = Output(Bool())
    val branchTarget = Output(UInt(32.W))
  })

  // ========================================
  // Branch Comparison Logic
  // ========================================
  
  val eq = io.operandA === io.operandB
  val lt_signed = io.operandA.asSInt < io.operandB.asSInt
  val lt_unsigned = io.operandA < io.operandB
  
  io.branchTaken := false.B
  
  switch(io.branchOp) {
    is(BranchOp.BEQ)  { io.branchTaken := eq }
    is(BranchOp.BNE)  { io.branchTaken := !eq }
    is(BranchOp.BLT)  { io.branchTaken := lt_signed }
    is(BranchOp.BGE)  { io.branchTaken := !lt_signed }
    is(BranchOp.BLTU) { io.branchTaken := lt_unsigned }
    is(BranchOp.BGEU) { io.branchTaken := !lt_unsigned }
    is(BranchOp.JAL)  { io.branchTaken := true.B }   // Always jump
    is(BranchOp.JALR) { io.branchTaken := true.B }   // Always jump
  }
  
  // ========================================
  // Branch Target Calculation
  // ========================================
  
  // For JALR: target = (rs1 + offset) & ~1 (clear LSB)
  // For others: target = PC + offset
  when(io.branchOp === BranchOp.JALR) {
    io.branchTarget := (io.operandA + io.offset) & "hFFFFFFFE".U
  }.otherwise {
    io.branchTarget := io.pc + io.offset
  }
}