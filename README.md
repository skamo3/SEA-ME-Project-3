SEA:ME 3rd Project  
> Before start
> - This repository follows the previous project of SEA:ME.
> - This repository describes how to create Yocto custom images.
> - Please refer to the official website for more information.

# Table of Contents

# Basics of Yocto
- On the official website of Yocto explained as follows,  ```The Yocto Project (YP) is an open-source collaboration project that helps developers create custom Linux-based systems regardless of the hardware architecture. ```

- ## [Follow this link for key concepts.](https://www.yoctoproject.org/software-overview/)

# Start Yocto on macOS with docker

- Before make docker change docker setting
<img width="1270" alt="image" src="https://user-images.githubusercontent.com/54701846/215798263-587bd4a9-75e7-4911-b6d9-da331b7c34cc.png">
- Setting > Resources > Advanced
    - Change ```Disk image size``` to more than 150GB. The bigger the free space, the better.
    - Other options also bigger the better

## [Creating a Volume for Yocto Resource Management](https://github.com/crops/docker-win-mac-docs/wiki/Mac-Instructions)
- Create volume for the resources. This volume will manage your project files.
## Install docker image for Yocto
- Follow this command for create container:
-  `docker run --rm -it --name myyocto -v myvolume:/workdir crops/poky --workdir=/workdir`
    - {myvolume} is your volume name that you made the previous step.
    - {workdir} is the name of the directory to use as the workspace.
    - {myyocto} is your container name.

## Download the required file in the docker container for yocto project
- Use the docker exec command at the terminal because the sudo command or command cannot be executed with administrator privileges after docker connection (or you can change permission setting if you know)
- docker exec -it --user=root {container name}
    ``` bash
    docker exec -it --user=root myyocto sudo apt-get update && upgrade
    docker exec -it --user=root myyocto sudo apt-get install vim bmap-tools zstd liblz4-tool gawk wget git-core diffstat unzip texinfo gcc-multilib build-essential chrpath socat libsdl1.2-dev xterm python
    ```

# Set the environment to start Yocto

## Poky and other repositories download
- Create work directory (If you want use this location you can skip this step)
    ```bash
    cd ~
    mkdir yocto
    cd yocto
    ```

- Download Poky and extension repository with git
    ```bash
    git clone -b kirkstone git://git.yoctoproject.org/poky.git
    git clone -b kirkstone git://git.yoctoproject.org/meta-raspberrypi
    git clone -b kirkstone git://git.openembedded.org/meta-openembedded
    git clone -b kirkstone https://github.com/meta-qt5/meta-qt5
    ```

- Build environment  
    ```source ./poky/oe-init-build-env ./builds```
    - You can change {./buids} to other location that you want to manage build files  

- Add layer in project
    - You can choice two way
    1. Use bitbake-layer command
        ```shell
        bitbake-layers add-layer ../meta-raspberrypi/ \
        ../meta-openembedded/meta-oe/ \
        ../meta-openembedded/meta-python/ \
        ../meta-openembedded/meta-networking/ \
        ../meta-openembedded/meta-multimedia/ \
        ../meta-qt5/
        ```
    - In this step, Don't forget that you have to write the correct path.
    2. Write directly in bblayers.conf file
    - Change conf/bblayers.conf file like this:
    <img width="591" alt="image" src="https://user-images.githubusercontent.com/54701846/215812138-af6394f1-319a-4852-8211-4550d2d5bf4d.png">

# Make custom recipe with own git repository

## Change the local config
- Change or Add this line in conf/local.conf
    - ```MACHINE ??= "qemux86-64``` -> ```MACHINE = "raspberrypi4```

- Add this line to allow bitbake to execute multiple tasks at once.
    ```bash
    BB_NUMBER_THREADS = "12"
    PARALLEL_MAKE = "-j 12"
    ```
    - fit your amount of CPU core

## Create custom layer
- Create custom layer via bitbake command
    ```bash
    cd ~/yocto/builds
    bitbake-layers create-layer ../meta-{name}
    ```

## Base task flow
- Before making image let's know what is going on with task flow when the recipe build
<img width="732" alt="image" src="https://user-images.githubusercontent.com/54701846/215819832-812d844c-c1a5-4875-94c3-4ab4a1444046.png">*Sources : https://velog.io/@markyang92/Yocto-recipe1

