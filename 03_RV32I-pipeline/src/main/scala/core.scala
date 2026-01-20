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
  val wbStage    = Module(new WBstage())  // ADD THIS!
  val wbBarrier  = Module(new WBbarrier())

  // ============================================
  // IF STAGE
  // ============================================
  ifStage.io.inPC    := pc
  ifStage.io.inInstr := imem(pc(13, 2))
  pc := pc + 4.U

  ifBarrier.io.inInstr := ifStage.io.outInstr
  ifBarrier.io.inPC    := ifStage.io.outPC

  // ============================================
  // ID STAGE
  // ============================================
  idStage.io.inInstr := ifBarrier.io.outInstr
  idStage.io.inPC    := ifBarrier.io.outPC

  // Register file read
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
  idBarrier.io.inIsRType     := idStage.io.outIsRType
  idBarrier.io.inALUOp       := idStage.io.outALUOp

  // ============================================
  // EX STAGE WITH FORWARDING
  // ============================================
  val exRS1 = idBarrier.io.outRS1
  val exRS2 = idBarrier.io.outRS2

  // Forwarding from EX/MEM (1 cycle ago)
  val exMemValid = (exBarrier.io.outRD =/= 0.U) && !exBarrier.io.outXcpt
  
  // Forwarding from MEM/WB (2 cycles ago)
  val memWbValid = (memBarrier.io.outRD =/= 0.U) && !memBarrier.io.outException
  
  // Forwarding from WB/Out (3 cycles ago)  
  val wbValid = (wbBarrier.io.outRD =/= 0.U) && !wbBarrier.io.outException

  // Forwarding function with priority: EX/MEM > MEM/WB > WB > original
  def forwardOperand(src: UInt, orig: UInt): UInt = {
    val fromExMem = exMemValid && (exBarrier.io.outRD === src)
    val fromMemWb = memWbValid && (memBarrier.io.outRD === src)
    val fromWb = wbValid && (wbBarrier.io.outRD === src)
    
    Mux(fromExMem, exBarrier.io.outAluValue,
      Mux(fromMemWb, memBarrier.io.outAluResult,
        Mux(fromWb, wbBarrier.io.outAluResult, orig)))
  }

  // Apply forwarding to operands
  val exOpA = forwardOperand(exRS1, idBarrier.io.outOperandA)
  val exOpB = Mux(
    idBarrier.io.outIsRType,
    forwardOperand(exRS2, idBarrier.io.outOperandB),
    idBarrier.io.outOperandB
  )

  exStage.io.inUOP      := idBarrier.io.outUOP
  exStage.io.inOperandA := exOpA
  exStage.io.inOperandB := exOpB
  exStage.io.inALUOp    := idBarrier.io.outALUOp

  // EX → EXbarrier
  exBarrier.io.inUOP      := exStage.io.outUOP
  exBarrier.io.inRD       := idBarrier.io.outRD
  exBarrier.io.inAluValue := exStage.io.outAluResult
  exBarrier.io.inXcpt     := idBarrier.io.outXcptInvalid

  // ============================================
  // MEM STAGE
  // ============================================
  memStage.io := DontCare

  memBarrier.io.inAluResult := exBarrier.io.outAluValue
  memBarrier.io.inRD        := exBarrier.io.outRD
  memBarrier.io.inException := exBarrier.io.outXcpt

  // ============================================
  // WB STAGE - WRITE TO REGISTER FILE HERE!
  // ============================================
  wbStage.io.inAluResult := memBarrier.io.outAluResult
  wbStage.io.inRD        := memBarrier.io.outRD
  
  // Connect WB stage to register file
  rf.io.req_3 <> wbStage.io.regFileReq

  // WB → WBbarrier (for output)
  wbBarrier.io.inAluResult := wbStage.io.check_res
  wbBarrier.io.inRD        := memBarrier.io.outRD
  wbBarrier.io.inException := memBarrier.io.outException

  // Output from final barrier
  io.result    := wbBarrier.io.outAluResult
  io.exception := wbBarrier.io.outException
}