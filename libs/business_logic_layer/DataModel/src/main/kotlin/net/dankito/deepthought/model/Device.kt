package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.DeviceTableName)
data class Device(
        @Column(name = TableConfig.DeviceUniversallyUniqueIdColumnName)
        var universallyUniqueId: String,

        @Column(name = TableConfig.DeviceNameColumnName)
        var name: String,

        @Column(name = TableConfig.DeviceDescriptionColumnName)
        var description: String,

        @Column(name = TableConfig.DevicePlatformColumnName)
        var platform: String = "",

        @Column(name = TableConfig.DeviceOsVersionColumnName)
        var osVersion: String = "",

        @Column(name = TableConfig.DevicePlatformArchitectureColumnName)
        var platformArchitecture: String = ""

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
    var groups: MutableSet<Group> = HashSet()
        private set

    @Column(name = TableConfig.DeviceIconColumnName)
    @Lob
    var deviceIcon: ByteArray? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = TableConfig.DeviceDeepThoughtApplicationJoinColumnName)
    var application: DeepThoughtApplication? = null
        internal set


    private constructor() : this("", "", "")



    fun addUser(user: User): Boolean {
        if (users.contains(user) == false) {
            if (users.add(user)) {
                user.addDevice(this)

                return true
            }
        }

        return false
    }

    fun removeUser(user: User): Boolean {
        if (users.contains(user) == true) {
            if (users.remove(user)) {
                user.removeDevice(this)

                return true
            }
        }

        return false
    }



    fun addGroup(group: Group): Boolean {
        if (groups.contains(group) == false) {
            if (groups.add(group)) {
                group.addDevice(this)

                return true
            }
        }

        return false
    }

    fun removeGroup(group: Group): Boolean {
        if (groups.contains(group) == true) {
            if (groups.remove(group)) {
                group.removeDevice(this)

                return true
            }
        }

        return false
    }

}
