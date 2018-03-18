package net.dankito.service.synchronization.initialsync.model

import net.dankito.deepthought.model.DeepThought


data class DeepThoughtSyncInfo(val id: String, val localDeviceId: String) {

    internal constructor() : this("", "") // for Jackson

    constructor(deepThought: DeepThought) : this(deepThought.id!!, deepThought.localDevice.id!!)

}