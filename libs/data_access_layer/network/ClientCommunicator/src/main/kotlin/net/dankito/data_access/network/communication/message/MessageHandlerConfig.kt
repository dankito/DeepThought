package net.dankito.data_access.network.communication.message

import net.dankito.data_access.network.communication.callback.IDeviceRegistrationHandler
import net.dankito.deepthought.model.INetworkSettings


data class MessageHandlerConfig(val networkSettings: INetworkSettings, val challengeHandler: ChallengeHandler, val registrationHandler: IDeviceRegistrationHandler)
