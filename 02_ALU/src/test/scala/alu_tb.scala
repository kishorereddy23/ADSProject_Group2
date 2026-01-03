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
// TDD CYCLE 10: SLTU (Set Less Than Unsigned) Operation
// =============================================================================

// Test SLTU operation
class ALUSltuTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sltu_Tester" should "test SLTU operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Basic unsigned comparison (true)
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(20.U)
      dut.io.operation.poke(ALUOp.SLTU)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Test Case 2: Basic unsigned comparison (false)
      dut.io.operandA.poke(20.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(ALUOp.SLTU)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 3: Equal values (false)
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(ALUOp.SLTU)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 4: Small < Large unsigned (true)
      // Unlike SLT, 1 < 0xFFFFFFFF is TRUE in unsigned
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(ALUOp.SLTU)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Test Case 5: Large < Small unsigned (false)
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SLTU)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 6: Zero comparison
      dut.io.operandA.poke(0.U)
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SLTU)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 9: SLT (Set Less Than - Signed) Operation
// =============================================================================

// Test SLT operation
class ALUSltTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Slt_Tester" should "test SLT operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Positive < Positive (true)
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(20.U)
      dut.io.operation.poke(ALUOp.SLT)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Test Case 2: Positive < Positive (false)
      dut.io.operandA.poke(20.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(ALUOp.SLT)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 3: Equal values (false)
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(10.U)
      dut.io.operation.poke(ALUOp.SLT)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 4: Negative < Positive (true)
      dut.io.operandA.poke("h80000000".U) // -2147483648
      dut.io.operandB.poke(1.U)
      dut.io.operation.poke(ALUOp.SLT)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Test Case 5: Positive < Negative (false)
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke("hFFFFFFFF".U) // -1
      dut.io.operation.poke(ALUOp.SLT)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 6: Negative < Negative (true: -1 < -2 is false, but -2 < -1 is true)
      dut.io.operandA.poke("hFFFFFFFE".U) // -2
      dut.io.operandB.poke("hFFFFFFFF".U) // -1
      dut.io.operation.poke(ALUOp.SLT)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 8: SRA (Shift Right Arithmetic) Operation
// =============================================================================

// Test SRA operation
class ALUSraTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sra_Tester" should "test SRA operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Positive number shift by 4 (same as SRL)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(ALUOp.SRA)
      dut.io.aluResult.expect("h01234567".U)
      dut.clock.step(1)

      // Test Case 2: Negative number shift by 4 (sign extension)
      dut.io.operandA.poke("h80000000".U)  // Most negative: -2147483648
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(ALUOp.SRA)
      dut.io.aluResult.expect("hF8000000".U)  // Sign extended
      dut.clock.step(1)

      // Test Case 3: Negative number shift by 31 (all ones)
      dut.io.operandA.poke("h80000000".U)
      dut.io.operandB.poke(31.U)
      dut.io.operation.poke(ALUOp.SRA)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)

      // Test Case 4: Shift by 0
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.SRA)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)

      // Test Case 5: Negative number, various shifts
      dut.io.operandA.poke("hFFFFFF00".U)  // -256
      dut.io.operandB.poke(8.U)
      dut.io.operation.poke(ALUOp.SRA)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 7: SRL (Shift Right Logical) Operation
// =============================================================================

