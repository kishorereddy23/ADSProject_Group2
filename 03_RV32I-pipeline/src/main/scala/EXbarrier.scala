package core_tile

import chisel3._
import uopc._

class EXbarrier extends Module {
  val io = IO(new Bundle {
    val inUOP      = Input(uopc())
    val inRD       = Input(UInt(5.W))
    val inAluValue = Input(UInt(32.W))
    val inXcpt     = Input(Bool())

    val outUOP      = Output(uopc())
    val outRD       = Output(UInt(5.W))
    val outAluValue = Output(UInt(32.W))
    val outXcpt     = Output(Bool())
  })

  val uopReg = RegInit(uopc.NOP)
  val rdReg  = RegInit(0.U(5.W))
  val aluReg = RegInit(0.U(32.W))
  val excReg = RegInit(false.B)

  uopReg := io.inUOP
  rdReg  := io.inRD
  aluReg := io.inAluValue
  excReg := io.inXcpt

  io.outUOP      := uopReg
  io.outRD       := rdReg
  io.outAluValue := aluReg
  io.outXcpt     := excReg
}