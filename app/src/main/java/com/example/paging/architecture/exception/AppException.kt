package com.example.paging.architecture.exception

import java.io.IOException

class AppException(val code: Code) : IOException(code.name) {

    enum class Code {
        INCORRECT_INITIAL_PAGE_INDEX,
        UNKNOWN_EXPECTED_ITEMS_COUNT,
        CACHED_ITEMS_MORE_THAN_EXPECTED,
        INCORRECT_ITEM_ID
    }
}