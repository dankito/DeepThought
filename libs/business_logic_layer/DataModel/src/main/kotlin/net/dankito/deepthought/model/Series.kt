package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany


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
        var references: MutableList<Reference> = ArrayList() // TODO: don't expose a mutable list to the outside
            private set


        fun hasReferences(): Boolean {
            return references.size > 0
        }

        internal fun addReference(reference: Reference): Boolean {
            return references.add(reference)
        }

        internal fun removeReference(reference: Reference): Boolean {
            return references.remove(reference)
        }


        override fun toString(): String {
            return title + " (" + references.size + ")"
        }

}