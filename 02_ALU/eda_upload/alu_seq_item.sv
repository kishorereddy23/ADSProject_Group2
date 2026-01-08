// ADS I Class Project
// Assignment 02: Arithmetic Logic Unit and UVM Testbench
//
// Chair of Electronic Design Automation, RPTU University Kaiserslautern-Landau
// File created on 09/21/2025 by Tharindu Samarakoon (gug75kex@rptu.de)
// File updated on 10/31/2025 by Tobias Jauch (tobias.jauch@rptu.de)

`include "uvm_macros.svh"
import uvm_pkg::*;
import alu_tb_config_pkg::*;

class alu_seq_item extends uvm_sequence_item;

    // =============================================================================
    // Define the randomized fields for the ALU transaction
    // =============================================================================
    rand bit [31:0] operandA;      // First operand (randomized)
    rand bit [31:0] operandB;      // Second operand (randomized)
    rand ALUOp operation;          // ALU operation (randomized)
    bit [31:0] aluResult;          // Result from ALU (not randomized, filled by monitor)

    // =============================================================================
    // Register the class with UVM factory and enable field utilities
    // =============================================================================
    `uvm_object_utils_begin(alu_seq_item)
        `uvm_field_int(operandA, UVM_DEFAULT)
        `uvm_field_int(operandB, UVM_DEFAULT)
        `uvm_field_enum(ALUOp, operation, UVM_DEFAULT)
        `uvm_field_int(aluResult, UVM_DEFAULT)
    `uvm_object_utils_end

    // =============================================================================
    // Constraint: Restrict operation to valid ALU operations only
    // =============================================================================
    constraint aluOp_constraint {
        operation inside {ADD, SUB, AND, OR, XOR, SLL, SRL, SRA, SLT, SLTU, PASSB};
    }

    // =============================================================================
    // Convert to string function for debug printing
    // =============================================================================
    virtual function string convert2str();
        return $sformatf("operandA: 0x%0x, operandB: 0x%0x, operation: %0p, aluResult: 0x%0x", 
                         operandA, operandB, operation, aluResult);
    endfunction

    // =============================================================================
    // Constructor
    // =============================================================================
    function new(string name = "alu_seq_item"); 
        super.new(name);
    endfunction   

endclass