package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import javax.persistence.Column
import javax.persistence.Entity


@Entity(name = TableConfig.SeriesTableName)
data class Series(

        @Column(name = TableConfig.SeriesTitleColumnName)
        var title: String

) : BaseEntity() {

        companion object {
                private const val serialVersionUID = -7176298227016698448L
        }


        private constructor() : this("")

}