|Task | What to do  |
|---|---|
|do_fetch()|	This task uses the SRC_URI variable and the argument’s prefix to determine the correct fetcher module.|
|do_unpack()|	Unpack source code to ${WORKDIR}|
|do_patch()|	Location and applying patch files|
|do_configure()	|Configures the source by enabling and disabling any build-time and configuration options for the software being built. The task runs with the current working directory set to ${B}.|
|do_compile()|	Compiles the source code. This task runs with the current working directory set to ${B}. </br></br> The default behavior of this task is to run the oe_runmake function if a makefile (Makefile, makefile, or GNUmakefile) is found. If no such file is found, the do_compile task does nothing.|
|do_install()|	Copies files that are to be packaged into the holding area \${D}. This task runs with the current working directory set to ${B}, which is the compilation directory.
|do_package()| Analyzes the content of the holding area ${D} and splits the content into subsets based on available packages and files. This task makes use of the PACKAGES and FILES variables.
|do_rootfs()| Creates the root filesystem (file and directory structure) for an image. See the [“Image Generation”](https://docs.yoctoproject.org/overview-manual/concepts.html#image-generation) section in the Yocto Project Overview and Concepts Manual for more information on how the root filesystem is created.|

- There is more tasks. But in this document only write about these task. If you want to know more tasks follow this [link](https://docs.yoctoproject.org/ref-manual/tasks.html#)

## Create custom image recipe
- Follow this command for move to own recipe path and make image recipe file
    ```bash
    cd ~/yocto/builds/meta-{name}
    mkdir -p recipes-{name}/images
    touch recipes-{name}/images/{name}.bb
    ```
    - You can check with ```tree``` command  
    ![image](https://user-images.githubusercontent.com/54701846/215818563-53c629e2-cb70-428f-b109-a324abec95c0.png)


## do_fetch(), do_unpack(), do_patch()

### Required variable in recipes
- Basically, the beginning of the recipe adds the SUMMARY, DESCRIPTION, and LICENSE variables. Without LICENSE, cannot do bitbake.
    ```python
    SUMMARY = "SEA:ME project image recipe"
    DESCRIPTION = "This image is for the SEA:ME project. This recipe gather whole of the SEA:ME project"
    LICENSE = "CLOSED"
    ```
    - For convenience, this recipe keeps License as a Closed, but for more information, please refer to the link below. 
        - [About Yocto License](https://docs.yoctoproject.org/dev/dev-manual/licenses.html)


### Protocols Supported by Yocto

|Protocol|Description| 
|---|---|
|az://	| Fetches files from an Azure Storage account using HTTPS.|
|bzr://	|Fetches files from a Bazaar revision control repository.|
|ccrc://	|Fetches files from a ClearCase repository.|
|cvs://	|Fetches files from a CVS revision control repository.|
|file://| Fetches files, which are usually files shipped with the Metadata, from the local machine. The path is relative to the FILESPATH variable. Thus, the build system searches, in order, from the following directories, which are assumed to be a subdirectories of the directory in which the recipe file (.bb) or append file (.bbappend) resides|
|ftp://	|Fetches files from the Internet using FTP.|
|git://	|Fetches files from a Git revision control repository.|
|gitsm://	|Fetches submodules from a Git revision control repository.|
|hg://	|Fetches files from a Mercurial (hg) revision control repository.|
|http://|	Fetches files from the Internet using HTTP.|
|https://|	Fetches files from the Internet using HTTPS.|
|npm://	|Fetches JavaScript modules from a registry.|
|osc://	|Fetches files from an OSC (OpenSUSE Build service) revision control repository.|
|p4://	|Fetches files from a Perforce (p4) revision control repository.|
|repo://|	Fetches files from a repo (Git) repository.|
|ssh://	|Fetches files from a secure shell.|
|svn://	|Fetches files from a Subversion (svn) revision control repository.|

### Import source files and release them to temporary directories
- In this step, do_fetch(), do_unpack(), do_compile() tasks are worked
```python
SRC_URI = "git://github.com/skamo3/SEA-ME-Project-2.git;protocol=https;branch=main"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/git"
```

### Options SRC_URI with git
- ```SRC_URI = "git://.../<REPO>.git;protocol=git;branch=master```
    - Verify that the source specified in \${SRC_URI} exists in \${DL_DIR} and download the source locally if it does not exist
    - When building a recipe, all required files must be in the directory defined in \${WORKDIR}. So before that, check \${DL_DIR} to see if it's downloaded, and if it is, cache.
    - \${SRC_URI}
        - Decide where to get the sauce or copy it
    - Git addresses and a variety of options are available

- ```SRC_REV = "${AUTO_REV}"``` or ```SRC_REV = "<commit_hash>"```
    - Check the current revision in the do_fetch() task and replace it with the revision if the revision is not correct in the do_unpack() task. Version Check
    - Automatically fetches with ${AUTO_REV}

- ```S = "${WORKDIR}/git"```
    - Local path where the source will be placed
    - Positioned as ```~/git``` in general

|parameter| Description  |
|---|---|
|nocheckout	| Tells the fetcher to not checkout source code when unpacking when set to “1”. Set this option for the URL where there is a custom routine to checkout code. The default is “0”.|
|rebaseable|	Indicates that the upstream Git repository can be rebased. You should set this parameter to “1” if revisions can become detached from branches. In this case, the source mirror tarball is done per revision, which has a loss of efficiency. Rebasing the upstream Git repository could cause the current revision to disappear from the upstream repository. This option reminds the fetcher to preserve the local cache carefully for future use. The default value for this parameter is “0”.| 
|nobranch|	Tells the fetcher to not check the SHA validation for the branch when set to “1”. The default is “0”. Set this option for the recipe that refers to the commit that is valid for a tag instead of the branch.|
|bareclone|	Tells the fetcher to clone a bare clone into the destination directory without checking out a working tree. Only the raw Git metadata is provided. This parameter implies the “nocheckout” parameter as well.|
|branch|	The branch(es) of the Git tree to clone. Unless “nobranch” is set to “1”, this is a mandatory parameter. The number of branch parameters must match the number of name parameters.|
|rev|	The revision to use for the checkout. The default is “master”.|
|tag|	Specifies a tag to use for the checkout. To correctly resolve tags, BitBake must access the network. For that reason, tags are often not used. As far as Git is concerned, the “tag” parameter behaves effectively the same as the “rev” parameter.|
|subpath|	Limits the checkout to a specific subpath of the tree. By default, the whole tree is checked out.|
|destsuffix|	The name of the path in which to place the checkout. By default, the path is git/.|
|usehead|	Enables local git:// URLs to use the current branch HEAD as the revision for use with AUTOREV. The “usehead” parameter implies no branch and only works when the transfer protocol is file://.|

- [See this link for more information.](https://docs.yoctoproject.org/bitbake/2.0/bitbake-user-manual/bitbake-user-manual-fetching.html)


## do_configure(), do_compile()
### do_configure()
- Before compiling a source file, declaring or defining environmental variables required 
```python
do_configure() {
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
```
- In this case, make Makefile with qmake. Prepare the compile. 
- If you need to set Environment variable Do it in this task.
- If the path is not divided and the CMake file is in the root directory as shown in the example above, do_configure() task runs CMake on its own if you do ```inherit cmake```.

### do_compile()
- Task to compile Makefile, CMakeLists.txt, etc. created by do_configure()
- Typically \${B} performs a compile on the path

```python
do_compile() {
    cd ${S}/builds/DICApp/
    make

    cd ${S}/builds/CanReceiver/
    make

    cd ${S}/builds/ServerApp/
    make
}
```

- If the path is not divided and the Makefile is in the root directory as shown in the example above, ```oe_runmake``` is automatically executed without creating do_compile()

- Without Makefile
    ```python
    do_compile() {
        ${CC} userfile.c ${LDFLAGS} -o userfile
    }
    ```
    - write down the compilation options directly as above.

## do_install()
- This task installs result at ${D}
- do_install() is performed at ${B}
- If you have a makefile, this task performs a make installation
- ```D=${WORKDIR}/image```

```python
do_install() {
    install -d ${D}/home/root/excutable
    install -m 0755 ${S}/builds/DICApp/DigitalInstrumentCluster ${D}/home/root/excutable
    install -m 0755 ${S}/builds/CanReceiver/CanReceiver ${D}/home/root/excutable
    install -m 0755 ${S}/builds/ServerApp/ServerApp ${D}/home/root/excutable
}
```
- If you put it in /home/root, you need to package it.
- In this task, the directory structure of the image to be finally built is captured.

- Build results will not be installed in the image if the do_install() task does nothing even if you build well
### install option
- -d: Create directory
    - this command is same like mkdir -r
- -m: Move and chmod
    - this command is same like chmod and mv the files and directories

## do_package()
- Steps to finely package a built image
- You can use it in other recipes by removing only the parts you need in the process.

```python
FILES:${PN} += " \
    /home/root/ \
    "
```
- In this case, do_package() for use ```/home/root/``` path in image

