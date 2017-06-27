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


    private constructor() : this("", "")


    @Column(name = TableConfig.UserFirstNameColumnName)
    var firstName: String = ""

    @Column(name = TableConfig.UserLastNameColumnName)
    var lastName: String = ""

    @Column(name = TableConfig.UserPasswordColumnName)
    var password: String = ""


    @OneToOne(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @JoinColumn(name = TableConfig.UserLastViewedDeepThoughtColumnName)
    var lastViewedDeepThought: DeepThought? = null

    //  @OneToMany(fetch = FetchType.EAGER, mappedBy = "deviceOwner"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
    @ManyToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH))
    @JoinTable(name = TableConfig.UserDeviceJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.UserDeviceJoinTableUserIdColumnName)/*, referencedColumnName = "id"*/), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.UserDeviceJoinTableDeviceIdColumnName)/*, referencedColumnName = "id"*/))
    var devices: MutableSet<Device> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.USER_SYNCHRONIZED_DEVICES_JOIN_TABLE_NAME,
            joinColumns = arrayOf( JoinColumn(name = TableConfig.USER_SYNCHRONIZED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME) ),
            inverseJoinColumns = arrayOf( JoinColumn(name = TableConfig.USER_SYNCHRONIZED_DEVICES_DEVICE_ID_COLUMN_NAME) ))
    var synchronizedDevices: List<Device> = ArrayList()
        private set

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = TableConfig.USER_IGNORED_DEVICES_JOIN_TABLE_NAME,
            joinColumns = arrayOf( JoinColumn(name = TableConfig.USER_IGNORED_DEVICES_LOCAL_CONFIG_ID_COLUMN_NAME) ),
            inverseJoinColumns = arrayOf( JoinColumn(name = TableConfig.USER_IGNORED_DEVICES_DEVICE_ID_COLUMN_NAME) ))
    var ignoredDevices: List<Device> = ArrayList()
        private set



    fun hasDevices(): Boolean {
        return devices.size > 0
    }

    fun containsDevice(device: Device): Boolean {
        return devices.contains(device)
    }

    fun addDevice(device: Device): Boolean {
        if(devices.contains(device) == false) {
            return devices.add(device)
        }

        return false
    }

    fun removeDevice(device: Device): Boolean {
        if(devices.contains(device) == true) {
            return devices.remove(device)
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