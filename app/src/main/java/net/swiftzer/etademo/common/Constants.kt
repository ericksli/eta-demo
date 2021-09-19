package net.swiftzer.etademo.common

import java.time.ZoneId
import kotlin.time.Duration

val DEFAULT_TIMEZONE: ZoneId = ZoneId.of("Asia/Hong_Kong")
val STATE_FLOW_STOP_TIMEOUT_MILLIS = Duration.seconds(5)
