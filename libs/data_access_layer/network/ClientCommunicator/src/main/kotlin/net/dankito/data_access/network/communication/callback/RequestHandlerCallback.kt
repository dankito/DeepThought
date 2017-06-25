package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.message.Response


interface RequestHandlerCallback {

    fun done(response: Response<*>)  // TODO: it should actually return a net.dankito.sync.communication.message.Response to keep layer consistency, but would needed double mapping

}
