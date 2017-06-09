package net.dankito.data_access.database


data class EntityManagerConfiguration constructor(val dataFolder: String, val databaseName: String) {

    var entityClasses = listOf<Class<*>>()

}
