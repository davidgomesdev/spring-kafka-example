package me.davidgomes.learningcqrspoc.service

import me.davidgomes.learningcqrspoc.dto.Person
import me.davidgomes.learningcqrspoc.event.PersonAged
import me.davidgomes.learningcqrspoc.event.PersonBorn
import me.davidgomes.learningcqrspoc.event.PersonEventEnvelope
import me.davidgomes.learningcqrspoc.repository.PersonRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.UUID

const val PERSON_TOPIC = "person-event"

@Service
class PersonService(
    private val template: KafkaTemplate<ByteArray, PersonEventEnvelope>,
    private val repository: PersonRepository
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun createPerson(name: String, initialAge: Int = 0): UUID {
        val event = PersonBorn(UUID.randomUUID(), name, initialAge)

        produceEvent(event)

        logger.info("Person was born (event id = {})", event.citizenID)

        return event.citizenID
    }

    fun getPerson(citizenID: UUID): Person? {
        val entity = repository.findByCitizenID(citizenID) ?: return null

        return Person(entity.citizenID, entity.name, entity.age)
    }

    @Scheduled(fixedRate = 2_000)
    fun agePeople() {
        logger.info("Aging people (one year on earth is 2 seconds on a POC)")
        repository.findAll()
            .map { PersonAged(it.citizenID) }
            .forEach {
                val citizenID = it.citizenID

                runCatching {
                    template.send(
                        PERSON_TOPIC,
                        it.toString().encodeToByteArray(),
                        PersonEventEnvelope(citizenID, it)
                    )
                        .get()
                }.onFailure { ex -> logger.error("There was a failure sending aged person (citizenID '${it.citizenID}')", ex) }
            }
    }

    private fun produceEvent(event: Any): UUID {
        val citizenID = UUID.randomUUID()
        val eventEnvelope = when (event) {
            is PersonBorn -> PersonEventEnvelope(citizenID, event)
            is PersonAged -> PersonEventEnvelope(citizenID, event)
            else -> throw IllegalArgumentException("Only person events allowed")
        }

        template.send(PERSON_TOPIC, citizenID.toString().encodeToByteArray(), eventEnvelope).get()

        return eventEnvelope.eventId
    }
}
