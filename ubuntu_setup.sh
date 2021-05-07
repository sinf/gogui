#!/bin/bash
set -e
set -x

if which apt; then
	sudo apt install ant xsltproc docbook-xsl inkscape
fi

if [ ! -f /tmp/Quaqua/dist/quaqua.jar ]; then
	cd /tmp
	wget https://www.randelshofer.ch/quaqua/files/quaqua-8.0.nested.zip
	unzip quaqua-8.0.nested.zip
	unzip quaqua-8.0.zip
	cd -
fi

mkdir -p lib
cd lib
cp /tmp/Quaqua/dist/quaqua.jar .
cd -

cd src/net/sf/gogui/images
./svg.sh
cd -

ant
