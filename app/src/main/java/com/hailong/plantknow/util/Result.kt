package com.hailong.plantknow.util

/**
 * 通用的结果包装类
 * 用于封装异步操作的成功和失败结果，提供类型安全的处理方式
 *
 * 设计模式：密封类 + 数据类，实现类似Kotlin标准库Result的功能
 *
 * @param T 成功时携带的数据类型
 */
sealed class Result<out T> {

    /**
     * 成功状态，包含操作返回的数据
     *
     * @param data 成功时返回的数据，类型为T
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * 错误状态，包含操作失败的异常信息
     *
     * @param exception 导致失败的异常对象
     */
    data class Error(val exception: Exception) : Result<Nothing>()
}