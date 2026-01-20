package core_tile

import chisel3._

class WBstage extends Module {
  val io = IO(new Bundle {
    val inAluResult = Input(UInt(32.W))
    val inRD        = Input(UInt(5.W))

    val regFileReq  = Output(new regFileWriteReq)
    val check_res   = Output(UInt(32.W))
  })

  io.regFileReq.addr := io.inRD
  io.regFileReq.data := io.inAluResult
  io.regFileReq.wr_en := io.inRD =/= 0.U

  io.check_res := io.inAluResult
}
