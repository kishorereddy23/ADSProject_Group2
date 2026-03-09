package core_tile

import chisel3._
import chisel3.util._

class ForwardingUnit extends Module {
  val io = IO(new Bundle {
    // Source registers from ID/EX barrier
    val rs1_ex = Input(UInt(5.W))
    val rs2_ex = Input(UInt(5.W))
    
    // Destination registers from pipeline stages
    val rd_mem = Input(UInt(5.W))     // From EX/MEM barrier
    val rd_wb1 = Input(UInt(5.W))     // From MEM/WB barrier
    val rd_wb2 = Input(UInt(5.W))     // From WB barrier
    
    // Write enable signals
    val wr_en_mem = Input(Bool())
    val wr_en_wb1 = Input(Bool())
    val wr_en_wb2 = Input(Bool())
    
    // Data values from pipeline stages
    val data_mem = Input(UInt(32.W))
    val data_wb1 = Input(UInt(32.W))
    val data_wb2 = Input(UInt(32.W))
    
    // Original operands from register file
    val operandA_orig = Input(UInt(32.W))
    val operandB_orig = Input(UInt(32.W))
    
    // Control signals
    val useRS2 = Input(Bool())  // True if instruction uses rs2 (R-type, branches)
    
    // Forwarded outputs
    val operandA_fwd = Output(UInt(32.W))
    val operandB_fwd = Output(UInt(32.W))
    
    // Debug outputs
    val forwardA_src = Output(UInt(2.W))  // 0=none, 1=MEM, 2=WB1, 3=WB2
    val forwardB_src = Output(UInt(2.W))
  })

  // ========================================
  // Forwarding Logic for Operand A (rs1)
  // ========================================
  
  val forwardA_from_mem = io.wr_en_mem && (io.rd_mem =/= 0.U) && (io.rd_mem === io.rs1_ex)
  val forwardA_from_wb1 = io.wr_en_wb1 && (io.rd_wb1 =/= 0.U) && (io.rd_wb1 === io.rs1_ex)
  val forwardA_from_wb2 = io.wr_en_wb2 && (io.rd_wb2 =/= 0.U) && (io.rd_wb2 === io.rs1_ex)
  
  // Priority: MEM > WB1 > WB2 > RegFile
  when(forwardA_from_mem) {
    io.operandA_fwd := io.data_mem
    io.forwardA_src := 1.U
  }.elsewhen(forwardA_from_wb1) {
    io.operandA_fwd := io.data_wb1
    io.forwardA_src := 2.U
  }.elsewhen(forwardA_from_wb2) {
    io.operandA_fwd := io.data_wb2
    io.forwardA_src := 3.U
  }.otherwise {
    io.operandA_fwd := io.operandA_orig
    io.forwardA_src := 0.U
  }
  
  // ========================================
  // Forwarding Logic for Operand B (rs2)
  // ========================================
  
  val forwardB_from_mem = io.wr_en_mem && (io.rd_mem =/= 0.U) && (io.rd_mem === io.rs2_ex)
  val forwardB_from_wb1 = io.wr_en_wb1 && (io.rd_wb1 =/= 0.U) && (io.rd_wb1 === io.rs2_ex)
  val forwardB_from_wb2 = io.wr_en_wb2 && (io.rd_wb2 =/= 0.U) && (io.rd_wb2 === io.rs2_ex)
  
  // Only forward if instruction uses rs2 (R-type, B-type)
  when(io.useRS2) {
    when(forwardB_from_mem) {
      io.operandB_fwd := io.data_mem
      io.forwardB_src := 1.U
    }.elsewhen(forwardB_from_wb1) {
      io.operandB_fwd := io.data_wb1
      io.forwardB_src := 2.U
    }.elsewhen(forwardB_from_wb2) {
      io.operandB_fwd := io.data_wb2
      io.forwardB_src := 3.U
    }.otherwise {
      io.operandB_fwd := io.operandB_orig
      io.forwardB_src := 0.U
    }
  }.otherwise {
    // I-type: use immediate (no forwarding)
    io.operandB_fwd := io.operandB_orig
    io.forwardB_src := 0.U
  }
}