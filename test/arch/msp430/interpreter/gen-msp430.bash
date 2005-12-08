#!/bin/bash

. ./msp430init.bash

./gen-REGREG.bash add 4 4 0 4 8 0 0 0 0
./gen-IMMREG.bash add 4 4 0 8 0 0 0 0
