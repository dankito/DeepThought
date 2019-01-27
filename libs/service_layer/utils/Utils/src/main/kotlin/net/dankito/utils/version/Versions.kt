package net.dankito.utils.version

import net.dankito.utils.Version


class Versions {

    companion object {
        val AppVersion = Version(0, 4)

        const val DataModelVersion = 2

        const val CommunicationProtocolVersion = 2

        const val SearchEngineIndexVersion = 3

        const val HtmlEditorVersion = 2
    }


    /*
        Change protocol:


        Data model:

        - 1 -> 2:
          Added entity LocalFileInfo
          FileLink: Added properties isLocalFile, mimeType, fileType, fileSize, fileLastModified, and hashSHA256. Removed isFolder.
          Removed entities FileType, NoteType, ExtensibleEnumeration


        Communication protocol:

        - 1 -> 2:
          Added fileSynchronizationPort to DiscoveredDevice.
          Implemented synchronizing files (sending fileSynchronizationPort in messages; added FileServer and FileSyncService)


        Search index:

        - 1 -> 2:
          Added FileLink- and LocalFileInfoIndexWriterAndSearcher. Implemented searching for files and LocalFileInfo.

        - 2 -> 3:
          Added fields for sorting items by content and source preview
          Prefixed item fields names with 'item_'


        HTML editor:

        - 1 -> 2:
          Using now RichTextEditor instead of embedded CKEditor

     */

}