package hipravin.jarvis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.retry.MethodRetryEvent;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableResilientMethods
@EnableAspectJAutoProxy
public class JarvisApplication {
    private static final Logger log = LoggerFactory.getLogger(JarvisApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(JarvisApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("Spring-async-");
        executor.initialize();
        return executor;
    }

    final AtomicLong retryCounter = new AtomicLong(0);
    @EventListener(MethodRetryEvent.class)
    public void onRetry(MethodRetryEvent methodRetryEvent) {
        if(retryCounter.incrementAndGet() % 10 == 0) {
            log.warn("Retry #{}: {}, {}", retryCounter.get(),
                    methodRetryEvent.getMethod().getName(), methodRetryEvent.getFailure().getMessage());
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {//TODO: figure out more reasonable CORS config
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
