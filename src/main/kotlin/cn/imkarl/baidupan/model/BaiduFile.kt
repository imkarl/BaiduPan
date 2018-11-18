package cn.imkarl.baidupan.model

import com.google.gson.annotations.SerializedName

data class BaiduFile(
        val category: Int = 6, // 6
        val unlist: Int = 0, // 0
        val fs_id: String,  // 非空长整型
        val oper_id: Int = 0,  // 0
        val server_ctime: Long,  // 单位：秒
        val server_mtime: Long,  // 单位：秒  服务器最后更新时间
        val local_ctime: Long,  // 单位：秒
        val local_mtime: Long,  // 单位：秒  本地最后更新时间
        @SerializedName("isdir") val isDir: Boolean,  // 1：文件夹，0：文件
        val share: Int = 0,  // 0
        //@SerializedName("dir_empty") val isEmptyDir: Boolean,  // 为文件时没有该字段【1：空文件夹，0：非空文件夹】
        val path: String,  // 路径
        @SerializedName("server_filename") var filename: String,  // 文件名
        val size: Long,  // 为文件夹时为0 【单位：byte】
        val md5: String?  // 为文件夹时没有该字段 【32位md5】
)

data class BaiduFileList(
        val list: ArrayList<BaiduFile>
)
