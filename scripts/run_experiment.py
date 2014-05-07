#!/usr/bin/env python

from optparse import OptionParser
from subprocess import call
import subprocess
import sys
import os

PROJECT_ROOT = "/Users/matthewbernstein/Development/School/Wisconsin/776_Advanced_Bioinformatics/rsem_indels/"
SCRIPT_ROOT = PROJECT_ROOT + "scripts"
EXP_NAME = sys.argv[1]
EXP_ROOT =  "./../experiments/" + EXP_NAME
EXP_READS = "./../experiments/" + EXP_NAME + "/sim_reads"
EXP_BOWTIE = "./../experiments/" + EXP_NAME + "/bowtie"
EXP_TRANS = "./../experiments/" + EXP_NAME + "/transcripts"
BOWTIE = "/Users/matthewbernstein/Development/bowtie2-2.2.1"
MOUSE_TRANS = "./../data/NM_refseq_ref.transcripts.fa"

# Create directory structure for the experiment
call(["mkdir", EXP_ROOT])
call(["mkdir", EXP_READS])
call(["mkdir", EXP_BOWTIE])

# Parse options
parser = OptionParser()
parser.add_option("-s", "--sample", type="int", help="Number of transcripts to sample from which to simulate the reads.")
parser.add_option("-b", "--build", action="store_true", help="Create new bowties alignment indices.")
parser.add_option("-m", "--simulate", type="int", help="Number of reads to simulate from the reference transcripts.")
parser.add_option("-i", "--indels", action="store_true", help="Simulate reads with indels.")
parser.add_option("-r", "--inserts", type="int", help="Maximum number of inserts per 100 bases")
parser.add_option("-d", "--deletes", type="int", help="Maximum number of deletes per 100 bases")
parser.add_option("-l", "--r_length", type="int", help="The length of the reads to be simulated.")
parser.add_option("-a", "--align", action="store_true", help="Create intitial bowtie alignments against the indices.")
parser.add_option("-p", "--preset", type="int", help="Choose preset bowtie alignment settings (1, 2, 3).")
parser.add_option("-e", "--rsem", action="store_true", help="Run the RSEM algorithm.")
parser.add_option("-o", "--hmm", action="store_true", help="Run RSEM HMM version.");
(options, args) = parser.parse_args()
print options

# The file where the reference transcripts are stored
tFile = MOUSE_TRANS
rLength = 25
preset = 1
indels = "false"
inserts = 1
deletes = 1

# Option to sample reference transcripts in order to simulate reads from smaller data
if options.__dict__['sample']:
	sample = options.__dict__['sample']
	call(["mkdir", EXP_TRANS])
	call(["java", "-Xms512M", "-Xmx1524M", "-jar", "./../jar/sample_transcripts.jar", \
		str(sample), MOUSE_TRANS, EXP_TRANS + "/" + EXP_NAME + "_ref.fa"])

# Option to build new bowtie indices
if options.__dict__['build']:
	if os.path.isdir(EXP_TRANS):
		tFile = "./../transcripts/" + EXP_NAME + "_ref.fa"
	os.chdir(EXP_BOWTIE)
	p = subprocess.Popen(["/Users/matthewbernstein/Development/bowtie2-2.2.1/bowtie2-build", tFile, EXP_NAME])
	p.wait()

# Option to set the length of the simulated reads
if options.__dict__['r_length']:
	rLength = options.__dict__['r_length']

# Option to set whether simulation should generate indels
if options.__dict__['indels']:
	indels = "true"
	if options.__dict__['inserts']:
		inserts = options.__dict__['inserts']
	if options.__dict__['deletes']:
		deletes = options.__dict__['deletes']
	

# Option to simulate reads
if options.__dict__['simulate']:
	numReads = options.__dict__['simulate']
	os.chdir(SCRIPT_ROOT)
	if os.path.isdir(EXP_TRANS):
                tFile = EXP_TRANS + "/" + EXP_NAME + "_ref.fa"
	
	call(["java", "-Xms512M", "-Xmx1524M", "-jar", "./../jar/simulate_reads.jar", \
                indels, str(inserts), str(deletes), str(rLength), str(numReads), tFile, \
		EXP_READS + "/" + EXP_NAME + "_reads.fa", EXP_READS + "/" + EXP_NAME + "_map.txt", \
		EXP_READS + "/" + EXP_NAME + "_exp.txt"])

# Option to set the bowtie alignment preset set of settings
if options.__dict__['preset']:
	preset = options.__dict__['preset']

# TODO Preset

# Option to perform initial bowtie alignment
if options.__dict__['align']:
	os.chdir(SCRIPT_ROOT)
	if os.path.isdir(EXP_TRANS):
		tFile = "./../transcripts/" + EXP_NAME + "_ref.fa"
		base = EXP_NAME
	else:
		base = "NM"
	
	# Run bowtie
	os.chdir(PROJECT_ROOT + "experiments/" + EXP_NAME + "/bowtie")
	p = subprocess.Popen(["/Users/matthewbernstein/Development/bowtie2-2.2.1/bowtie2", \
		"--local", "--ma", str(5), "--score-min", "G,1,4",  "-k", "20", "-x", base, "-f", "-U", PROJECT_ROOT + "experiments/" + EXP_NAME  + "/sim_reads/" + EXP_NAME + "_reads.fa", \
		"-S", PROJECT_ROOT + "experiments/" + EXP_NAME + "/sim_reads/" + EXP_NAME + "_align.txt"])

# Option to run the RSEM algorithm
if options.__dict__['rsem']:
	os.chdir(SCRIPT_ROOT)
	call(["java", "-Xms512M", "-Xmx1524M", "-jar", "./../jar/rsem.jar", \
                EXP_READS + "/" + EXP_NAME + "_reads.fa", \
		EXP_READS + "/" + EXP_NAME + "_map.txt", \
                EXP_TRANS + "/" + EXP_NAME + "_ref.fa", \
                EXP_READS + "/" + EXP_NAME + "_align.txt"])
	
