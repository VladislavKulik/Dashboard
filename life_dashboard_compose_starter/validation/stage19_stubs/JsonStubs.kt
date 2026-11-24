package org.json
class JSONObject {
    companion object { val NULL: Any = Any() }
    constructor()
    constructor(json: String)
    fun put(key: String, value: Any?): JSONObject = this
    fun opt(key: String): Any? = null
    fun optInt(key: String, fallback: Int = 0): Int = fallback
    fun optBoolean(key: String, fallback: Boolean = false): Boolean = fallback
    fun optString(key: String, fallback: String = ""): String = fallback
    fun optJSONObject(key: String): JSONObject? = null
    fun optJSONArray(key: String): JSONArray? = null
    override fun toString(): String = "{}"
    fun toString(indentSpaces: Int): String = "{}"
}
class JSONArray {
    constructor()
    fun put(value: Any?): JSONArray = this
    fun length(): Int = 0
    fun optJSONObject(index: Int): JSONObject? = null
}
