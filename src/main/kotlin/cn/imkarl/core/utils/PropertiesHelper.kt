package cn.imkarl.core.utils

import com.google.gson.JsonElement
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

/**
 * Properties辅助类
 * @author imkarl
 */
class PropertiesHelper(private val file: File) {

    private val properties: Properties = Properties()
    private val editor by lazy { EditorImpl() }

    init {
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        properties.load(FileInputStream(file))
    }

    fun getString(key: String): String? {
        return properties.getProperty(key, null)
    }
    fun getString(key: String, defVaule: String): String {
        return properties.getProperty(key, defVaule)
    }

    fun getJsonElement(key: String): JsonElement? {
        val result = properties.getProperty(key, null)
        if (result.isNullOrEmpty()) {
            return null
        }
        try {
            return JsonUtils.fromJson(result, JsonElement::class.java)
        } catch (e: Exception) {
            return null
        }
    }
    fun getJsonElement(key: String, defVaule: JsonElement): JsonElement {
        val result = properties.getProperty(key, null)
        if (result.isNullOrEmpty()) {
            return defVaule
        }
        try {
            return JsonUtils.fromJson(result, JsonElement::class.java)
        } catch (e: Exception) {
            return defVaule
        }
    }

    fun <T: Any> getObj(key: String, classOfT: Class<T>): T? {
        val json = getJsonElement(key) ?: return null
        try {
            return JsonUtils.fromJson(json, classOfT)
        } catch (e: Exception) {
            return null
        }
    }
    fun <T: Any> getObj(key: String, classOfT: Type): T? {
        val json = getJsonElement(key) ?: return null
        try {
            return JsonUtils.fromJson(json, classOfT)
        } catch (e: Exception) {
            return null
        }
    }
    fun <T: Any> getObj(key: String, defVaule: T): T {
        val json = getJsonElement(key) ?: return defVaule
        try {
            return JsonUtils.fromJson(json, defVaule.javaClass)
        } catch (e: Exception) {
            return defVaule
        }
    }

    fun getAll(): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        properties.stringPropertyNames()
                .map { key ->
                    val value = getString(key, "")
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        map.put(key, value)
                    }
                }
        return map
    }

    fun edit(): Editor {
        return editor
    }
    fun edit(block: Editor.() -> Unit) {
        block.invoke(editor)
        editor.commit()
    }


    fun <T: Any> field(key: String = ""): ReadWriteProperty<Any, T?> {
        return object: ReadWriteProperty<Any, T?> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T? {
                return getObj(if (key.isNotEmpty()) key else property.name, property.returnType.javaType)
            }
            override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
                edit {
                    put(if (key.isNotEmpty()) key else property.name, value)
                }
            }
        }
    }
    fun string(key: String = ""): ReadWriteProperty<Any, String?> {
        return object: ReadWriteProperty<Any, String?> {
            override fun getValue(thisRef: Any, property: KProperty<*>): String? {
                return getString(if (key.isNotEmpty()) key else property.name)
            }
            override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
                edit {
                    put(if (key.isNotEmpty()) key else property.name, value)
                }
            }
        }
    }


    private inner class EditorImpl: Editor {
        override fun put(key: String, value: String?): Editor {
            if (key.isNotEmpty()) {
                if (value.isNullOrEmpty()) {
                    remove(key)
                } else {
                    properties.setProperty(key, value)
                }
            }
            return this
        }

        override fun put(key: String, value: JsonElement?): Editor {
            return put(key, value?.toString())
        }

        override fun put(key: String, value: Any?): Editor {
            return put(key, if (value != null) JsonUtils.toJson(value) else null)
        }

        override fun remove(key: String): Editor {
            if (key.isNotEmpty()) {
                properties.remove(key)
            }
            return this
        }

        override fun clear(): Editor {
            properties.clear()
            return this
        }

        override fun commit() {
            properties.store(FileOutputStream(file), null)
        }
    }

    interface Editor {
        fun put(key: String, value: String?): Editor
        fun put(key: String, value: JsonElement?): Editor
        fun put(key: String, value: Any?): Editor
        fun remove(key: String): Editor
        fun clear(): Editor

        fun commit()
    }

}
