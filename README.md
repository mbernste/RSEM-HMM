RSEM
====
This project implements a simplified version of the RNA-Seq Expectation Maximization algorithm
devised by Bo Li and Colin Dewey for estimating gene and
isophorm expression levels in RNA-Seq data.

The original paper can be found here: http://www.biomedcentral.com/1471-2105/12/323.  

A paper describing their implementation of the algorithm can
be found here: http://www.biomedcentral.com/1471-2105/12/323

Their implementation can be found on GitHub here: https://github.com/bli25wisc/RSEM

The goal of this project is to add the ability for the algorithm to make accurate estimates with the presence
of insertions and deletions in the RNA-Seq reads.  This is a more realistic 
assumption if the reference transcripts were taken from the cell of a different
organism than that which generated the reads.
