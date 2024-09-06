package me.davidgomes.learningcqrspoc.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "persons")
class PersonEntity(
    @Column(unique = true, updatable = false)
    val citizenID: UUID,
    val name: String,
    var age: Int,
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,
)
