package net.dankito.deepthought.model

import net.dankito.deepthought.model.config.TableConfig
import net.dankito.deepthought.model.enums.ApplicationLanguage
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity(name = TableConfig.DeepThoughtApplicationTableName)
data class DeepThoughtApplication(

        @OneToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
        @JoinColumn(name = TableConfig.DeepThoughtApplicationLastLoggedOnUserJoinColumnName)
        var lastLoggedOnUser: User,

        //  @Column(name = TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
        @Column(name = TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName)
        var autoLogOnLastLoggedOnUser: Boolean = false

) : BaseEntity(), Serializable {


    companion object {
        private const val serialVersionUID = -3232937271770851228L
    }



    @OneToMany(fetch = FetchType.LAZY, mappedBy = "application", cascade = arrayOf(CascadeType.PERSIST))
    var users: MutableSet<User> = HashSet()
        private set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "application", cascade = arrayOf(CascadeType.PERSIST))
    var groups: MutableSet<Group> = HashSet()
        private set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "application", cascade = arrayOf(CascadeType.PERSIST))
    var devices: MutableSet<Device> = HashSet()
        private set

    @OneToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
    @JoinColumn(name = TableConfig.DeepThoughtApplicationLocalDeviceJoinColumnName)
    var localDevice: Device? = null
        internal set


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "application", cascade = arrayOf(CascadeType.PERSIST))
    @OrderBy(value = "sortOrder")
    var applicationLanguages: MutableSet<ApplicationLanguage> = HashSet<ApplicationLanguage>() // these are the Languages the UI can display
        private set


    private constructor() : this(User(""), false)


    fun addUser(user: User): Boolean {
        if (users.add(user)) {
            user.application = this
            return true
        }

        return false
    }

    fun removeUser(user: User): Boolean {
        if (users.remove(user)) {
            user.application = null
            return true
        }

        return false
    }


    fun addGroup(group: Group): Boolean {
        if (groups.add(group)) {
            group.application = this
            return true
        }

        return false
    }

    fun removeGroup(group: Group): Boolean {
        if (groups.remove(group)) {
            group.application = null
            return true
        }

        return false
    }


    fun addDevice(device: Device): Boolean {
        if (devices.add(device)) {
            device.application = this
            return true
        }

        return false
    }

    fun removeDevice(device: Device): Boolean {
        if (localDevice == device) { // don't delete local device!
            return false
        }

        if (devices.remove(device)) {
            device.application = null
            return true
        }

        return false
    }

}
