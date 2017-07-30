package net.dankito.data_access.database


data class EntityManagerConfiguration(val dataFolder: String, val databaseName: String, var entityClasses: List<Class<*>> = listOf<Class<*>>())
