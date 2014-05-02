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
parser.add_option("-l", "--r_length", type="int", help="The length of the reads to be simulated.")
parser.add_option("-a", "--align", action="store_true", help="Create intitial bowtie alignments against the indices.")
parser.add_option("-p", "--preset", type="int", help="Choose preset bowtie alignment settings (1, 2, 3).")
(options, args) = parser.parse_args()
print options

# The file where the reference transcripts are stored
tFile = MOUSE_TRANS
rLength = 25
preset = 1

# Option to sample reference transcripts in order to simulate reads from smaller data
if options.__dict__['sample']:
	sample = options.__dict__['sample']
	call(["mkdir", EXP_TRANS])
	call(["java", "-Xms512M", "-Xmx1524M", "-jar", "./../jar/sample_transcripts.jar", \
		str(sample), MOUSE_TRANS, EXP_TRANS + "/" + \
		EXP_NAME + "_ref.fa"])

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

# Option to simulate reads
if options.__dict__['simulate']:
	numReads = options.__dict__['simulate']
	os.chdir(SCRIPT_ROOT)
	if os.path.isdir(EXP_TRANS):
                tFile = EXP_TRANS + "/" + EXP_NAME + "_ref.fa"
	
	call(["java", "-Xms512M", "-Xmx1524M", "-jar", "./../jar/simulate_reads.jar", \
                str(rLength), str(numReads), tFile, EXP_READS + "/" + EXP_NAME + "_reads.fa", \
		 EXP_READS + "/" + EXP_NAME + "_map.txt", EXP_READS + "/" + EXP_NAME + "_exp.txt"])

# Option to set the bowtie alignment preset set of settings
if options.__dict__['preset']:
	preset = options.__dict__['preset']

# Set the settings for the bowtie aligner
if preset == 1:
	numMatches = 100

# Option to perform initial bowtie alignment
if options.__dict__['align']:
	if os.path.isdir(EXP_TRANS):
		tFile = "./../transcripts/" + EXP_NAME + "_ref.fa"
		base = EXP_NAME
	else:
		base = "NM"
	
	# Convert
	readsPath = PROJECT_ROOT + "experiments/" + EXP_NAME  + "/sim_reads/" + EXP_NAME + "_reads.fa"
	print readsPath
	convertPath = SCRIPT_ROOT + "/fasta_to_fastq.pl"
	p = subprocess.Popen(["perl", convertPath, readsPath], \
		stdout=subprocess.PIPE, stderr=subprocess.PIPE)
	p.wait()
	(out, err) = p.communicate()
	f = open(EXP_READS + "/" + EXP_NAME + "_reads.fq", 'w+')
	f.write(out)
	f.close()
	print out

	# Run bowtie
	#p = subprocess.Popen(["/Users/matthewbernstein/Development/bowtie2-2.2.1/bowtie2", \
	#	"--local", "-k", str(numMatches), "-x", base, -U ./../../../output/out_small_25.fq -S bowtie_small_25.txt])

	
