#!/bin/bash

. ./msp430init.bash

./gen-REGREG.bash add 4 4 0 4 8 0 0 0 0
./gen-IMMREG.bash add 4 4 0 8 0 0 0 0
./gen-ABSREG.bash add 0x400 4 4 0 8 0 0 0 0
./gen-ABSREG.bash mov 0x400 2 0 0 2 0 0 0 0
