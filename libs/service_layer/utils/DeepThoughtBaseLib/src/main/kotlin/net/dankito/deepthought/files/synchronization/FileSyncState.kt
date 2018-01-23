package net.dankito.deepthought.files.synchronization

import net.dankito.deepthought.model.DiscoveredDevice
import net.dankito.deepthought.model.FileLink


data class FileSyncState(val file: FileLink,
                         var countTries: Int = 0,
                         val devicesNotHavingFile: MutableList<DiscoveredDevice> = mutableListOf(),
                         val devicesHavingFileButNoFreeSlots: MutableList<DiscoveredDevice> = mutableListOf()
)