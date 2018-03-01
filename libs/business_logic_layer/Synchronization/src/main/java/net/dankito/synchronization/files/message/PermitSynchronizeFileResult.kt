package net.dankito.synchronization.files.message


enum class PermitSynchronizeFileResult {

    SynchronizationPermitted,
    DoNotHaveFile,
    NoSlotsAvailableTryLater,
    Prohibited,
    ErrorOccurred

}