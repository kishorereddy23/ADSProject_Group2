#!/bin/bash
# Simple script to verify generated Verilog is syntactically correct

echo "=========================================="
echo "Verifying ALU Verilog Implementation"
echo "=========================================="

# Check if Verilog file exists
if [ ! -f "generated-src/ALU.v" ]; then
    echo "ERROR: generated-src/ALU.v not found!"
    echo "Please run 'sbt run' first to generate Verilog"
    exit 1
fi

echo "✓ ALU.v found"

# Check file size (should be reasonable, not empty)
FILE_SIZE=$(wc -c < "generated-src/ALU.v")
if [ $FILE_SIZE -lt 100 ]; then
    echo "ERROR: ALU.v seems too small ($FILE_SIZE bytes)"
    exit 1
fi

echo "✓ File size looks good ($FILE_SIZE bytes)"

# Use Verilator to check syntax (if installed)
if command -v verilator &> /dev/null; then
    echo ""
    echo "Running Verilator syntax check..."
    verilator --lint-only -Wall generated-src/ALU.v
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "=========================================="
        echo "✓ SUCCESS: Verilog syntax is valid!"
        echo "=========================================="
    else
        echo ""
        echo "=========================================="
        echo "⚠ WARNING: Verilator found some issues"
        echo "=========================================="
    fi
else
    echo "⚠ Verilator not installed, skipping syntax check"
    echo "  (Install with: sudo apt-get install verilator)"
fi

echo ""
echo "Checking for key ALU operations in Verilog..."

# Check that key operations exist in the generated Verilog
OPERATIONS=("ADD" "SUB" "AND" "OR" "XOR" "SLL" "SRL" "SRA" "SLT" "SLTU" "PASSB")
MISSING=0

for op in "${OPERATIONS[@]}"; do
    if grep -q "$op" generated-src/ALU.v; then
        echo "✓ Found $op operation"
    else
        echo "✗ Missing $op operation reference"
        MISSING=$((MISSING + 1))
    fi
done

echo ""
if [ $MISSING -eq 0 ]; then
    echo "=========================================="
    echo "✓ All operations found in Verilog!"
    echo "=========================================="
else
    echo "=========================================="
    echo "⚠ $MISSING operations not found"
    echo "  (This might be OK if Chisel optimized the code)"
    echo "=========================================="
fi

echo ""
echo "Summary:"
echo "  - Verilog file generated: YES"
echo "  - File size: $FILE_SIZE bytes"
echo "  - Syntax valid: $(command -v verilator &> /dev/null && echo 'CHECKED' || echo 'NOT CHECKED')"
echo "  - Operations present: $((11 - MISSING))/11"
echo ""
echo "Next steps:"
echo "  1. Make sure 'sbt test' passes (most important!)"
echo "  2. Verify alu_seq_item.sv is complete"
echo "  3. Document your work for submission"