package net.dankito.deepthought.files.synchronization.model

import net.dankito.synchronization.model.DiscoveredDevice
import net.dankito.synchronization.model.FileLink


data class FileSyncState(val file: FileLink,
                         var countTries: Int = 0,
                         val devicesUnlikelyToGetFileFrom: MutableList<DiscoveredDevice> = mutableListOf(),
                         val devicesWithGoodChances: MutableList<DiscoveredDevice> = mutableListOf()
)