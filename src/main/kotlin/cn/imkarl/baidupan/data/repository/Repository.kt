package cn.imkarl.baidupan.data.repository

import cn.imkarl.baidupan.data.api.ApiManager
import cn.imkarl.baidupan.data.local.db.CacheDao
import cn.imkarl.baidupan.model.ResponseWrapper
import cn.imkarl.core.exception.ApiException
import cn.imkarl.core.utils.EncryptUtils
import cn.imkarl.core.utils.JsonUtils
import cn.imkarl.core.utils.LogUtils
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers

/**
 * Repository基类
 * @author imkarl
 */
open class Repository {

    companion object {

        fun <T> createApi(apiClass: Class<T>): T {
            return ApiManager.createApi(apiClass)
        }

        /**
         * 解包
         * @return 取出ResponseWrapper中的data
         */
        fun <T> unwrap(): ObservableTransformer<ResponseWrapper<T>, T> {
            return ObservableTransformer {
                it.flatMap { wrapper ->
                    // 解包之前先判断数据是否非法
                    checkResponse(wrapper)
                    Observable.just<T>(wrapper.data)
                }
            }
        }

        /**
         * 常规处理
         * 默认的线程调度、响应结果的合法性检查
         */
        fun <R> process(): ObservableTransformer<R, R> {
            return ObservableTransformer {
                it.subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap { data ->
                            // 避免没有解包时，无法判断数据是否非法
                            checkResponse(data)
                            Observable.just<R>(data)
                        }
            }
        }


        /**
        * 检查数据是否合法，不合法则抛出异常
        */
        fun checkResponse(response: Any?) {
            if (response is ResponseWrapper<*>) {
                checkResponse(response)
            }
        }
        private fun checkResponse(wrapper: ResponseWrapper<*>) {
            if (!wrapper.isSuccess()) {
                throw ApiException(wrapper.code.toString(), "")
            }
        }

    }

    /**
     * 读取缓存数据
     *
     * @param cacheKey 缓存的KEY（不同数据对应不同的缓存KEY）
     * @param typeToken 实体类型
     * @param expires 缓存有效期
     * @return 如果存在缓存，返回缓存数据；否则读取网络数据并缓存
     */
    inline fun <reified R: Any> cache(cacheKey: String, expires: Long): ObservableTransformer<R, R> {
        val fullCacheKey = EncryptUtils.sha256(this.javaClass.name + "_" + cacheKey)!!
        return ObservableTransformer { observable ->
            // 读取缓存
            val cacheInfo = CacheDao.findByKey(fullCacheKey)
            // 检测缓存是否有效
            if (cacheInfo != null &&
                    cacheInfo.json.isNotEmpty()
                    && cacheInfo.createTime + expires > System.currentTimeMillis()) {
                var result: R? = null
                try {
                    result = JsonUtils.fromJson(cacheInfo.json, object: TypeToken<R>() {})
                } catch (e: Exception) {
                    LogUtils.e(e)
                }

                if (result != null) {
                    return@ObservableTransformer Observable.just(result)
                }
            }

            // 发起原始请求，并写入缓存
            observable
                    .observeOn(Schedulers.io())
                    .map { data ->
                        CacheDao.saveOrUpdate(fullCacheKey, JsonUtils.toJson(data))
                        data
                    }
        }
    }

    fun deleteCache(cacheKey: String) {
        val fullCacheKey = this.javaClass.name + "_" + cacheKey
        CacheDao.deleteByKey(fullCacheKey)
    }

}
