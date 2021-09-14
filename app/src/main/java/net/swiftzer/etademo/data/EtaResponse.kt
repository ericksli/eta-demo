package net.swiftzer.etademo.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EtaResponse(
    /**
     * system status code.
     */
    @SerialName("status") val status: Int = STATUS_ERROR_OR_ALERT,
    /**
     * Alert message.
     */
    @SerialName("message") val message: String = "",
    /**
     * URL for Special Train Services Arrangement case.
     */
    @SerialName("url") val url: String = "",
    /**
     * Indicate if the train is delayed.
     */
    @SerialName("isdelay") val isDelay: String = IS_DELAY_FALSE,
    @SerialName("data") val data: Map<String, Data> = emptyMap()
) {
    @Serializable
    data class Data(
        /**
         * Indicate the destinations of the train in the specific line (up trip).
         */
        @SerialName("UP") val up: List<Eta> = emptyList(),
        /**
         * Indicate the destinations of the train in the specific line (down trip).
         */
        @SerialName("DOWN") val down: List<Eta> = emptyList()
    )

    @Serializable
    data class Eta(
        /**
         * Platform numbers for the departure / arrival train.
         */
        @SerialName("plat") val plat: String = "1",
        /**
         * Estimated arrival time (or departure time) of the train.
         */
        @SerialName("time") val time: String = EMPTY_TIMESTAMP,
        /**
         * MTR Station Code in capital letters.
         */
        @SerialName("dest") val dest: String = "",
        /**
         * The sequence of the 4 upcoming trains.
         */
        @SerialName("seq") val seq: String = "0"
    )

    companion object {
        const val EMPTY_TIMESTAMP = "-"

        const val STATUS_NORMAL = 1
        const val STATUS_ERROR_OR_ALERT = 0
        const val IS_DELAY_TRUE = "Y"
        const val IS_DELAY_FALSE = "N"
    }
}
