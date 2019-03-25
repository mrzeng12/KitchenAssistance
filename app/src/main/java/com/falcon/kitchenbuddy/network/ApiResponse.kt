package com.falcon.kitchenbuddy.network

import com.falcon.kitchenbuddy.network.ApiResult.API_ERROR
import com.falcon.kitchenbuddy.network.ApiResult.API_SUCCESS


/**
 * A generic class that holds a value with API response status.
 * @param <T>
</T> */
data class ApiResponse<out T>(val status: ApiResult, val body: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse(API_SUCCESS, data, null)
        }

        fun <T> error(msg: String): ApiResponse<T> {
            return ApiResponse(API_ERROR, null, msg)
        }
    }
}
