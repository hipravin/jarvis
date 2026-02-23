package hipravin.jarvis.monitoring;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TraceConfig implements WebMvcConfigurer {

    private final TraceHandler traceHandler;

    public TraceConfig(TraceHandler traceHandler) {
        this.traceHandler = traceHandler;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceHandler);
    }
}