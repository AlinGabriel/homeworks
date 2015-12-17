#!/bin/bash
qsub -cwd -sync Y -S /bin/bash -q ibm-quad.q _script_tema2.sh	
cat result
