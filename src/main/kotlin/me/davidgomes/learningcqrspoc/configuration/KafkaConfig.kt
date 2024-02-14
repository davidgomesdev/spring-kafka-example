package me.davidgomes.learningcqrspoc.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.davidgomes.learningcqrspoc.service.ORDER_TOPIC
import me.davidgomes.learningcqrspoc.service.PERSON_TOPIC
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapAddress: String
) {

    @Bean
    fun producerFactory(): ProducerFactory<ByteArray, Any> {
        return DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
            ),
            ByteArraySerializer(),
            JsonSerializer(jacksonObjectMapper())
        )
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<ByteArray, Any> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<ByteArray, Any> {
        return DefaultKafkaConsumerFactory(
            mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
                ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to ByteArrayDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java,
                "properties.${JsonDeserializer.TRUSTED_PACKAGES}" to "*",
                ConsumerConfig.GROUP_ID_CONFIG to "learning-cqrs-poc",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
            ),
            ByteArrayDeserializer(),
            ErrorHandlingDeserializer(JsonDeserializer<Any>(jacksonObjectMapper()).trustedPackages("*"))
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<ByteArray, Any> {
        return ConcurrentKafkaListenerContainerFactory<ByteArray, Any>().also {
            it.consumerFactory = consumerFactory()
        }
    }

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        return KafkaAdmin(
            mapOf(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
            )
        )
    }

    @Bean
    fun personsTopic(): NewTopic {
        return TopicBuilder.name(PERSON_TOPIC)
            .partitions(10)
            .build()
    }

    @Bean
    fun personsDeadletterTopic(): NewTopic {
        return TopicBuilder.name("$PERSON_TOPIC-deadletter")
            .build()
    }

    @Bean
    fun ordersTopic(): NewTopic {
        return TopicBuilder.name(ORDER_TOPIC)
            .partitions(10)
            .build()
    }

    @Bean
    fun ordersDeadletterTopic(): NewTopic {
        return TopicBuilder.name("$ORDER_TOPIC-deadletter")
            .build()
    }
}
