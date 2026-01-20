package Assignment02

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// Define the ALU Operation Enum
object ALUOp extends ChiselEnum {
  val ADD  = Value(0x0.U)
  val SUB  = Value(0x1.U)
  val AND  = Value(0x2.U)
  val OR   = Value(0x3.U)
  val XOR  = Value(0x4.U)
  val SLL  = Value(0x5.U)
  val SRL  = Value(0x6.U)
  val SRA  = Value(0x7.U)
  val SLT  = Value(0x8.U)
  val SLTU = Value(0x9.U)
  val PASSB = Value(0xA.U)
}

class ALU extends Module {
  val io = IO(new Bundle {
    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))
    val operation = Input(ALUOp())
    val aluResult = Output(UInt(32.W))
  })

  io.aluResult := 0.U

  switch(io.operation) {
    is(ALUOp.ADD)  { io.aluResult := io.operandA + io.operandB }
    is(ALUOp.SUB)  { io.aluResult := io.operandA - io.operandB }
    is(ALUOp.AND)  { io.aluResult := io.operandA & io.operandB }
    is(ALUOp.OR)   { io.aluResult := io.operandA | io.operandB }
    is(ALUOp.XOR)  { io.aluResult := io.operandA ^ io.operandB }
    is(ALUOp.SLL)  { io.aluResult := io.operandA << io.operandB(4, 0) }
    is(ALUOp.SRL)  { io.aluResult := io.operandA >> io.operandB(4, 0) }
    is(ALUOp.SRA)  { io.aluResult := (io.operandA.asSInt >> io.operandB(4, 0)).asUInt }
    is(ALUOp.SLT)  { io.aluResult := Mux(io.operandA.asSInt < io.operandB.asSInt, 1.U, 0.U) }
    is(ALUOp.SLTU) { io.aluResult := Mux(io.operandA < io.operandB, 1.U, 0.U) }
    is(ALUOp.PASSB){ io.aluResult := io.operandB }
  }
}