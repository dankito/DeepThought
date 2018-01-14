package net.dankito.deepthought.javafx.service.events

import net.dankito.deepthought.model.Source
import net.dankito.service.eventbus.messages.IEventBusMessage


class EditingSourceDoneEvent(val didSaveSource: Boolean, val savedSource: Source? = null) : IEventBusMessage