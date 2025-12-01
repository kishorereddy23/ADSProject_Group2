// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec


/** 
  * Read serial tester
  * Tests the serial receiver with various data patterns
  */
class ReadSerialTester extends AnyFlatSpec with ChiselScalatestTester {

  "ReadSerial" should "work" in {
    test(new ReadSerial).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        // Test 1: Transmit 0xFF (11111111)
        println("Test 1: Sending 0xFF")
        
        // Initial state: bus is idle (high)
        dut.io.rxd.poke(true.B)
        dut.io.valid.expect(false.B)
        dut.clock.step(1)
        
        // Start bit: 0
        dut.io.rxd.poke(false.B)
        dut.clock.step(1)
        
        // Data bits for 0xFF: 11111111 (MSB first)
        // Bit 7
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        // Bit 6
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        // Bit 5
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        // Bit 4
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        // Bit 3
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        // Bit 2
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        // Bit 1
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        // Bit 0 (last bit)
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        
        // NOW check - valid should be high after last bit is clocked in
        dut.io.valid.expect(true.B)
        dut.io.data.expect(0xFF.U)
        
        // Go back to idle
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        dut.io.valid.expect(false.B)
        
        
        // Test 2: Transmit 0xA5 (10100101)
        println("Test 2: Sending 0xA5")
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        
        // Start bit
        dut.io.rxd.poke(false.B)
        dut.clock.step(1)
        
        // 0xA5 = 10100101 (MSB first)
        dut.io.rxd.poke(true.B)   // Bit 7 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 6 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 5 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 4 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 3 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 2 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 1 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 0 = 1
        dut.clock.step(1)
        
        // Check output
        dut.io.valid.expect(true.B)
        dut.io.data.expect(0xA5.U)
        
        // Return to idle
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        dut.io.valid.expect(false.B)
        
        
        // Test 3: Transmit 0x00 (00000000)
        println("Test 3: Sending 0x00")
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        
        // Start bit
        dut.io.rxd.poke(false.B)
        dut.clock.step(1)
        
        // 0x00 = 00000000
        for(i <- 0 until 8) {
          dut.io.rxd.poke(false.B)
          dut.clock.step(1)
        }
        
        // Check output
        dut.io.valid.expect(true.B)
        dut.io.data.expect(0x00.U)
        
        // Return to idle
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        dut.io.valid.expect(false.B)
        
        
        // Test 4: Back-to-back transmissions
        println("Test 4: Back-to-back 0x12 and 0x34")
        dut.io.rxd.poke(true.B)
        dut.clock.step(1)
        
        // First byte: 0x12 (00010010)
        dut.io.rxd.poke(false.B)  // Start bit
        dut.clock.step(1)
        
        // 0x12 = 00010010
        dut.io.rxd.poke(false.B)  // Bit 7 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 6 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 5 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 4 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 3 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 2 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 1 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 0 = 0
        dut.clock.step(1)
        
        dut.io.valid.expect(true.B)
        dut.io.data.expect(0x12.U)
        
        // Immediately start second byte: 0x34 (no idle)
        dut.io.rxd.poke(false.B)  // Start bit
        dut.clock.step(1)
        dut.io.valid.expect(false.B)  // Valid should be low now
        
        // 0x34 = 00110100
        dut.io.rxd.poke(false.B)  // Bit 7 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 6 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 5 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 4 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 3 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(true.B)   // Bit 2 = 1
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 1 = 0
        dut.clock.step(1)
        dut.io.rxd.poke(false.B)  // Bit 0 = 0
        dut.clock.step(1)
        
        dut.io.valid.expect(true.B)
        dut.io.data.expect(0x34.U)
        
        println("All Serial Receiver tests passed!")
    }
  } 
}