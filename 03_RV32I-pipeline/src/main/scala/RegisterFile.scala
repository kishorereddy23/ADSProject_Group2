package core_tile

import chisel3._

class regFileReadReq extends Bundle {
  val addr = UInt(5.W)
}

class regFileReadResp extends Bundle {
  val data = UInt(32.W)
}

class regFileWriteReq extends Bundle {
  val addr  = UInt(5.W)
  val data  = UInt(32.W)
  val wr_en = Bool()
}

class regFile extends Module {
  val io = IO(new Bundle {
    val req_1  = Input(new regFileReadReq)
    val resp_1 = Output(new regFileReadResp)

    val req_2  = Input(new regFileReadReq)
    val resp_2 = Output(new regFileReadResp)

    val req_3  = Input(new regFileWriteReq)
  })

  // 32 registers of 32-bit, init to 0
  val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  // Combinational reads (x0 always 0)
  io.resp_1.data := Mux(io.req_1.addr === 0.U, 0.U, regs(io.req_1.addr))
  io.resp_2.data := Mux(io.req_2.addr === 0.U, 0.U, regs(io.req_2.addr))

  // Synchronous write (ignore writes to x0)
  when(io.req_3.wr_en && (io.req_3.addr =/= 0.U)) {
    regs(io.req_3.addr) := io.req_3.data
  }

  // Keep x0 hardwired to 0
  regs(0) := 0.U
}
