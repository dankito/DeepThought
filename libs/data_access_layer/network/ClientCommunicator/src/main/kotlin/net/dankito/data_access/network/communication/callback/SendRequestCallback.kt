package net.dankito.data_access.network.communication.callback

import net.dankito.data_access.network.communication.message.Response


interface SendRequestCallback<T> {

    fun done(response: Response<T>)

}
