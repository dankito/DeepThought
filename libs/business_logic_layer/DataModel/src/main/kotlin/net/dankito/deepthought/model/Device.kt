package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
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

) : UserDataEntity() {


    companion object {
        private const val serialVersionUID = 7190723756152328858L
    }



    @Column(name = TableConfig.DeviceLastKnownIpColumnName)
    var lastKnownIpAddress: String = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "devices") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }
    var users: MutableSet<User> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "devices")
    var groups: MutableSet<UsersGroup> = HashSet()
        private set


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.DEVICE_SYNCHRONIZED_DEVICES_JOIN_TABLE_NAME,
            joinColumns = arrayOf( JoinColumn(name = TableConfig.DEVICE_SYNCHRONIZED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME) ),
            inverseJoinColumns = arrayOf( JoinColumn(name = TableConfig.DEVICE_SYNCHRONIZED_DEVICES_DEVICE_ID_COLUMN_NAME) ))
    var synchronizedDevices: List<Device> = ArrayList()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.DEVICE_IGNORED_DEVICES_JOIN_TABLE_NAME,
            joinColumns = arrayOf( JoinColumn(name = TableConfig.DEVICE_IGNORED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME) ),
            inverseJoinColumns = arrayOf( JoinColumn(name = TableConfig.DEVICE_IGNORED_DEVICES_DEVICE_ID_COLUMN_NAME) ))
    var ignoredDevices: List<Device> = ArrayList()
        private set

    @Column(name = TableConfig.DeviceIconColumnName)
    @Lob
    var deviceIcon: ByteArray? = null


    internal constructor() : this("", "", OsType.DESKTOP)



    internal fun addUser(user: User): Boolean {
        return users.add(user)
    }

    internal fun removeUser(user: User): Boolean {
        return users.remove(user)
    }



    fun addGroup(group: UsersGroup): Boolean {
        if (groups.contains(group) == false) {
            if (groups.add(group)) {
                group.addDevice(this)

                return true
            }
        }

        return false
    }

    fun removeGroup(group: UsersGroup): Boolean {
        if (groups.contains(group) == true) {
            if (groups.remove(group)) {
                group.removeDevice(this)

                return true
            }
        }

        return false
    }


    fun addSynchronizedDevice(device: Device): Boolean {
        if (synchronizedDevices.contains(device) == false) {
            return (synchronizedDevices as? MutableList<Device>)?.add(device) ?: false
        }

        return false
    }

    fun removeSynchronizedDevice(device: Device): Boolean {
        if (synchronizedDevices.contains(device)) {
            return (synchronizedDevices as? MutableList<Device>)?.remove(device) ?: false
        }

        return false
    }


    fun addIgnoredDevice(device: Device): Boolean {
        if (ignoredDevices.contains(device) == false) {
            return (ignoredDevices as? MutableList<Device>)?.add(device) ?: false
        }

        return false
    }

    fun removeIgnoredDevice(device: Device): Boolean {
        if (ignoredDevices.contains(device)) {
            return (ignoredDevices as? MutableList<Device>)?.remove(device) ?: false
        }

        return false
    }

}
