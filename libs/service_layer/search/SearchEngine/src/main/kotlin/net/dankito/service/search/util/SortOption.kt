package net.dankito.service.search.util


class SortOption(val property: String, val ascending: Boolean) {

    override fun toString(): String {
        return "$property ascending? $ascending"
    }

}