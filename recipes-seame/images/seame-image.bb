SUMMARY = "SEAME yocto basic image"

require recipes-core/images/core-image-base.bb

IMAGE_ROOTFS_EXTRA_SPACE = "5242880"

IMAGE_INSTALL:append = " \
						seame-second \
						"


