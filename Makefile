#------------------------------------------------------------------------------
# $Id$
# $Source$
#------------------------------------------------------------------------------

VERSION=0.2.2.x

IMAGES= \
  images/back.png \
  images/button_cancel.png \
  images/filenew.png \
  images/fileopen.png \
  images/filesave2.png \
  images/forward.png \
  images/gear.png \
  images/gohome.png \
  images/next.png \
  images/openterm.png \
  images/player_back.png \
  images/player_end.png \
  images/player_fwd.png \
  images/player_next.png \
  images/player_rew.png \
  images/player_start.png \
  images/stop.png \
  images/wood.png

PACKAGES= \
  gmp \
  go \
  gtp \
  gui \
  sgf \
  utils

DOC= \
  doc/xml/analyze.xml \
  doc/xml/book.xml \
  doc/xml/compatibility.xml \
  doc/xml/gpl.xml \
  doc/xml/gtpshell.xml \
  doc/xml/interrupt.xml \
  doc/xml/invocation.xml \
  doc/xml/news.xml \
  doc/xml/programs.xml \
  doc/xml/readme.xml \
  doc/xml/reference-gmptogtp.xml \
  doc/xml/reference-gtpnet.xml \
  doc/xml/reference-netgtp.xml \
  doc/xml/reference-twogtp.xml \
  doc/xml/reference.xml \
  doc/xml/tools.xml

JAR= \
  gogui.jar \
  gmptogtp.jar \
  gtpnet.jar \
  netgtp.jar \
  sgftotex.jar \
  twogtp.jar

all: $(JAR)

gogui.jar: doc/html/index.html version $(shell cat build/files-gogui.txt)
	mkdir -p build/gogui
	javac -O -deprecation -sourcepath . -source 1.4 -d build/gogui @build/files-gogui.txt
	mkdir -p build/gogui/doc
	cp -R doc/html/* build/gogui/doc
	mkdir -p build/gogui/images
	for i in $(IMAGES); do cp $$i build/gogui/$$i; done 
	jar cmf build/manifest-gogui.txt gogui.jar -C build/gogui .

gmptogtp.jar: $(shell cat build/files-gmptogtp.txt)
	mkdir -p build/gmptogtp
	javac -O -deprecation -sourcepath . -source 1.4 -d build/gmptogtp @build/files-gmptogtp.txt
	jar cmf build/manifest-gmptogtp.txt gmptogtp.jar -C build/gmptogtp .

netgtp.jar: $(shell cat build/files-netgtp.txt)
	mkdir -p build/netgtp
	javac -O -deprecation -sourcepath . -source 1.4 -d build/netgtp @build/files-netgtp.txt
	jar cmf build/manifest-netgtp.txt netgtp.jar -C build/netgtp .

gtpnet.jar: $(shell cat build/files-gtpnet.txt)
	mkdir -p build/gtpnet
	javac -O -deprecation -sourcepath . -source 1.4 -d build/gtpnet @build/files-gtpnet.txt
	jar cmf build/manifest-gtpnet.txt gtpnet.jar -C build/gtpnet .

sgftotex.jar: $(shell cat build/files-sgftotex.txt)
	mkdir -p build/sgftotex
	javac -O -deprecation -sourcepath . -source 1.4 -d build/sgftotex @build/files-sgftotex.txt
	jar cmf build/manifest-sgftotex.txt sgftotex.jar -C build/sgftotex .

twogtp.jar: $(shell cat build/files-twogtp.txt)
	mkdir -p build/twogtp
	javac -O -deprecation -sourcepath . -source 1.4 -d build/twogtp @build/files-twogtp.txt
	jar cmf build/manifest-twogtp.txt twogtp.jar -C build/twogtp .

.PHONY: changelog clean gogui_debug srcdoc tags version

# Run with 'jdb -classpath build_dbg -sourcepath src GoGui'
gogui_debug: doc/html/index.html version
	mkdir -p build/gogui_debug
	javac -g -deprecation -sourcepath . -source 1.4 -d build/gogui_debug @build/files-gogui.txt
	mkdir -p build/gogui_debug/doc
	mkdir -p build/gogui_debug/images
	for i in $(IMAGES); do cp $$i build/gogui_debug/$$i; done 
	cp -R doc/html/* build/gogui_debug/doc

version:
	sed 's/m_version = \".*\"/m_version = \"$(VERSION)\"/' <src/gui/Version.java >src/gui/.Version.java.new
	mv src/gui/.Version.java.new src/gui/Version.java

clean:
	-rm -rf build/gogui build/gogui_debug build/gtpnet build/gmptogtp build/netgtp build/sgftotex build/twogtp $(JAR)

doc/html/index.html: $(DOC)
	echo "$(VERSION)" >doc/xml/version.xml
	$(MAKE) -C doc

docsrc:
	javadoc -sourcepath src -d docsrc -source 1.4 $(PACKAGES)

tags:
	etags `find . -name "*.java"`
