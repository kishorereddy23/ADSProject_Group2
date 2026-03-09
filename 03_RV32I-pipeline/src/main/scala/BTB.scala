package core_tile

import chisel3._
import chisel3.util._

class BTB extends Module {
  val io = IO(new Bundle {
    // Lookup (IF stage)
    val PC           = Input(UInt(32.W))
    val valid        = Output(Bool())
    val target       = Output(UInt(32.W))
    val predictTaken = Output(Bool())

    // Update (EX stage)
    val update       = Input(Bool())
    val updatePC     = Input(UInt(32.W))
    val updateTarget = Input(UInt(32.W))
    val mispredicted = Input(Bool())
    val actualTaken  = Input(Bool())
  })

  val NUM_SETS = 8
  val NUM_WAYS = 2
  val TAG_BITS = 27

  // ── FSM state encoding ────────────────────────────────────────────
  // CRITICAL: Use 0 = WEAK_TAKEN so RegInit(0) == weakTaken (spec default)
  // This is the ONLY way to make Chisel RegInit work correctly for Vec of Vec
  val WEAK_TAKEN       = 0.U(2.W)  // 00 — initial state (RegInit resets to 0)
  val STRONG_TAKEN     = 1.U(2.W)  // 01
  val WEAK_NOT_TAKEN   = 2.U(2.W)  // 10
  val STRONG_NOT_TAKEN = 3.U(2.W)  // 11

  // ── Storage ───────────────────────────────────────────────────────
  // RegInit(0) on predState means every entry starts at WEAK_TAKEN
  val validBits   = RegInit(VecInit(Seq.fill(NUM_SETS)(VecInit(Seq.fill(NUM_WAYS)(false.B)))))
  val tagStore    = RegInit(VecInit(Seq.fill(NUM_SETS)(VecInit(Seq.fill(NUM_WAYS)(0.U(TAG_BITS.W))))))
  val targetStore = RegInit(VecInit(Seq.fill(NUM_SETS)(VecInit(Seq.fill(NUM_WAYS)(0.U(32.W))))))
  val predState   = RegInit(VecInit(Seq.fill(NUM_SETS)(VecInit(Seq.fill(NUM_WAYS)(WEAK_TAKEN)))))
  val lruBit      = RegInit(VecInit(Seq.fill(NUM_SETS)(false.B)))

  def getIdx(pc: UInt): UInt = pc(4, 2)
  def getTag(pc: UInt): UInt = pc(31, 5)

  // ── Lookup (combinational, uses current PC) ───────────────────────
  val lkpIdx  = getIdx(io.PC)
  val lkpTag  = getTag(io.PC)
  val way0Hit = validBits(lkpIdx)(0) && (tagStore(lkpIdx)(0) === lkpTag)
  val way1Hit = validBits(lkpIdx)(1) && (tagStore(lkpIdx)(1) === lkpTag)
  val anyHit  = way0Hit || way1Hit
  val hitWay  = Mux(way0Hit, 0.U(1.W), 1.U(1.W))

  val hitPredictor = Mux(way0Hit, predState(lkpIdx)(0),   predState(lkpIdx)(1))
  val hitTarget    = Mux(way0Hit, targetStore(lkpIdx)(0), targetStore(lkpIdx)(1))

  io.valid        := anyHit
  io.target       := Mux(anyHit, hitTarget, 0.U)
  // predictTaken is true when state is WEAK_TAKEN or STRONG_TAKEN
  io.predictTaken := anyHit && (hitPredictor === WEAK_TAKEN || hitPredictor === STRONG_TAKEN)

  // ── 2-bit saturating FSM ──────────────────────────────────────────
  // taken=true  → move toward STRONG_TAKEN
  // taken=false → move toward STRONG_NOT_TAKEN
  def fsmNext(state: UInt, taken: Bool): UInt = {
    Mux(taken,
      MuxCase(STRONG_TAKEN, Seq(
        (state === STRONG_NOT_TAKEN) -> WEAK_NOT_TAKEN,
        (state === WEAK_NOT_TAKEN)   -> WEAK_TAKEN,
        (state === WEAK_TAKEN)       -> STRONG_TAKEN,
        (state === STRONG_TAKEN)     -> STRONG_TAKEN
      )),
      MuxCase(STRONG_NOT_TAKEN, Seq(
        (state === STRONG_TAKEN)     -> WEAK_TAKEN,
        (state === WEAK_TAKEN)       -> WEAK_NOT_TAKEN,
        (state === WEAK_NOT_TAKEN)   -> STRONG_NOT_TAKEN,
        (state === STRONG_NOT_TAKEN) -> STRONG_NOT_TAKEN
      ))
    )
  }

  // ── Update (registered, fires when EX stage resolves a branch) ────
  val updIdx    = getIdx(io.updatePC)
  val updTag    = getTag(io.updatePC)
  val updW0Hit  = validBits(updIdx)(0) && (tagStore(updIdx)(0) === updTag)
  val updW1Hit  = validBits(updIdx)(1) && (tagStore(updIdx)(1) === updTag)
  val updAnyHit = updW0Hit || updW1Hit
  val updHitWay = Mux(updW0Hit, 0.U(1.W), 1.U(1.W))

  val way0Empty = !validBits(updIdx)(0)
  val way1Empty = !validBits(updIdx)(1)
  // LRU eviction: lruBit=false means way0 is LRU; lruBit=true means way1 is LRU
  val evictWay  = Mux(way0Empty,       0.U(1.W),
                  Mux(way1Empty,       1.U(1.W),
                  Mux(!lruBit(updIdx), 0.U(1.W),
                                       1.U(1.W))))
  val writeWay  = Mux(updAnyHit, updHitWay, evictWay)

  when(io.update) {
    when(updAnyHit) {
      // Existing entry: update target and advance FSM
      targetStore(updIdx)(updHitWay) := io.updateTarget
      predState(updIdx)(updHitWay)   := fsmNext(predState(updIdx)(updHitWay), io.actualTaken)
      lruBit(updIdx)                 := (updHitWay === 0.U)
    }.otherwise {
      // New entry: allocate slot, initialise FSM to WEAK_TAKEN (= 0)
      validBits(updIdx)(evictWay)   := true.B
      tagStore(updIdx)(evictWay)    := updTag
      targetStore(updIdx)(evictWay) := io.updateTarget
      predState(updIdx)(evictWay)   := WEAK_TAKEN
      lruBit(updIdx)                := (evictWay === 0.U)
    }
  }

  // Update LRU on lookup hit (when no update happening same cycle)
  when(anyHit && !io.update) {
    lruBit(lkpIdx) := (hitWay === 0.U)
  }

  // ── Debug printf ──────────────────────────────────────────────────
  when(io.update) {
    when(updAnyHit) {
      printf(p"[BTB] UPDATE-HIT  idx=${updIdx} way=${updHitWay} tag=0x${Hexadecimal(updTag)} " +
             p"actualTaken=${io.actualTaken} mispredicted=${io.mispredicted}\n")
    }.otherwise {
      printf(p"[BTB] UPDATE-NEW  idx=${updIdx} way=${evictWay} tag=0x${Hexadecimal(updTag)} " +
             p"target=0x${Hexadecimal(io.updateTarget)} actualTaken=${io.actualTaken}\n")
    }
  }
}