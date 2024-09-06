package me.davidgomes.learningcqrspoc.service

import me.davidgomes.learningcqrspoc.entity.PersonEntity
import me.davidgomes.learningcqrspoc.event.PeopleAged
import me.davidgomes.learningcqrspoc.event.PersonBorn
import me.davidgomes.learningcqrspoc.event.PersonEventEnvelope
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Named
import org.apache.kafka.streams.processor.api.FixedKeyProcessor
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext
import org.apache.kafka.streams.processor.api.FixedKeyRecord
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component

@Component
class PeopleProcessor {

    @Autowired
    fun buildPipeline(builder: StreamsBuilder) {
        builder.stream(
            PERSON_TOPIC,
            Consumed.with(
                Serdes.ByteArray(),
                Serdes.serdeFrom(
                    JsonSerializer(),
                    ErrorHandlingDeserializer(
                        JsonDeserializer<PersonEventEnvelope>()
                            .trustedPackages(PersonEventEnvelope::class.java.packageName)
                    )
                )
            )
        )
            .processValues({
                object : FixedKeyProcessor<ByteArray, PersonEventEnvelope, PersonEventEnvelope> {
                    lateinit var context: FixedKeyProcessorContext<ByteArray, PersonEventEnvelope>
                    lateinit var store: KeyValueStore<ByteArray, PersonEntity>

                    override fun init(context: FixedKeyProcessorContext<ByteArray, PersonEventEnvelope>) {
                        super.init(context)
                        this.context = context
                        store = context.getStateStore("persons")
                    }

                    override fun process(record: FixedKeyRecord<ByteArray, PersonEventEnvelope>) {
                        val event = record.value().event

                        if (event is PeopleAged) {
                            store.all().forEachRemaining { store.put(it.key, it.value.apply { age++ }) }
                        } else if (event is PersonBorn) {
                            store.put(record.key(), PersonEntity(event.citizenID, event.name, 0))
                        }

                        context.forward(record)
                    }
                }
            }, "persons")
            .to("test")
//            .toTable(Named.`as`("persons"))
    }
}
