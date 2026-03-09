package PipelinedRV32I

import chisel3._
import chisel3.util._
import core_tile._

class PipelinedRV32I(BinaryFile: String, useBTB: Boolean = true) extends Module {
  val io = IO(new Bundle {
    val result              = Output(UInt(32.W))
    val exception           = Output(Bool())
    val perf_cycles         = Output(UInt(32.W))
    val perf_instructions   = Output(UInt(32.W))
    val perf_branches       = Output(UInt(32.W))
    val perf_branch_correct = Output(UInt(32.W))
    val perf_btb_accuracy   = Output(UInt(32.W))
  })

  val core = Module(new PipelinedRV32Icore(BinaryFile, useBTB))

  io.result              := core.io.result
  io.exception           := core.io.exception
  io.perf_cycles         := core.io.perf_cycles
  io.perf_instructions   := core.io.perf_instructions
  io.perf_branches       := core.io.perf_branches
  io.perf_branch_correct := core.io.perf_branch_correct
  io.perf_btb_accuracy   := core.io.perf_btb_accuracy
}