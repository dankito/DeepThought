package net.dankito.deepthought.model


import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.GroupTableName)
data class Group(
        @Column(name = TableConfig.GroupNameColumnName)
        var name: String
) : UserDataEntity() {

    companion object {
        private const val serialVersionUID = 6783280420819608640L
    }


    @Column(name = TableConfig.GroupUniversallyUniqueIdColumnName)
    var universallyUniqueId = ""

    @Column(name = TableConfig.GroupDescriptionColumnName)
    var description = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Entry?
    var users: MutableSet<User> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY)/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/
    @JoinTable(name = TableConfig.GroupDeviceJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.GroupDeviceJoinTableGroupIdColumnName)/*, referencedColumnName = "id"*/), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.GroupDeviceJoinTableDeviceIdColumnName)/*, referencedColumnName = "id"*/))
    var devices: MutableSet<Device> = HashSet()
        private set

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.GroupDeepThoughtApplicationJoinColumnName)
    var application: DeepThoughtApplication? = null
        internal set


    private constructor() : this("")

    constructor(name: String, universallyUniqueId: String, owner: User) : this(name) {
        this.universallyUniqueId = universallyUniqueId
        this.owner = owner
    }


    fun addUser(user: User): Boolean {
        if (users.contains(user) == false) {
            if (users.add(user)) {
                user.addGroup(this)

                return true
            }
        }

        return false
    }

    fun removeUser(user: User): Boolean {
        if (users.contains(user) == true) {
            if (users.remove(user)) {
                user.removeGroup(this)

                return true
            }
        }

        return false
    }


    fun hasDevices(): Boolean {
        return devices.size > 0
    }

    fun addDevice(device: Device): Boolean {
        if (devices.contains(device) == false) {
            if (devices.add(device)) {
                device.addGroup(this)

                return true
            }
        }

        return false
    }

    fun removeDevice(device: Device): Boolean {
        if (devices.contains(device) == true) {
            if (devices.remove(device)) {
                device.removeGroup(this)

                return true
            }
        }

        return false
    }


}
