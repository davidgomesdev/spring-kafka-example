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
    JsonSubTypes.Type(PeopleAged::class, name = "aged")
)
interface PersonEvent

data class PersonBorn(val citizenID: UUID = UUID.randomUUID(), val name: String) :
    PersonEvent

object PeopleAged : PersonEvent
