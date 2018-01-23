package net.dankito.deepthought.files.synchronization.model


enum class SynchronizeFileResult {

    Success,
    DidNotReceiveAllBytes,
    RemoteFileSynchronizationPortNotSet,
    LocalFileInfoNotSet,
    RemoteDoesNotHaveFile,
    NoSlotsAvailableTryLater,
    Prohibited,
    ErrorOccurred
}