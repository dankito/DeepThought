package net.dankito.utils


interface ConsumerListener<T> {

    fun consumeItem(item: T)

}
