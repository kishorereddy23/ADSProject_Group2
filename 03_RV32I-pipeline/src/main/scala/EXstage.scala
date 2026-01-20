package core_tile

import chisel3._
import chisel3.util._
import uopc._
import Assignment02.{ALU, ALUOp}

class EXstage extends Module {
  val io = IO(new Bundle {
    val inUOP      = Input(uopc())
    val inOperandA = Input(UInt(32.W))
    val inOperandB = Input(UInt(32.W))
    val inALUOp    = Input(ALUOp())

    val outUOP       = Output(uopc())
    val outAluResult = Output(UInt(32.W))
  })

  val alu = Module(new ALU())

  alu.io.operandA := io.inOperandA
  alu.io.operandB := io.inOperandB
  alu.io.operation := io.inALUOp

  io.outUOP       := io.inUOP
  io.outAluResult := alu.io.aluResult
}