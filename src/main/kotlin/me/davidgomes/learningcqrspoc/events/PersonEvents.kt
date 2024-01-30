package me.davidgomes.learningcqrspoc.events

import java.util.UUID

data class PersonBorn(val name: String)

data class PersonAged(val citizenID: UUID)
