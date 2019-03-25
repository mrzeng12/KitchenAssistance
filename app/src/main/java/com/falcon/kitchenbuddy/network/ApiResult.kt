package com.falcon.kitchenbuddy.network

/**
 * Status of a resource that is provided to the repository.
 *
 *
 * These are usually created by the Service classes where they return
 * `LiveData<APIResource<T>>` to pass back the latest body to the Repository with its fetch status.
 */
enum class ApiResult {
    API_SUCCESS,
    API_ERROR
}
