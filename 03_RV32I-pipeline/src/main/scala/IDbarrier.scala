package core_tile

import chisel3._
import uopc._

class IDbarrier extends Module {
  val io = IO(new Bundle {
    val inUOP         = Input(uopc())
    val inRD          = Input(UInt(5.W))
    val inOperandA    = Input(UInt(32.W))
    val inOperandB    = Input(UInt(32.W))
    val inXcptInvalid = Input(Bool())
    val inRS1         = Input(UInt(5.W))
    val inRS2         = Input(UInt(5.W))

    val outUOP         = Output(uopc())
    val outRD          = Output(UInt(5.W))
    val outOperandA    = Output(UInt(32.W))
    val outOperandB    = Output(UInt(32.W))
    val outXcptInvalid = Output(Bool())
    val outRS1         = Output(UInt(5.W))
    val outRS2         = Output(UInt(5.W))
  })

  val uopReg   = RegInit(uopc.NOP)
  val rdReg    = RegInit(0.U(5.W))
  val opAReg   = RegInit(0.U(32.W))
  val opBReg   = RegInit(0.U(32.W))
  val xcptReg  = RegInit(false.B)
  val rs1Reg   = RegInit(0.U(5.W))
  val rs2Reg   = RegInit(0.U(5.W))

  uopReg  := io.inUOP
  rdReg   := io.inRD
  opAReg  := io.inOperandA
  opBReg  := io.inOperandB
  xcptReg := io.inXcptInvalid
  rs1Reg  := io.inRS1
  rs2Reg  := io.inRS2

  io.outUOP         := uopReg
  io.outRD          := rdReg
  io.outOperandA    := opAReg
  io.outOperandB    := opBReg
  io.outXcptInvalid := xcptReg
  io.outRS1         := rs1Reg
  io.outRS2         := rs2Reg
}
