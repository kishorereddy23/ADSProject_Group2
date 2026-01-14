`ifndef ALU_SEQ_ITEM_SV
`define ALU_SEQ_ITEM_SV

`include "uvm_macros.svh"
import uvm_pkg::*;

class alu_seq_item extends uvm_sequence_item;

  rand logic [31:0] operandA;
  rand logic [31:0] operandB;
  rand alu_tb_config_pkg::ALUOp operation;

  logic [31:0] aluResult;

  constraint aluOp_constraint {
    operation inside {
      alu_tb_config_pkg::ADD,
      alu_tb_config_pkg::SUB,
      alu_tb_config_pkg::AND,
      alu_tb_config_pkg::OR,
      alu_tb_config_pkg::XOR,
      alu_tb_config_pkg::SLL,
      alu_tb_config_pkg::SRL,
      alu_tb_config_pkg::SRA,
      alu_tb_config_pkg::SLT,   // Added
      alu_tb_config_pkg::SLTU,  // Added
      alu_tb_config_pkg::PASSB  // Added
    };
  }

  `uvm_object_utils_begin(alu_seq_item)
    `uvm_field_int (operandA,  UVM_ALL_ON)
    `uvm_field_int (operandB,  UVM_ALL_ON)
    `uvm_field_enum(alu_tb_config_pkg::ALUOp, operation, UVM_ALL_ON)
    `uvm_field_int (aluResult, UVM_ALL_ON)
  `uvm_object_utils_end

  function new(string name = "alu_seq_item");
    super.new(name);
  endfunction

  virtual function string convert2str();
    return $sformatf(
      "operandA: 0x%08h, operandB: 0x%08h, operation: %s, aluResult: 0x%08h",
      operandA, operandB, operation.name(), aluResult
    );
  endfunction

endclass

`endif
