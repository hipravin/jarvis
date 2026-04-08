package hipravin.jarvis.statistic;

import hipravin.jarvis.event.SearchCompletedEvent;
import hipravin.jarvis.statistic.dto.SearchCompletedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class StatisticConsumer {
    private static final Logger log = LoggerFactory.getLogger(StatisticConsumer.class);

    private final StatisticProcessorServiceImpl statisticProcessorService;

    public StatisticConsumer(StatisticProcessorServiceImpl statisticProcessorService) {
        this.statisticProcessorService = statisticProcessorService;
    }

    @RetryableTopic(attempts = "3", numPartitions = "20",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backOff = @BackOff(delay = 500, maxDelay = 10000, multiplier = 1.5))
    @KafkaListener(topics = {"search-stat-topic"}, groupId = "group1")
    public void searchStatListener(@Payload SearchCompletedDto searchCompletedDto,
                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                    @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received q [{}] from group1, partition-{} with offset-{}, topic {}",
                searchCompletedDto.request().query(), partition, offset, topic);

        statisticProcessorService.process(searchCompletedDto);

    }

    @DltHandler
    public void handleDlt(SearchCompletedEvent searchCompletedEvent) {
        log.error("Message dead lettered: '{}'", searchCompletedEvent);
    }
}
