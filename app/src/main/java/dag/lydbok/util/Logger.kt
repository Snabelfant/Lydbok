package dag.lydbok.util

import android.util.Log
import com.fasterxml.jackson.core.JsonProcessingException

object Logger {
    private val mapper = JsonMapper()
    private var isTest = false

    fun info(any: Any) {
        log(false, any)
    }

    fun error(any: Any) {
        log(true, any)
    }

    private fun log(isError: Boolean, any: Any?) {
        if (isTest) {
            println(toLogString(any, true))
        } else {
            if (isError) {
                Log.e("ZZZ", toLogString(any))
            } else {
                Log.i("ZZZ", toLogString(any))
            }
        }
    }

    private fun toLogString(any: Any?, pretty: Boolean = false) =
        if (any != null) {
            try {
                ": ${mapper.write(any, pretty)}"
            } catch (e: JsonProcessingException) {
                " !! ${e.message}"
            }
        } else {
            ""
        }

    fun test() {
        isTest = true
    }
}
