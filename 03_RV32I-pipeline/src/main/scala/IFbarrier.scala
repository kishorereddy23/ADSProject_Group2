package core_tile

import chisel3._

class IFbarrier extends Module {
  val io = IO(new Bundle {
    val inPC    = Input(UInt(32.W))
    val inInstr = Input(UInt(32.W))

    val outPC   = Output(UInt(32.W))
    val outInstr= Output(UInt(32.W))
  })

  val pcReg    = RegInit(0.U(32.W))
  val instrReg = RegInit(0.U(32.W))

  pcReg    := io.inPC
  instrReg := io.inInstr

  io.outPC    := pcReg
  io.outInstr := instrReg
}