package net.dankito.utils.ui.model


data class ConfirmationDialogConfig(val showNoButton: Boolean = true, val noButtonText: String? = null,
                                    val showThirdButton: Boolean = false, val thirdButtonText: String? = null,
                                    val confirmButtonText: String? = null)