#!/opt/homebrew/bin/bash

TOT=0; for arf in $(cat input.txt | sed -e 's/^$/X/g' ; echo 'X' );do [ "xX" = "x$arf" ] && echo $TOT && TOT=0 || TOT=$((TOT+arf));done | sort -nr | head -3 | xargs | sed -e 's/ /+/g' | bc
