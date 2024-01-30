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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,
    @Column(unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    val citizenID: UUID? = null,
    val name: String,
    val age: UInt
)
