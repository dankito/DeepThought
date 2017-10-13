package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.TagTableName)
open class Tag(
        @Column(name = TableConfig.TagNameColumnName)
        var name: String

): BaseEntity(), Serializable {

    companion object {
        private const val serialVersionUID = 1204202485407318615L
    }




    @Column(name = TableConfig.TagDescriptionColumnName)
    var description = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Item?
    var items: List<Item> = ArrayList() // TODO: don't expose a mutable list to the outside
        protected set

    init {
        this.name = name
    }


    private constructor() : this("")


    fun hasItems(): Boolean {
        return countItems > 0
    }

    val countItems: Int
        get() = items.size

    internal fun addItem(item: Item): Boolean {
        // i know this is not perfect as added Item could have a smaller ItemIndex than already added ones
        (items as? MutableList<Item>)?.add(0, item) // but sorting is not an option as with sorting all Items would have to be loaded witch is bad on Tags with a lot of Items

        return true
    }

    internal fun removeItem(item: Item): Boolean {
        return (items as? MutableList<Item>)?.remove(item) ?: false
    }


    val displayText: String
        @Transient
        get() = "$name ($countItems)"

    override fun toString(): String {
        return displayText
    }

}
