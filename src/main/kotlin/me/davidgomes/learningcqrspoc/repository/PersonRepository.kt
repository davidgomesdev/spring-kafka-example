package me.davidgomes.learningcqrspoc.repository

import me.davidgomes.learningcqrspoc.entity.PersonEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PersonRepository : CrudRepository<PersonEntity, ULong> {
    fun findByCitizenID(citizenID: UUID): PersonEntity?
}
