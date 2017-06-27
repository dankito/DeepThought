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

}