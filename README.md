# DeepThought #

![DeepThought Logo](.img/deepthought-logo.png)

A knowledge management application for Android and Java to easily store all your data and quickly re-find it.

Local data storage and sync, no external servers involved.

**Features**

- real time full text search engine (my one gigabyte database is searched in less than 100 milliseconds)
- local data storage
- cloudless data synchronization over local network
- a Rich Text editor
- real time full text search engine (my one gigabyte database is searched in less than 100 milliseconds.)
- extracting data from web sites (the same technique as for the Firefox reader view is used)
- extracting text from PDF files
- attaching files (including automatic file synchronization)
- tags
- RSS feed reader

- Upcoming: OCR (text extraction from images)

## Download

- **Android App** https://play.google.com/store/apps/details?id=net.dankito.deepthought
- **JavaFX Desktop App** (must be compiled from source, see section "Development", downloadable version coming soon)

## Firewall configuration

A couple of firewall ports have to be opened for the application to work properly.

**Data retrieval**

| Direction     | Port(s)       |  Functionality           |
| ------------- | ------------- | ------------------------ |
| OUT           | TCP 80, 443   | Save webpages, RSS feeds |


**Synchronization**

All ports only need to be opened for the local network.

As DeepThought uses
a serverless multi-master synchronization, nothing outside the local network
is ever contacted.

As the synchronization layer is currently undergoing a major redesign, these ports will be significantly simplified with the upcoming 0.6.0 release.


| Direction     | Port(s)                                 |  Functionality           |
| ------------- | --------------------------------------- | ------------------------ |
| IN            | UDP 32788                               | Device discovery         |
| IN            | First free port starting from TCP 32789 | messaging |
| IN            | First free port starting from TCP 27387 | data synchronization
| IN            | TCP 32789 - 33489                       | first time synchronization; only used for initial synchronization of two devices
| IN            | First free port starting from TCP 60705 | file synchronization

## Development

### Getting the sourcecode

To init submodules, clone with  
    
    git clone --recursive https://github.com/dankito/DeepThought.git

or clone and run  

    git submodule init  
    git submodule update

### Start the JavaFX application

For running the Java Desktop Application you at least need Java 8_u40 and the JavaFX library.

JavaFX is not bundled with OpenJDK, some distributions allow to install
it via the package manager (e.g. `apt install openjfx`).

To start the Desktop Application go to `DeepThoughtJavaFX/src/main/kotlin/net/dankito/deepthought/javafx/`, right click on `DeepThoughtJavaFXApplication.kt` and choose Debug or Run.

or via gradle (remove the `subprojects` entry from the main gradle file):

    cd DeepThoughtJavaFX
    ../gradlew build
    cd build/distributions
    unzip DeepThoughtJavaFX.zip
    DeepThoughtJavaFX/bin/DeepThoughtJavaFX

