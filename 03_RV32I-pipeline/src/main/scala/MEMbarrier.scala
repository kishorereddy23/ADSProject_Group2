package core_tile

import chisel3._

class MEMbarrier extends Module {
  val io = IO(new Bundle {
    val inAluResult = Input(UInt(32.W))
    val inRD        = Input(UInt(5.W))
    val inException = Input(Bool())

    val outAluResult = Output(UInt(32.W))
    val outRD        = Output(UInt(5.W))
    val outException = Output(Bool())
  })

  val aluReg = RegInit(0.U(32.W))
  val rdReg  = RegInit(0.U(5.W))
  val excReg = RegInit(false.B)

  aluReg := io.inAluResult
  rdReg  := io.inRD
  excReg := io.inException

  io.outAluResult := aluReg
  io.outRD        := rdReg
  io.outException := excReg

  when (true.B) {
  printf(p"MEMB: inVal=0x${Hexadecimal(io.inAluResult)} outVal=0x${Hexadecimal(io.outAluResult)} " +
         p"inRD=${io.inRD} outRD=${io.outRD}\n")
}

}
