package net.dankito.deepthought.files.synchronization.model


enum class PermitSynchronizeFileResult {

    SynchronizationPermitted,
    DoNotHaveFile,
    NoSlotsAvailableTryLater,
    Prohibited,
    ErrorOccurred

}