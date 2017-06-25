package net.dankito.data_access.network.communication.message

import net.dankito.data_access.network.communication.callback.IsSynchronizationPermittedHandler
import net.dankito.deepthought.model.INetworkSettings


data class MessageHandlerConfig(val networkSettings: INetworkSettings, val challengeHandler: ChallengeHandler, val permissionHandler: IsSynchronizationPermittedHandler)
