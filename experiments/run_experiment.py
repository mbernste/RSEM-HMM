from subprocess import call
import sys

EXP_NAME = sys.argv[0]
SIMULATE = "./../scripts/simulate"
SIM_READS_DESTINATION = "./../data/sim_reads/"

call([SIMULATE, SIM_READS_DESTINATION + EXP_NAME + ".fa"])
