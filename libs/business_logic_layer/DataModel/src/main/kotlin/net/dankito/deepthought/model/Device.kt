package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import javax.persistence.*


@Entity(name = TableConfig.DeviceTableName)
data class Device(

        @Column(name = TableConfig.DeviceNameColumnName)
        var name: String,

        @Column(name = TableConfig.UniqueDeviceIdColumnName)
        var uniqueDeviceId: String,

        @Enumerated(EnumType.ORDINAL)
        @Column(name = TableConfig.DeviceOsTypeColumnName)
        var osType: OsType,

        @Column(name = TableConfig.DeviceOsNameColumnName)
        var osName: String = "",

        @Column(name = TableConfig.DeviceOsVersionColumnName)
        var osVersion: String = "",

        @Column(name = TableConfig.DeviceDescriptionColumnName)
        var description: String = ""

) : BaseEntity() {


    companion object {
        private const val serialVersionUID = 7190723756152328858L
    }


    @Column(name = TableConfig.DeviceIconColumnName)
    @Lob
    var deviceIcon: ByteArray? = null


    internal constructor() : this("", "", OsType.DESKTOP)



    fun getDisplayText(): String {
        var displayText = osName + " " + osVersion;

        if(osName.toLowerCase().contains("android")) {
            displayText = name + " (" + displayText + ")";
        }

        return displayText
    }

}
