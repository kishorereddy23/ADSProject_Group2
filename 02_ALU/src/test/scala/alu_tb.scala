// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import Assignment02._

// =============================================================================
// TDD CYCLE 1: ADD Operation
// =============================================================================

// Test ADD operation
class ALUAddTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Add_Tester" should "test ADD operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Basic addition
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(ALUOp.ADD)
      dut.io.aluResult.expect(20.U)
      dut.clock.step(1)

      // Test Case 2: Add with zero
      dut.io.operandA.poke(100.U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.ADD)
      dut.io.aluResult.expect(100.U)
      dut.clock.step(1)

      // Test Case 3: Wraparound (overflow) - modulo 2^32
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.ADD)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 4: Large numbers
      dut.io.operandA.poke("h7FFFFFFF".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.ADD)
      dut.io.aluResult.expect("h80000000".U)
      dut.clock.step(1)

      // Test Case 5: Both operands are max value
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(ALUOp.ADD)
      dut.io.aluResult.expect("hFFFFFFFE".U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 3: AND Operation
// =============================================================================

// Test AND operation
class ALUAndTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_And_Tester" should "test AND operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Basic AND
      dut.io.operandA.poke("hFF00FF00".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.operation.poke(ALUOp.AND)
      dut.io.aluResult.expect("h0F000F00".U)
      dut.clock.step(1)

      // Test Case 2: AND with all ones (identity)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(ALUOp.AND)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)

      // Test Case 3: AND with zero
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.AND)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 4: AND with itself
      dut.io.operandA.poke("hAAAAAAAA".U)
      dut.io.operandB.poke("hAAAAAAAA".U)
      dut.io.operation.poke(ALUOp.AND)
      dut.io.aluResult.expect("hAAAAAAAA".U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 2: SUB Operation
// =============================================================================

// Test SUB operation
class ALUSubTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sub_Tester" should "test SUB operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Basic subtraction
      dut.io.operandA.poke(20.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(ALUOp.SUB)
      dut.io.aluResult.expect(10.U)
      dut.clock.step(1)

      // Test Case 2: Subtract zero
      dut.io.operandA.poke(100.U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.SUB)
      dut.io.aluResult.expect(100.U)
      dut.clock.step(1)

      // Test Case 3: Result is zero
      dut.io.operandA.poke(50.U)
      dut.io.operandB.poke(50.U)
      dut.io.operation.poke(ALUOp.SUB)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 4: Underflow (wraparound)
      dut.io.operandA.poke(0.U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SUB)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)

      // Test Case 5: Large subtraction
      dut.io.operandA.poke("h80000000".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SUB)
      dut.io.aluResult.expect("h7FFFFFFF".U)
      dut.clock.step(1)
    }
  }
}