package me.davidgomes.learningcqrspoc.service

import me.davidgomes.learningcqrspoc.entity.PersonEntity
import me.davidgomes.learningcqrspoc.event.PeopleAged
import me.davidgomes.learningcqrspoc.event.PersonBorn
import me.davidgomes.learningcqrspoc.event.PersonEventEnvelope
import me.davidgomes.learningcqrspoc.repository.PersonRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.DltHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class PersonSourcer(
    private val repository: PersonRepository
) {

    private val log = LoggerFactory.getLogger(this::class.java)

//    @RetryableTopic(attempts = "2", kafkaTemplate = "kafkaTemplate")
//    @KafkaListener(topics = [PERSON_TOPIC])
    fun consume(envelope: PersonEventEnvelope) {
        when (val event = envelope.event) {
            is PersonBorn -> {
                repository.save(PersonEntity(event.citizenID, event.name, 0))
                log.info("Created new person ({})", event.citizenID)
            }

            is PeopleAged -> {
                repository.incrementPeopleAge()
                log.info("Aged everyone")
            }
        }
    }

    @DltHandler
    fun handleDeadletters(envelope: PersonEventEnvelope, @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String) {
        log.warn("Received event '${envelope.event}' of topic '$topic' (id = ${envelope.eventId})")
    }
}
