#!/usr/bin/env python

from optparse import OptionParser
from subprocess import call
import subprocess
import sys
import os

EXP_NAME = sys.argv[1]
EXP_ROOT =  "./../experiments/" + EXP_NAME
EXP_READS = "./../experiments/" + EXP_NAME + "/sim_reads"
EXP_BOWTIE = "./../experiments/" + EXP_NAME + "/bowtie"
EXP_TRANS = "./../experiments/" + EXP_NAME + "/transcripts"
MOUSE_TRANS = "./../data/NM_refseq_ref.transcripts.fa"
BOWTIE = "/Users/matthewbernstein/Development/bowtie2-2.2.1"

# Create directory structure for the experiment
call(["mkdir", EXP_ROOT])
call(["mkdir", EXP_READS])
call(["mkdir", EXP_BOWTIE])

# Parse options
parser = OptionParser()
parser.add_option("-s", "--sample", type="int", help="Number of transcripts to sample from which to simulate the reads.")
parser.add_option("-b", "--bowtie", action="store_true", help="Create new bowties alignment indices.")
(options, args) = parser.parse_args()
print options

# Option to sample reference transcripts in order to simulate reads from smaller data
if options.__dict__['sample']:
	sample = options.__dict__['sample']
	call(["mkdir", EXP_TRANS])
	call(["java", "-Xms512M", "-Xmx1524M", "-jar", "./../jar/sample_transcripts.jar", \
		str(sample), MOUSE_TRANS, EXP_TRANS + "/" + \
		EXP_NAME + "_ref.fa"])

# Option to create new bowtie indices
if options.__dict__['bowtie']:
	tFile = ""
	if os.path.isdir(EXP_TRANS):
		tFile = EXP_TRANS + "/" + EXP_NAME + "_ref.fa"
	else:
		tFile = MOUSE_TRANS
	p = subprocess.Popen(["/Users/matthewbernstein/Development/bowtie2-2.2.1/bowtie2-build", tFile, EXP_NAME])
	p.wait()
	call(["mv", "E1.1.bt2", EXP_BOWTIE])
