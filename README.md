# DeepThought #

A knowledge management application for Android and Java to easily store all your data and quickly re-find it.

It supports:

- a Rich Text editor
- local data storage
- real time full text search engine (my one gigabyte database is searched in less than 100 milliseconds.)
- cloudless data synchronization over local network
- extracting data from web sites (the same technique as for the Firefox reader view is used)
- extracting text from PDF files
- attaching files (including automatic file synchronization)
- tags
- RSS feed reader
- Upcoming: OCR (text extraction from images)

## Cloning and start-up


To init submodules, clone with  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;git clone --recursive https://github.com/dankito/DeepThought.git

or clone and run  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;git submodule init  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;git submodule update


To start the Desktop Application go to  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DeepThoughtJavaFx -> src -> main -> java -> net -> deepthought  
, right click on 'DeepThoughtFx' and choose Debug or Run.


For running the Java Desktop Application you at least need Java 8_u40.

## Firewall configuration

### Data retrieval
If you want to save web pages or RSS feeds, you have to open the corresponding ports (usually TCP 80 and 443).

### Synchronization
All ports only need to be opened for the local network.
As DeepThought uses a serverless multi-master synchronization, nothing outside the local network is ever contacted.

Used ports are:
- UDP 32788 (device discovery)
- First free port starting from TCP 32789 (messaging)
- First free port starting from TCP 27387 (data synchronization)
- TCP port between 32789 - 33489 (first time synchronization; only used for initial synchronization of two devices). I admit this is really ugly.
- First free port starting from TCP 60705 (file synchronization)

As the synchronization layer is currently undergoing a major redesign, these ports will be significantly simplified with the upcoming 0.6.0 release.