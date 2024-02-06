package me.davidgomes.learningcqrspoc.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.UUID

data class PersonEventEnvelope(val event: PersonEvent, val eventId: UUID = UUID.randomUUID())

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(PersonBorn::class, name = "born"),
    JsonSubTypes.Type(PersonAged::class, name = "aged")
)
interface PersonEvent {
    val citizenID: UUID
}

data class PersonBorn(override val citizenID: UUID = UUID.randomUUID(), val name: String) :
    PersonEvent

data class PersonAged(override val citizenID: UUID) : PersonEvent
