package me.davidgomes.learningcqrspoc.configuration

import me.davidgomes.learningcqrspoc.service.PERSON_TOPIC
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {

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
}
