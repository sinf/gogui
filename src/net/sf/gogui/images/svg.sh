#!/bin/bash

conv() {
	out="$(basename $1 .svg)-${2}x${2}.png"
	echo "$out"
	if [ ! -f "$out" ]; then
		inkscape -o "$out" --export-type=png -h "$2" -w "$2" "$1"
	fi
}

for i in 16 24 32 48 64
do
	for file in *.svg
	do
		echo "conversion svg> pgn de " $file
		conv "$file" $i
	done
done

for file in "gogui-black.svg" "gogui-white.svg" "gogui-setup.svg"
do
	conv "$file" 8
done
