package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.SeriesTableName)
data class Series(

        @Column(name = TableConfig.SeriesTitleColumnName)
        var title: String

) : BaseEntity() {

    companion object {
        private const val serialVersionUID = -7176298227016698448L
    }


    private constructor() : this("")


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "series")
    var sources: MutableList<Source> = ArrayList() // TODO: don't expose a mutable list to the outside
        private set


    fun hasSources(): Boolean {
        return sources.size > 0
    }

    val countSources: Int
        get() = sources.size

    internal fun addSource(source: Source): Boolean {
        return sources.add(source)
    }

    internal fun removeSource(source: Source): Boolean {
        return sources.remove(source)
    }


    val displayText: String
        @Transient
        get() = "$title ($countSources)"

    override fun toString(): String {
        return displayText
    }

}