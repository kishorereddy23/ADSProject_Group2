xsim {top} -testplusarg UVM_TESTNAME=alu_test -testplusarg UVM_VERBOSITY=UVM_FULL -wdb {alu_dump.wdb} -cov_db_dir ./cov_db -cov_db_name alu_cov -autoloadwcfg -runall
