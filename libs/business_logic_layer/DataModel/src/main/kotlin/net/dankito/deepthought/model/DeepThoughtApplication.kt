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

        @OneToOne(fetch = FetchType.EAGER, cascade = arrayOf(CascadeType.PERSIST))
        @JoinColumn(name = TableConfig.DeepThoughtApplicationLocalDeviceJoinColumnName)
        val localDevice: Device,

        //  @Column(name = TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
        @Column(name = TableConfig.DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName)
        var autoLogOnLastLoggedOnUser: Boolean = false

) : BaseEntity(), Serializable {


    companion object {
        private const val serialVersionUID = -3232937271770851228L
    }



    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var users: MutableSet<User> = HashSet()
        private set

    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var groups: MutableSet<UsersGroup> = HashSet()
        private set

    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    var devices: MutableSet<Device> = HashSet()
        private set


    @OneToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.PERSIST))
    @OrderBy(value = "sortOrder")
    var applicationLanguages: MutableSet<ApplicationLanguage> = HashSet<ApplicationLanguage>() // these are the Languages the UI can display
        private set


    private constructor() : this(User("", ""), Device(), false)


    fun addUser(user: User): Boolean {
        if (users.add(user)) {
            return true
        }

        return false
    }

    fun removeUser(user: User): Boolean {
        if (users.remove(user)) {
            return true
        }

        return false
    }


    fun addGroup(group: UsersGroup): Boolean {
        if (groups.add(group)) {
            return true
        }

        return false
    }

    fun removeGroup(group: UsersGroup): Boolean {
        if (groups.remove(group)) {
            return true
        }

        return false
    }


    fun addDevice(device: Device): Boolean {
        if (devices.add(device)) {
            return true
        }

        return false
    }

    fun removeDevice(device: Device): Boolean {
        if (localDevice == device) { // don't delete local device!
            return false
        }

        if (devices.remove(device)) {
            return true
        }

        return false
    }

}
