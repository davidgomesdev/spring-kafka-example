package me.davidgomes.learningcqrspoc.repository

import me.davidgomes.learningcqrspoc.entity.PersonEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : CrudRepository<PersonEntity, ULong>
