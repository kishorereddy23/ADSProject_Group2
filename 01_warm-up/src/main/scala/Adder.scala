// ADS I Class Project
// Chisel Introduction
//
// Chair of Electronic Design Automation, RPTU in Kaiserslautern
// File created on 18/10/2022 by Tobias Jauch (@tojauch)

package adder

import chisel3._
import chisel3.util._


/** 
  * Half Adder Class 
  * 
  * Your task is to implement a basic half adder as presented in the lecture.
  * Each signal should only be one bit wide (inputs and outputs).
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class HalfAdder extends Module{
  
  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a half adder as presented in the lecture
     */
    val a  = Input(UInt(1.W))   // First input bit
    val b  = Input(UInt(1.W))   // Second input bit
    val s  = Output(UInt(1.W))  // Sum output
    val c  = Output(UInt(1.W))  // Carry output
    })

  /* 
   * TODO: Describe output behaviour based on the input values
   */
  // Sum = A XOR B
  io.s := io.a ^ io.b

  // Carry = A AND B
  io.c := io.a & io.b

}

/** 
  * Full Adder Class 
  * 
  * Your task is to implement a basic full adder. The component's behaviour should 
  * match the characteristics presented in the lecture. In addition, you are only allowed 
  * to use two half adders (use the class that you already implemented) and basic logic 
  * operators (AND, OR, ...).
  * Each signal should only be one bit wide (inputs and outputs).
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class FullAdder extends Module{

  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a half adder as presented in the lecture
     */
    val a  = Input(UInt(1.W))   // First input bit
    val b  = Input(UInt(1.W))   // Second input bit
    val ci = Input(UInt(1.W))   // Carry input
    val s  = Output(UInt(1.W))  // Sum output
    val co = Output(UInt(1.W))  // Carry output
    })


  /* 
   * TODO: Instanciate the two half adders you want to use based on your HalfAdder class
   */
  val ha1 = Module(new HalfAdder())
  val ha2 = Module(new HalfAdder())


  /* 
   * TODO: Describe output behaviour based on the input values and the internal signals
   */
  // First half adder: Add a and b
  ha1.io.a := io.a
  ha1.io.b := io.b

  // Second half adder: Add result with carry-in
  ha2.io.a := ha1.io.s
  ha2.io.b := io.ci

  // Output connections
  io.s := ha2.io.s  // Final sum from second half adder
  
  // Carry out is OR of both carry outputs
  io.co := ha1.io.c | ha2.io.c

}

/** 
  * 4-bit Adder class 
  * 
  * Your task is to implement a 4-bit ripple-carry-adder. The component's behaviour should 
  * match the characteristics presented in the lecture.  Remember: An n-bit adder can be 
  * build using one half adder and n-1 full adders.
  * The inputs and the result should all be 4-bit wide, the carry-out only needs one bit.
  * There should be no delay between input and output signals, we want to have
  * a combinational behaviour of the component.
  */
class FourBitAdder extends Module{

  val io = IO(new Bundle {
    /* 
     * TODO: Define IO ports of a 4-bit ripple-carry-adder as presented in the lecture
     */
    val a  = Input(UInt(4.W))   // First 4-bit operand
    val b  = Input(UInt(4.W))   // Second 4-bit operand
    val s  = Output(UInt(4.W))  // 4-bit sum output
    val co = Output(UInt(1.W))  // Final carry output
    })

  /* 
   * TODO: Instanciate the full adders and one half adderbased on the previously defined classes
   */

  val ha = Module(new HalfAdder())
  val fa1 = Module(new FullAdder())
  val fa2 = Module(new FullAdder())
  val fa3 = Module(new FullAdder())


  /* 
   * TODO: Describe output behaviour based on the input values and the internal 
   */
  
  // Internal carry wires
  val c0 = Wire(UInt(1.W))
  val c1 = Wire(UInt(1.W))
  val c2 = Wire(UInt(1.W))

  // Bit 0 (LSB): Half Adder
  ha.io.a := io.a(0)
  ha.io.b := io.b(0)
  c0 := ha.io.c

  // Bit 1: Full Adder
  fa1.io.a  := io.a(1)
  fa1.io.b  := io.b(1)
  fa1.io.ci := c0
  c1 := fa1.io.co

  // Bit 2: Full Adder
  fa2.io.a  := io.a(2)
  fa2.io.b  := io.b(2)
  fa2.io.ci := c1
  c2 := fa2.io.co

  // Bit 3 (MSB): Full Adder
  fa3.io.a  := io.a(3)
  fa3.io.b  := io.b(3)
  fa3.io.ci := c2

  // Concatenate all sum bits from MSB to LSB
  io.s := Cat(fa3.io.s, fa2.io.s, fa1.io.s, ha.io.s)
  
  // Final carry out from MSB
  io.co := fa3.io.co
}
