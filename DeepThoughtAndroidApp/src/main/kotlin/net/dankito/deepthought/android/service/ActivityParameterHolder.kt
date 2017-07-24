package net.dankito.deepthought.android.service

import java.util.*


class ActivityParameterHolder {

    private val parametersHolder = HashMap<String, Any>()

    private val resultHolder = HashMap<String, Any>()


    fun setParameters(parameters: Any): String {
        val id = createId()

        parametersHolder.put(id, parameters)

        return id
    }

    fun getParameters(id: String): Any? {
        return parametersHolder[id]
    }

    fun clearParameters(id: String) {
        parametersHolder.remove(id)
    }


    fun setActivityResult(id: String, result: Any) {
        resultHolder.put(id, result)
    }

    fun getActivityResult(id: String): Any? {
        return resultHolder[id]
    }

    fun clearActivityResults(vararg resultIdsToClear: String) {
        resultIdsToClear.forEach { resultHolder.remove(it) }
    }

    fun clearActivityResults() {
        resultHolder.clear()
    }


    private fun createId(): String {
        return UUID.randomUUID().toString()
    }

}