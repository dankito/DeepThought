package net.dankito.deepthought.files.synchronization.message


enum class PermitSynchronizeFileResult {

    SynchronizationPermitted,
    DoNotHaveFile,
    NoSlotsAvailableTryLater,
    Prohibited,
    ErrorOccurred

}