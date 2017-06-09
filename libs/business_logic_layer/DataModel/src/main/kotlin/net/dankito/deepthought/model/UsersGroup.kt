package net.dankito.deepthought.model


import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.GroupTableName)
data class UsersGroup(

        @Column(name = TableConfig.GroupNameColumnName)
        var name: String,

        @Column(name = TableConfig.GroupUniversallyUniqueIdColumnName)
        var universallyUniqueId: String

) : UserDataEntity() {

    companion object {
        private const val serialVersionUID = 6783280420819608640L
    }


    @Column(name = TableConfig.GroupDescriptionColumnName)
    var description = ""

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Entry?
    var users: MutableSet<User> = HashSet()
        private set

    @ManyToMany(fetch = FetchType.LAZY)/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/
    @JoinTable(name = TableConfig.GroupDeviceJoinTableName, joinColumns = arrayOf(JoinColumn(name = TableConfig.GroupDeviceJoinTableGroupIdColumnName)/*, referencedColumnName = "id"*/), inverseJoinColumns = arrayOf(JoinColumn(name = TableConfig.GroupDeviceJoinTableDeviceIdColumnName)/*, referencedColumnName = "id"*/))
    var devices: MutableSet<Device> = HashSet()
        private set


    private constructor() : this("", "")


    internal fun addUser(user: User): Boolean {
        return users.add(user)
    }

    internal fun removeUser(user: User): Boolean {
        return users.remove(user)
    }


    fun hasDevices(): Boolean {
        return devices.size > 0
    }

    internal fun addDevice(device: Device): Boolean {
        return devices.add(device)
    }

    internal fun removeDevice(device: Device): Boolean {
        return devices.remove(device)
    }


}
