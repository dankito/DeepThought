package net.dankito.deepthought.files.synchronization


enum class PermitSynchronizeFileResult {

    SynchronizationPermitted,
    DoNotHaveFile,
    NoSlotsAvailableTryLater,
    Prohibited,
    ErrorOccurred

}