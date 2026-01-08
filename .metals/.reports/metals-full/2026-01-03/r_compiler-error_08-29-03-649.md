error id: 2231967A81E4FA8A9DD04E7BCC30AB2E
file://<WORKSPACE>/02_ALU/src/main/scala/ALU.scala
### java.util.NoSuchElementException: head of empty String

occurred in the presentation compiler.



action parameters:
offset: 455
uri: file://<WORKSPACE>/02_ALU/src/main/scala/ALU.scala
text:
```scala
// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU University Kaiserslautern-Landau

package Assignment02

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// Define the ALU Operation Enum
object ALUOp extends ChiselEnum {
  val ADD   = Value(0x0.U)
  val SUB   = Value(0x1.U)
  val AND   = Value(0x2.U)
  val OR    = Value(0x3.U)
  val XOR   = @@Value(0x4.U)
  val SLL   = Value(0x5.U)
  val SRL   = Value(0x6.U)
  val SRA   = Value(0x7.U)
  val SLT   = Value(0x8.U)
  val SLTU  = Value(0x9.U)
  val PASSB = Value(0xA.U)
}

class ALU extends Module {
  
  val io = IO(new Bundle {
    val operandA  = Input(UInt(32.W))
    val operandB  = Input(UInt(32.W))
    val operation = Input(ALUOp())
    val aluResult = Output(UInt(32.W))
  })

  // Default: output zero (all tests will fail initially)
  io.aluResult := 0.U

  // =============================================================================
  // TDD CYCLE 1: ADD Operation Implementation
  // =============================================================================
  switch(io.operation) {
    is(ALUOp.ADD) {
      // Addition with wraparound (modulo 2^32)
      io.aluResult := io.operandA + io.operandB
    }
    // =============================================================================
    // TDD CYCLE 2: SUB Operation Implementation
    // =============================================================================
    is(ALUOp.SUB) {
      // Subtraction with wraparound
      io.aluResult := io.operandA - io.operandB
    }
    // =============================================================================
    // TDD CYCLE 3: AND Operation Implementation
    // =============================================================================
    is(ALUOp.AND) {
      // Bitwise AND
      io.aluResult := io.operandA & io.operandB
    }
    // =============================================================================
    // TDD CYCLE 4: OR Operation Implementation
    // =============================================================================
    is(ALUOp.OR) {
      // Bitwise OR
      io.aluResult := io.operandA | io.operandB
    }
    // =============================================================================
    // TDD CYCLE 5: XOR Operation Implementation
    // =============================================================================
    is(ALUOp.XOR) {
      // Bitwise XOR
      io.aluResult := io.operandA ^ io.operandB
    }
    // =============================================================================
    // TDD CYCLE 6: SLL (Shift Left Logical) Operation Implementation
    // =============================================================================
    is(ALUOp.SLL) {
      // Shift Left Logical - use only lower 5 bits of operandB
      io.aluResult := io.operandA << io.operandB(4, 0)
    }
  }

}
```


presentation compiler configuration:
Scala version: 2.13.18
Classpath:
<HOME>/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.13.18/scala-library-2.13.18.jar [exists ]
Options:





#### Error stacktrace:

```
scala.collection.StringOps$.head$extension(StringOps.scala:1124)
	scala.meta.internal.metals.ClassfileComparator.compare(ClassfileComparator.scala:30)
	scala.meta.internal.metals.ClassfileComparator.compare(ClassfileComparator.scala:3)
	java.base/java.util.PriorityQueue.siftUpUsingComparator(PriorityQueue.java:660)
	java.base/java.util.PriorityQueue.siftUp(PriorityQueue.java:637)
	java.base/java.util.PriorityQueue.offer(PriorityQueue.java:330)
	java.base/java.util.PriorityQueue.add(PriorityQueue.java:311)
	scala.meta.internal.metals.ClasspathSearch.$anonfun$search$3(ClasspathSearch.scala:32)
	scala.meta.internal.metals.ClasspathSearch.$anonfun$search$3$adapted(ClasspathSearch.scala:26)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.ClasspathSearch.search(ClasspathSearch.scala:26)
	scala.meta.internal.metals.WorkspaceSymbolProvider.search(WorkspaceSymbolProvider.scala:107)
	scala.meta.internal.metals.MetalsSymbolSearch.search$1(MetalsSymbolSearch.scala:114)
	scala.meta.internal.metals.MetalsSymbolSearch.search(MetalsSymbolSearch.scala:118)
	scala.meta.internal.pc.AutoImportsProvider.autoImports(AutoImportsProvider.scala:58)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$autoImports$1(ScalaPresentationCompiler.scala:399)
	scala.meta.internal.pc.CompilerAccess.withSharedCompiler(CompilerAccess.scala:148)
	scala.meta.internal.pc.CompilerAccess.$anonfun$withInterruptableCompiler$1(CompilerAccess.scala:92)
	scala.meta.internal.pc.CompilerAccess.$anonfun$onCompilerJobQueue$1(CompilerAccess.scala:209)
	scala.meta.internal.pc.CompilerJobQueue$Job.run(CompilerJobQueue.scala:152)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	java.base/java.lang.Thread.run(Thread.java:840)
```
#### Short summary: 

java.util.NoSuchElementException: head of empty String