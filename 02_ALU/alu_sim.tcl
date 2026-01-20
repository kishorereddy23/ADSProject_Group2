# -------------------------------
# alu_sim.tcl - UVM Testbench run with coverage
# -------------------------------

# Create build folder if not exist and move into it
file mkdir build
cd build

<<<<<<< HEAD
# -------------------------------
# Compile SystemVerilog and Verilog files listed in compile_list.f with UVM library
# -------------------------------
exec xvlog -sv -f ../src/test/uvm/compile_list.f -L uvm -define SIM_TIMEOUT=50000

# -------------------------------
# Elaborate the design with snapshot name "top" and relaxed checks
# Enable coverage collection with --cov_db_dir and --cov_db_name
# -------------------------------
exec xelab alu_tb -relax -s top -timescale 1ns/1ps -debug typical \
    --cov_db_dir ./cov_db --cov_db_name alu_cov

# -------------------------------
# Run the simulation with UVM testbench
# -------------------------------
exec xsim top -R -log xsim.log \
=======
exec xelab alu_tb -relax -L uvm -s top -timescale 1ns/1ps -v 2
exec xsim top \
>>>>>>> upstream/main
    -testplusarg UVM_TESTNAME=alu_test \
    -testplusarg UVM_VERBOSITY=UVM_FULL \
    -wdb alu_dump.wdb \
    --cov_db_dir ./cov_db --cov_db_name alu_cov


puts "UVM simulation with coverage completed successfully!"
puts "Coverage database is in ./cov_db/xsim.covdb/alu_cov/"
puts "Use the GUI or Tcl commands inside xsim to generate text/HTML reports."
