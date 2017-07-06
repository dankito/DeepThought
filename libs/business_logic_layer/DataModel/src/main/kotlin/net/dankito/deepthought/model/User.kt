package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.io.Serializable
import java.util.*
import javax.persistence.*




@Entity(name = TableConfig.UserTableName)
data class User(

        @Column(name = TableConfig.UserUserNameColumnName)
        var userName: String,

        @Column(name = TableConfig.UserUniversallyUniqueIdColumnName)
        var universallyUniqueId: String

) : BaseEntity(), Serializable {

    companion object {
        private const val serialVersionUID = 7734370867234770314L
    }


    internal constructor() : this("", "")


    @Column(name = TableConfig.UserFirstNameColumnName)
    var firstName: String = ""

    @Column(name = TableConfig.UserLastNameColumnName)
    var lastName: String = ""

    @Column(name = TableConfig.UserPasswordColumnName)
    var password: String = ""


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.UserSynchronizedDevicesJoinTableName,
            joinColumns = arrayOf( JoinColumn(name = TableConfig.UserSynchronizedDevicesUserIdColumnName) ),
            inverseJoinColumns = arrayOf( JoinColumn(name = TableConfig.UserSynchronizedDevicesDeviceIdColumnName) ))
    var synchronizedDevices: List<Device> = ArrayList()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.UserIgnoredDevicesJoinTableName,
            joinColumns = arrayOf( JoinColumn(name = TableConfig.UserIgnoredDevicesUserIdColumnName) ),
            inverseJoinColumns = arrayOf( JoinColumn(name = TableConfig.UserIgnoredDevicesDeviceIdColumnName) ))
    var ignoredDevices: List<Device> = ArrayList()
        private set



    fun containsSynchronizedDevice(device: Device): Boolean {
        return synchronizedDevices.contains(device)
    }

    fun addSynchronizedDevice(device: Device): Boolean {
        if(containsSynchronizedDevice(device) == false) {
            return (synchronizedDevices as? MutableList<Device>)?.add(device) ?: false
        }

        return false
    }

    fun removeSynchronizedDevice(device: Device): Boolean {
        if(containsSynchronizedDevice(device)) {
            return (synchronizedDevices as? MutableList<Device>)?.remove(device) ?: false
        }

        return false
    }


    fun containsIgnoredDevice(device: Device): Boolean {
        return ignoredDevices.contains(device)
    }

    fun addIgnoredDevice(device: Device): Boolean {
        if(containsIgnoredDevice(device) == false) {
            return (ignoredDevices as? MutableList<Device>)?.add(device) ?: false
        }

        return false
    }

    fun removeIgnoredDevice(device: Device): Boolean {
        if(containsIgnoredDevice(device)) {
            return (ignoredDevices as? MutableList<Device>)?.remove(device) ?: false
        }

        return false
    }

}