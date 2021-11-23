# JDKMon <a href="https://foojay.io/today/works-with-openjdk"><img align="right" src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png" width="100"></a>


[![GitHub stars](https://badgen.net/github/stars/HanSolo/JDKMon)](https://GitHub.com/HanSolo/JDKMon/stargazers/)

[![Github all releases](https://img.shields.io/github/downloads/HanSolo/JDKMon/total.svg)](https://GitHub.com/HanSolo/JDKMon/releases/)

[![Windows](https://svgshare.com/i/ZhY.svg)](https://svgshare.com/i/ZhY.svg)
[![macOS](https://svgshare.com/i/ZjP.svg)](https://svgshare.com/i/ZjP.svg)
[![Linux](https://svgshare.com/i/Zhy.svg)](https://svgshare.com/i/Zhy.svg)

[![GitHub license](https://badgen.net/github/license/HanSolo/JDKMon)](https://github.com/HanSolo/JDKMon/blob/master/LICENSE)


<br>
JDKMon is a little tool written in JavaFX that tries to detect all JDK's installed
on your machine and will inform you about new updates of each OpenJDK distribution found. 

At the moment the following distributions will be identified:
- AdoptOpenJDK
- AdoptOpenJDK J9
- Bi Sheng
- Corretto
- Debian (pkgs not downloadable)
- Dragonwell
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

![About](https://i.ibb.co/S5gGPQT/JDKMon-About.png)

You could also always get the latest version [here](https://github.com/HanSolo/JDKMon/releases).

Depending on the operating system it will try to find the JDK's
in the following folders:

MacOS: `/System/Volumes/Data/Library/Java/JavaVirtualMachines/`

Windows: `C:\Program Files\Java\`

Linux: `/usr/lib/jvm`(FXTrayIcon only supports a few linux distros) 

You can change the folder JDKMon is scanning for installed JDK's by selecting
the `"Add search path"` menu entry. The selected path will be added to the list of 
folders stored in the jdkmon.properties file in your user home folder.
JDKMon will scan all given folders for installed JDK's. 

If you would like to reset the folders that should be scanned to the default, simply
select `"Default search path"` in the menu.

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

How it looks on MacOS and Linux (light/dark mode):
![Updates](https://i.ibb.co/HttqQ3n/update-mac-linux.png)

![Download](https://i.ibb.co/DbYK1F3/download-mac-linux.png)


How it looks on Windows (light/dark mode):

![Updates](https://i.ibb.co/w6d9bV4/update-win.png)

![Download](https://i.ibb.co/HF5F8ff/download-win.png)
