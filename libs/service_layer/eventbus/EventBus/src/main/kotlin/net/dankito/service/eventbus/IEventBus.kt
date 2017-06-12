package net.dankito.service.eventbus

import net.dankito.service.eventbus.messages.IEventBusMessage


interface IEventBus {

    fun register(listener: Any)

    fun unregister(listener: Any)


    fun post(message: IEventBusMessage)

    fun postAsync(message: IEventBusMessage)

}