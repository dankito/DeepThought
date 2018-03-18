package net.dankito.service.search


object FieldValue {

    val BooleanFieldFalseValue = "false"
    val BooleanFieldTrueValue = "true"

    val NoTagsFieldValue = "notags"
    val NoSourceFieldValue = "noreference" // leave its outdated field value as otherwise previously created items won't get found
    val NoFilesFieldValue = "nofiles"
    val NoNotesFieldValue = "nonotes"

}