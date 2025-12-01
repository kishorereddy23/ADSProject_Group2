// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package readserial

import chisel3._
import chisel3.util._


/** controller class */
class Controller extends Module{
  
  val io = IO(new Bundle {
    val rxd    = Input(Bool())      // Serial input line
    val enable = Output(Bool())     // Enable signal for counter and shift register
    val count  = Input(UInt(4.W))   // Counter value from counter
    val valid  = Output(Bool())     // Valid output signal
  })

  // Internal state register
  // States: 0 = IDLE, 1 = RECEIVING
  val state = RegInit(0.U(1.W))
  
  // Register for valid signal to hold it for one cycle
  val validReg = RegInit(false.B)

  // Default outputs
  io.enable := false.B
  io.valid  := validReg

  // State machine
  when(state === 0.U) {
    // IDLE state - waiting for start bit (0)
    validReg := false.B
    when(io.rxd === false.B) {
      // Start bit detected, move to RECEIVING state
      state := 1.U
    }
  } .otherwise {
    // RECEIVING state - receiving data bits
    io.enable := true.B
    validReg := false.B  // Default to false
    
    when(io.count === 7.U) {
      // All 8 bits received (counter at 7 means 8th bit is being sampled)
      state := 0.U       // Go back to IDLE next cycle
      validReg := true.B // Signal that data is valid
    }
  }
}


/** counter class */
class Counter extends Module{
  
  val io = IO(new Bundle {
    val enable = Input(Bool())     // Enable counting
    val count  = Output(UInt(4.W)) // Current count value (0-7)
  })

  // Internal counter register (counts 0 to 7, then wraps)
  val count = RegInit(0.U(4.W))

  when(io.enable) {
    when(count === 7.U) {
      // Reached 7, reset to 0 on next enable
      count := 0.U
    } .otherwise {
      // Increment counter
      count := count + 1.U
    }
  } .otherwise {
    // Not enabled, reset counter
    count := 0.U
  }
  
  // Output current count
  io.count := count
}


/** shift register class */
class ShiftRegister extends Module{
  
  val io = IO(new Bundle {
    val rxd    = Input(Bool())       // Serial input
    val enable = Input(Bool())       // Enable shifting
    val data   = Output(UInt(8.W))   // Parallel output (8 bits)
  })

  // Internal 8-bit shift register
  val shiftReg = RegInit(0.U(8.W))

  when(io.enable) {
    // MSB comes first, so we shift left and insert at LSB
    shiftReg := Cat(shiftReg(6, 0), io.rxd)
  }

  io.data := shiftReg
}


/** 
  * The last warm-up task deals with a more complex component. Your goal is to design a serial receiver.
  * It scans an input line ("serial bus") named rxd for serial transmissions of data bytes. A transmission 
  * begins with a start bit '0' followed by 8 data bits. The most significant bit (MSB) is transmitted first. 
  * There is no parity bit and no stop bit. After the last data bit has been transferred a new transmission 
  * (beginning with a start bit, '0') may immediately follow. If there is no new transmission the bus line 
  * goes high ('1', this is considered the "idle" bus signal). In this case the receiver waits until the next 
  * transmission begins. The outputs of the design are an 8-bit parallel data signal and a valid signal. 
  * The valid signal goes high ('1') for one clock cycle after the last serial bit has been transmitted, 
  * indicating that a new data byte is ready.
  */
class ReadSerial extends Module{
  
  val io = IO(new Bundle {
    val rxd   = Input(Bool())       // Serial input line
    val data  = Output(UInt(8.W))   // Parallel data output
    val valid = Output(Bool())      // Valid signal
  })

  // Instantiate modules
  val controller = Module(new Controller())
  val counter    = Module(new Counter())
  val shiftReg   = Module(new ShiftRegister())

  // Connect controller
  controller.io.rxd   := io.rxd
  controller.io.count := counter.io.count

  // Connect counter
  counter.io.enable := controller.io.enable

  // Connect shift register  
  shiftReg.io.rxd    := io.rxd
  shiftReg.io.enable := controller.io.enable

  // Global outputs
  io.data  := shiftReg.io.data
  io.valid := controller.io.valid
}