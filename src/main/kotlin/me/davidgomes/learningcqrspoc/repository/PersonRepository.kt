package me.davidgomes.learningcqrspoc.repository

import me.davidgomes.learningcqrspoc.entity.PersonEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface PersonRepository : CrudRepository<PersonEntity, ULong> {
    fun findByCitizenID(citizenID: UUID): PersonEntity?

    @Transactional
    @Modifying
    @Query("update PersonEntity p set p.age = p.age + 1")
    fun incrementPeopleAge()
}
