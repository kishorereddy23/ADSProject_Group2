package core_tile

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import Assignment02.{ALU, ALUOp}
import uopc._
import RV32I._

class PipelinedRV32Icore(BinaryFile: String) extends Module {
  val io = IO(new Bundle {
    val result    = Output(UInt(32.W))
    val exception = Output(Bool())
  })

  // Instruction memory
  val imem = Mem(4096, UInt(32.W))
  loadMemoryFromFile(imem, BinaryFile)

  // Program counter
  val pc = RegInit(0.U(32.W))

  // Submodules
  val rf         = Module(new regFile())
  val ifStage    = Module(new IFstage())
  val ifBarrier  = Module(new IFbarrier())
  val idStage    = Module(new IDstage())
  val idBarrier  = Module(new IDbarrier())
  val exStage    = Module(new EXstage())
  val exBarrier  = Module(new EXbarrier())
  val memStage   = Module(new MEM())
  val memBarrier = Module(new MEMbarrier())
  val wbBarrier  = Module(new WBbarrier())

  // IF stage
  ifStage.io.inPC    := pc
  ifStage.io.inInstr := imem(pc(13, 2))
  pc := pc + 4.U

  ifBarrier.io.inInstr := ifStage.io.outInstr
  ifBarrier.io.inPC    := ifStage.io.outPC

  // ID stage
  idStage.io.inInstr := ifBarrier.io.outInstr
  idStage.io.inPC    := ifBarrier.io.outPC

  // regfile read
  rf.io.req_1.addr := idStage.io.rfReadReq1.addr
  rf.io.req_2.addr := idStage.io.rfReadReq2.addr
  idStage.io.rfReadResp1.data := rf.io.resp_1.data
  idStage.io.rfReadResp2.data := rf.io.resp_2.data

  // ID → IDbarrier
  idBarrier.io.inUOP         := idStage.io.outUOP
  idBarrier.io.inRD          := idStage.io.outRD
  idBarrier.io.inOperandA    := idStage.io.outOperandA
  idBarrier.io.inOperandB    := idStage.io.outOperandB
  idBarrier.io.inXcptInvalid := idStage.io.outXcptInvalid
  idBarrier.io.inRS1         := idStage.io.outRS1
  idBarrier.io.inRS2         := idStage.io.outRS2

  // -----------------------------
  // EX stage + simple WB forwarding
  // -----------------------------
  val exRS1 = idBarrier.io.outRS1
  val exRS2 = idBarrier.io.outRS2

  val wbValid = (wbBarrier.io.outRD =/= 0.U) && !wbBarrier.io.outException

  def fwdWB(src: UInt, orig: UInt): UInt = {
    Mux(wbValid && (wbBarrier.io.outRD === src), wbBarrier.io.outAluResult, orig)
  }

  val exOpA = fwdWB(exRS1, idBarrier.io.outOperandA)
  val exOpB = Mux(
    idStage.io.inInstr(6, 0) === OPCODE_OP,      // R-type: rs2
    fwdWB(exRS2, idBarrier.io.outOperandB),
    idBarrier.io.outOperandB                    // I-type: immediate
  )

  exStage.io.inUOP      := idBarrier.io.outUOP
  exStage.io.inOperandA := exOpA
  exStage.io.inOperandB := exOpB
  exStage.io.inALUOp    := idStage.io.outALUOp

  // EX → EXbarrier
  exBarrier.io.inUOP      := exStage.io.outUOP
  exBarrier.io.inRD       := idBarrier.io.outRD
  exBarrier.io.inAluValue := exStage.io.outAluResult
  exBarrier.io.inXcpt     := idBarrier.io.outXcptInvalid

  // MEM stage + barrier (no real MEM yet)
  memStage.io := DontCare

  memBarrier.io.inAluResult := exBarrier.io.outAluValue
  memBarrier.io.inRD        := exBarrier.io.outRD
  memBarrier.io.inException := exBarrier.io.outXcpt

  // WB barrier + writeback
  wbBarrier.io.inAluResult := memBarrier.io.outAluResult
  wbBarrier.io.inRD        := memBarrier.io.outRD
  wbBarrier.io.inException := memBarrier.io.outException

  val wbData = wbBarrier.io.outAluResult
  val wbRD   = wbBarrier.io.outRD
  val wbExc  = wbBarrier.io.outException

  rf.io.req_3.addr  := wbRD
  rf.io.req_3.data  := wbData
  rf.io.req_3.wr_en := (wbRD =/= 0.U) && !wbExc

  io.result    := wbData
  io.exception := wbExc
}
