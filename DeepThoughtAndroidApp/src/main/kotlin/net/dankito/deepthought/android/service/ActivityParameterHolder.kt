package net.dankito.deepthought.android.service


class ActivityParameterHolder {

    private val resultHolder = HashMap<String, Any>()


    fun setActivityResult(id: String, result: Any) {
        resultHolder.put(id, result)
    }

    fun getActivityResult(id: String): Any? {
        return resultHolder[id]
    }

    fun clearActivityResults() {
        resultHolder.clear()
    }

}