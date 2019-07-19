package dag.lydbok.util

import android.util.Log

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

object Logger {
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val writerPrettyPrint: ObjectWriter = objectMapper.writerWithDefaultPrettyPrinter()
    private val writerLogFile: ObjectWriter = objectMapper.writer()
    private var isTest = false

    init {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.registerModule(JavaTimeModule())
    }

    fun info(any: Any) {
        log(false, any)
    }

    fun error(any: Any) {
        log(true, any)
    }

    private fun log(isError: Boolean, any: Any?) {
        if (isTest) {
            println(toLogString(writerPrettyPrint, any))
        } else {
            if (isError) {
                Log.e("ZZZ", toLogString(writerLogFile, any))
            } else {
                Log.i("ZZZ", toLogString(writerLogFile, any))
            }
        }
    }

    private fun toLogString(objectWriter: ObjectWriter, any: Any?) =
        if (any != null) {
            try {
                ": ${objectWriter.writeValueAsString(any)}"
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
