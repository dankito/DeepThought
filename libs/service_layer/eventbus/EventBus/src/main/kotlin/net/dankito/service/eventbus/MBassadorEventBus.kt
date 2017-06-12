package net.dankito.service.eventbus

import net.dankito.service.eventbus.messages.IEventBusMessage
import net.engio.mbassy.bus.MBassador


class MBassadorEventBus : IEventBus {

    private val bus = MBassador<Any>()


    override fun register(listener: Any) {
        bus.subscribe(listener)
    }

    override fun unregister(listener: Any) {
        bus.unsubscribe(listener)
    }


    override fun post(message: IEventBusMessage) {
        bus.post(message).now()
    }

    override fun postAsync(message: IEventBusMessage) {
        bus.post(message).asynchronously()
    }

}