# JDKMon 

[![GitHub stars](https://badgen.net/github/stars/HanSolo/JDKMon)](https://GitHub.com/HanSolo/JDKMon/stargazers/)

[![Github all releases](https://img.shields.io/github/downloads/HanSolo/JDKMon/total.svg)](https://GitHub.com/HanSolo/JDKMon/releases/)

[![Windows](https://svgshare.com/i/ZhY.svg)](https://svgshare.com/i/ZhY.svg)
[![macOS](https://svgshare.com/i/ZjP.svg)](https://svgshare.com/i/ZjP.svg)
[![Linux](https://svgshare.com/i/Zhy.svg)](https://svgshare.com/i/Zhy.svg)

[![GitHub license](https://badgen.net/github/license/HanSolo/JDKMon)](https://github.com/HanSolo/JDKMon/blob/master/LICENSE)


<br>

[JDKMon Home](https://harmoniccode.blogspot.com/p/jdkmon.html)

<br>
JDKMon is a little tool written in JavaFX that tries to detect all JDK's installed
on your machine and will inform you about new updates of each OpenJDK distribution found.
In addition JDKMon is also able to monitor JavaFX SDK versions that are installed on your
machine.

At the moment the following distributions will be identified:
- AdoptOpenJDK
- AdoptOpenJDK J9
- Bi Sheng
- Corretto
- Debian (pkgs not downloadable)
- Dragonwell
- Gluon GraalVM
- Graalvm CE8
- Graalvm CE11
- Graalvm CE16
- Graalvm CE17
- JetBrains
- Kona
- Liberica
- Liberica Native
- Mandrel
- Microsoft
- OJDK Build
- Open Logic
- Oracle (not all pkgs downloadable)
- Oracle OpenJDK
- RedHat (pkgs not downloadable)
- SAP Machine
- Semeru
- Semeru Certified
- Temurin
- Trava
- Zulu
- Zulu Prime


In case the distribution found was not identified it will be mentioned in the main window as
"Unknown build of OpenJDK". If you stumble upon this case, please file an [issue](https://github.com/HanSolo/JDKMon/issues) and mention
the distribution incl. version that you are using.

JDKMon can be build for: 
- Windows x64
- MacOS x64
- MacOS aarch64
- Linux x64
- Linux aarch64 (e.g. Raspberry Pi 4 running Raspberry Pi OS 64bit)

You can always check if you have the latest version install by taking a look at the About window.
This can be opened by selecting ```About``` from the menu. If there is a new version available
you will find a link in the About window that will open the page with the latest release in your default
browser.

![About](https://i.ibb.co/QNqBLcG/About.png)

You could also always get the latest version [here](https://github.com/HanSolo/JDKMon/releases).

Depending on the operating system it will try to find the JDK's
in the following folders:

MacOS: `/System/Volumes/Data/Library/Java/JavaVirtualMachines/`

Windows: `C:\Program Files\Java\`

Linux: `/usr/lib/jvm`(FXTrayIcon only supports a few linux distros) 

You can change the folders JDKMon is scanning for installed JDK's by selecting
the `"Add JDK search path"` menu entry. The selected path will be added to the list of 
folders stored in the jdkmon.properties file in your user home folder.
JDKMon will scan all given folders for installed JDK's. 

If you would like to reset the folders that should be scanned to the default, simply
select `"Default JDK search path"` in the menu.

You can also change the folders JDKMon is scanning for JavaFX SDK's by selecting
the `"Add JavaFX search path"` menu entry. The selected path will be added to the list of
folders stored in the jdkmon.properties file in your user home folder.
You just need to add the folder than contains all your `javafx-sdk-mm.ii.uu` folders.

If you would like to reset the folders that should be scanned for JavaFX SDK's to the default,
simply select `"Default JavaFX search path"`. It will be reset to the your home folder.

You can search and download for a JDK from different distributions in the dialog that opens when
you select `"Download a JDK"` from the menu.

The application will stay in the system tray with an icon. If you click the icon
a menu will appear where you can select


`JDK Main: The main window`

`Rescan: Will rescan for installed JDK's and check for updates`

`Add search path: Will open the directory chooser to add a path to the search paths`

`Default search path: Will reset the search path to the platform dependent default`

`Remember download folder: When remember download folder is active you don't have to select a download folder everytime`

`Download a JDK: Opens a dialog where you can search/download a JDK from different distributions`

`Exit: Exit the application`
`

<b>ATTENTION:</b><br>
When running the application via `java -jar` on Linux you might have to
add `-Djdk.gtk.version=2` to make it work correctly e.g. `java -Djdk.gtk.version=2 -jar JDKMon-linux-17.0.0.jar`

On Mac and Windows the dark mode will be detected automatically and the user interface will
adjust it's design to either dark or light mode. On Linux you can change the variable "dark_mode" in the
jdkmon.properties file (in your user folder) to "TRUE" to see the application in dark mode.
The application will only check for dark/light mode during startup and won't change when it is running.

The main window will show you all JDK's found by JDKMon and if there is an
update available it will show you the archive types of the available updates.
In the image below you can see that there is an update for GraalVM available
and that you can download it as a tar.gz file.
To download an update just click on the archive type tag and choose a folder
where the download should be stored.

If an update is available and there are release details available, a little blue dot with a question mark
will appear at the end of the packages list. When clicking on the dot the default browser will open with
the release details over at [foojay.io](https://foojay.io)

JDKMon will not only help you to keep your installed OpenJDK distributions up to date but will also check
the NVD for known vulnerabilities (CVE) related to their version number. If you have for example installed
an OpenJDK distribution with the version 11.0.13, JDKMon will check the NVD for known vulnerabilities of OpenJDK
with the version number 11.0.13. In case JDKMon will find vulnerabilities, it will indicate this with a red
square with an exclamation mark behind the version number. Meaning to say if you have for example Zulu 11.0.13 
and Liberica 11.0.13 installed ony your machine, JDKMon will show you the vulnerabilties, it found for
OpenJDK 11.0.13 for both distributions. So there is no guarantee that your installed distribution is really
affected by the CVE's found in NVD but you at least get the info that there are vulnerabilities for 11.0.13.

![vulnerability](https://i.ibb.co/JRTjp7R/JDKMon.png)

When you click on the yellow circle a window will open which shows the CVE's found. 
You can click on each CVE to open it in a browser with more detailled information.

![vulnerability](https://i.ibb.co/jGVFVh2/vulnerabitlities.png)

How it looks on MacOS and Linux (light/dark mode):
![Updates](https://i.ibb.co/HttqQ3n/update-mac-linux.png)

![Download](https://i.ibb.co/DbYK1F3/download-mac-linux.png)


How it looks on Windows (light/dark mode):

![Updates](https://i.ibb.co/w6d9bV4/update-win.png)

![Download](https://i.ibb.co/HF5F8ff/download-win.png)
