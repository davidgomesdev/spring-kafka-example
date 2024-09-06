package me.davidgomes.learningcqrspoc.service

import me.davidgomes.learningcqrspoc.dto.Person
import me.davidgomes.learningcqrspoc.entity.PersonEntity
import me.davidgomes.learningcqrspoc.event.PeopleAged
import me.davidgomes.learningcqrspoc.event.PersonBorn
import me.davidgomes.learningcqrspoc.event.PersonEvent
import me.davidgomes.learningcqrspoc.event.PersonEventEnvelope
import me.davidgomes.learningcqrspoc.repository.PersonRepository
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.UUID

const val PERSON_TOPIC = "person-event"

@Service
class PersonService(
    private val template: KafkaTemplate<ByteArray, Any>,
    private val repository: PersonRepository,
    private val personsStore: ReadOnlyKeyValueStore<ByteArray, PersonEntity>
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun createPerson(name: String): UUID {
        val event = PersonBorn(name = name)
        val eventEnvelope = PersonEventEnvelope(event)

        template.send(PERSON_TOPIC, event.citizenID.toString().encodeToByteArray(), eventEnvelope).get()

        logger.info("Person was born (event id = {})", eventEnvelope.eventId)

        return event.citizenID
    }

    fun getPerson(citizenID: UUID): Person? {
        val entity = personsStore[citizenID.toString().encodeToByteArray()] ?: return null

        return Person(entity.citizenID, entity.name, entity.age)
    }

    @Scheduled(fixedRate = 2_000)
    fun agePeople() {
        logger.info("Aging people (one year on earth is 2 seconds on a POC)")

        val event = PeopleAged
        val eventEnvelope = PersonEventEnvelope(event)

        template.send(PERSON_TOPIC, event.toString().encodeToByteArray(), eventEnvelope).get()
    }
}
