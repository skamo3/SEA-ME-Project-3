DESCRIPTION = "SEA:ME second git repository bb file"

LICENSE = "CLOSED"

DEPENDS += "qtbase qtquickcontrols2"

inherit qmake5

IMAGE_INSTALL:append = " \
						dbus \
						can-utils \
						libsocketcan \
						"

SRC_URI = "git://github.com/skamo3/SEA-ME-Project-2.git;protocol=https;branch=main"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/git"

do_configure () {

	mkdir -p ${S}/builds/DICApp
	cd ${S}/builds/DICApp
	qmake ${S}/RpiApplications/DICApp/DigitalInstrumentCluster
	
	mkdir -p ${S}/builds/CanReceiver
	cd ${S}/builds/CanReceiver
	qmake ${S}/RpiApplications/CanReceiver/CanReceiver

	mkdir -p ${S}/builds/ServerApp
	cd ${S}/builds/ServerApp
	qmake ${S}/RpiApplications/Server/ServerApp

	
}

do_compile() {
	cd ${S}/builds/DICApp/
	make

	cd ${S}/builds/CanReceiver/
	make

	cd ${S}/builds/ServerApp/
	make
	
}

do_install() {
#	install -d ${D}${bindir}/excutable
#	install -m 0755 ${S}/builds/DICApp/DigitalInstrumentCluster ${D}${bindir}/excutable
#	install -m 0755 ${S}/builds/CanReceiver/CanReceiver ${D}${bindir}/excutable
#	install -m 0755 ${S}/builds/ServerApp/ServerApp ${D}${bindir}/excutable

	install -d ${D}/home/root/excutable
	install -m 0755 ${S}/builds/DICApp/DigitalInstrumentCluster ${D}/home/root/excutable
	install -m 0755 ${S}/builds/CanReceiver/CanReceiver ${D}/home/root/excutable
	install -m 0755 ${S}/builds/ServerApp/ServerApp ${D}/home/root/excutable
}

FILES:${PN} += " \
		/home/root/ \
		"



