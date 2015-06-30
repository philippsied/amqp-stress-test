#!/bin/bash

# Check for valid call
if [ "$#" -ne 3 ]; then
   echo "Usage: $0 <nameOfMeasure> <pathToServerStats>.csv <pathToScenario>.json"
   exit 1
fi

target="measure.json"
tmp="tmpfile"
name=$1
fileToConvert=$2
scenario=$3

# Set float digit to '.'
numericbkp=$LC_NUMERIC
export LC_NUMERIC="en_US.UTF-8"


## Skip first line with column names and second line with measured value (falsified by start)  and extract the cpu, used RAM, net io(rx), net io(tx) and the free hdd
tail -n +3 "$fileToConvert" | awk -F "," '{print $110 "," $90 "," $27 "," $24 "," $83 "," NF }' > "$tmp"


## Start creating target json

printf '{\n  "name":"%s",\n  "server":{\n    "samples":[' "$name" > "$target"
entryprefix=""

## iterate over lines of csv
	# Save old separator and set new separator
	OLDIFS=$IFS
	IFS=","
	time=0
	i=0
	sumcpu=0.0
	sumnetrx=0
	sumnettx=0

	# extract for each line
	while read cpu memused netrx nettx hddfree count
	 do
	  if [ "$count" -ne 112 ]; then
	   let time+=5000
	   continue
	  fi
	  # check for empty values -> treated as zero

	  
	  # mem, hdd value is given in bytes -> change to MB
	  let memused/=1048576
	  let hddfree/=1048576
	  
	  # net is given in bits/sec -> change to kbit/s
	  let netrx/=1024
	  let nettx/=1024
	  
	  # print out entry
	  printf "$entryprefix\n     {\n" >> "$target"
	  printf '      "elapsed":%u,\n' "$time" >> "$target"
	  printf '      "cpu-load":%03.1f,\n' "$cpu" >> "$target"
	  printf '      "memused-MB":%u,\n' $memused >> "$target"
	  printf '      "net-rate-rx-Kbps":%u,\n' $netrx >> "$target"
	  printf '      "net-rate-tx-Kbps":%u,\n' $nettx >> "$target"
	  printf '      "hddfree-MB":%u\n' $hddfree >> "$target"
	  printf '     }' >> "$target"
	  
	  # adjust counter, sum up for avg calculation
	  let time+=5000
	  sumcpu=`bc <<< "scale=1; $sumcpu + $cpu"`
	  let sumnetrx+=$netrx
	  let sumnettx+=$nettx
	  let i++
	  entryprefix=","
	 done < "$tmp"
	IFS=$OLDIFS

## add average values
printf '],\n' >> "$target"
printf '    "avg-cpu-load":%02.1f,\n' `bc <<< "scale=1; $sumcpu/$i"` >> "$target"
printf '    "avg-net-rate-rx-Kbps":%02.1f,\n' `bc <<< "scale=1; $sumnetrx/$i"` >> "$target"
printf '    "avg-net-rate-rx-Kbps":%02.1f\n' `bc <<< "scale=1; $sumnettx/$i"` >> "$target"
echo -e '   },' >> "$target"

cat $scenario | tail -n +2 | head -n -1 >> "$target"

echo -e '}' >> "$target"

# restore float digit
export LC_NUMERIC=$numericbkp


rm "$tmp"



