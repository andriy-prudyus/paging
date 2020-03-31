package com.example.paging.architecture.exception

/**
 * Wrapper for an exception
 *
 * @param code error code
 * @param originalThrowable exception that has to be wrapped
 */
class AppException(
    val code: Code,
    originalThrowable: Throwable? = null
) : Exception(originalThrowable) {

    override val message: String? = "errorCode=${code.name}; message=${originalThrowable?.message}"

    enum class Code {
        INCORRECT_INITIAL_PAGE_INDEX,
        UNKNOWN_EXPECTED_ITEMS_COUNT,
        CACHED_ITEMS_MORE_THAN_EXPECTED,
        INCORRECT_ITEM_ID
    }
}