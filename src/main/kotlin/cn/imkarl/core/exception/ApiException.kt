package cn.imkarl.core.exception

class ApiException(val errorCode: String, val errorMessage: String)
    : Exception("errorCode: $errorCode, errorMessage: $errorMessage")
