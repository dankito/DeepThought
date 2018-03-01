package net.dankito.synchronization.files.model


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