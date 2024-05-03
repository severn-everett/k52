package com.severett.k52.serde

import com.severett.k52.util.toDateTimeString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

class InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toDateTimeString())
    }

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}