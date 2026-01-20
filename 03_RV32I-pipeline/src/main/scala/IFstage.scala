package core_tile

import chisel3._
import chisel3.util._

class IFstage extends Module {
  val io = IO(new Bundle {
    val inPC    = Input(UInt(32.W))
    val inInstr = Input(UInt(32.W))

    val outPC   = Output(UInt(32.W))
    val outInstr= Output(UInt(32.W))
  })

  // In this simple design, IFstage just forwards PC and instruction
  io.outPC    := io.inPC
  io.outInstr := io.inInstr
}
