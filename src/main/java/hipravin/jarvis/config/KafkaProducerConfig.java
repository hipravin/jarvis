package hipravin.jarvis.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig implements InitializingBean {

    private final KafkaProperties kafkaProperties;
    private final ProducerFactory<?,?> producerFactory;

    public KafkaProducerConfig(KafkaProperties kafkaProperties, ProducerFactory<?, ?> producerFactory) {
        this.kafkaProperties = kafkaProperties;
        this.producerFactory = producerFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        if(producerFactory instanceof DefaultKafkaProducerFactory<?,?> f) {
//            f.setProducerPerThread(true);
//        }
    }

//    @Bean
//    public ProducerFactory<String, ClockTickEvent> producerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(
//                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//                kafkaProperties.getBootstrapServers());
//        configProps.put(
//                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                StringSerializer.class);
//        configProps.put(
//                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                JacksonJsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean
//    public KafkaTemplate<String, ClockTickEvent> kafkaClockTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
}