// Test SRL operation
class ALUSrlTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Srl_Tester" should "test SRL operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Shift by 0 (no change)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.SRL)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)

      // Test Case 2: Shift by 4
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(ALUOp.SRL)
      dut.io.aluResult.expect("h01234567".U)
      dut.clock.step(1)

      // Test Case 3: Shift by 31 (maximum)
      dut.io.operandA.poke("h80000000".U)
      dut.io.operandB.poke(31.U)
      dut.io.operation.poke(ALUOp.SRL)
      dut.io.aluResult.expect(1.U)
      dut.clock.step(1)

      // Test Case 4: Only lower 5 bits used (shift by 36 = shift by 4)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(36.U)  // 36 & 0x1F = 4
      dut.io.operation.poke(ALUOp.SRL)
      dut.io.aluResult.expect("h01234567".U)
      dut.clock.step(1)

      // Test Case 5: Logical shift of negative (no sign extension)
      dut.io.operandA.poke("hFFFFFFFF".U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(ALUOp.SRL)
      dut.io.aluResult.expect("h0FFFFFFF".U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 6: SLL (Shift Left Logical) Operation
// =============================================================================

// Test SLL operation
class ALUSllTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Sll_Tester" should "test SLL operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Shift by 0 (no change)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.SLL)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)

      // Test Case 2: Shift by 4
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(4.U)
      dut.io.operation.poke(ALUOp.SLL)
      dut.io.aluResult.expect("h23456780".U)
      dut.clock.step(1)

      // Test Case 3: Shift by 31 (maximum)
      dut.io.operandA.poke(1.U)
      dut.io.operandB.poke(31.U)
      dut.io.operation.poke(ALUOp.SLL)
      dut.io.aluResult.expect("h80000000".U)
      dut.clock.step(1)

      // Test Case 4: Only lower 5 bits used (shift by 32 = shift by 0)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(32.U)  // 32 & 0x1F = 0
      dut.io.operation.poke(ALUOp.SLL)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)

      // Test Case 5: Only lower 5 bits used (shift by 36 = shift by 4)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(36.U)  // 36 & 0x1F = 4
      dut.io.operation.poke(ALUOp.SLL)
      dut.io.aluResult.expect("h23456780".U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 5: XOR Operation
// =============================================================================

// Test XOR operation
class ALUXorTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Xor_Tester" should "test XOR operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Basic XOR
      dut.io.operandA.poke("hFF00FF00".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.operation.poke(ALUOp.XOR)
      dut.io.aluResult.expect("hF00FF00F".U)
      dut.clock.step(1)

      // Test Case 2: XOR with itself (should be zero)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke("h12345678".U)
      dut.io.operation.poke(ALUOp.XOR)
      dut.io.aluResult.expect(0.U)
      dut.clock.step(1)

      // Test Case 3: XOR with all ones (inverts)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(ALUOp.XOR)
      dut.io.aluResult.expect("hEDCBA987".U)
      dut.clock.step(1)

      // Test Case 4: XOR with zero (identity)
      dut.io.operandA.poke("hAAAAAAAA".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.XOR)
      dut.io.aluResult.expect("hAAAAAAAA".U)
      dut.clock.step(1)
    }
  }
}

// =============================================================================
// TDD CYCLE 4: OR Operation
// =============================================================================

// Test OR operation
class ALUOrTest extends AnyFlatSpec with ChiselScalatestTester {
  "ALU_Or_Tester" should "test OR operation" in {
    test(new ALU).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)

      // Test Case 1: Basic OR
      dut.io.operandA.poke("hFF00FF00".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.operation.poke(ALUOp.OR)
      dut.io.aluResult.expect("hFF0FFF0F".U)
      dut.clock.step(1)

      // Test Case 2: OR with zero (identity)
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke(0.U)
      dut.io.operation.poke(ALUOp.OR)
      dut.io.aluResult.expect("h12345678".U)
      dut.clock.step(1)

      // Test Case 3: OR with all ones
      dut.io.operandA.poke("h12345678".U)
      dut.io.operandB.poke("hFFFFFFFF".U)
      dut.io.operation.poke(ALUOp.OR)
      dut.io.aluResult.expect("hFFFFFFFF".U)
      dut.clock.step(1)

      // Test Case 4: OR with itself
      dut.io.operandA.poke("h55555555".U)
      dut.io.operandB.poke("h55555555".U)
      dut.io.operation.poke(ALUOp.OR)
      dut.io.aluResult.expect("h55555555".U)
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