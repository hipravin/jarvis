package hipravin.jarvis.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class TraceHandler implements HandlerInterceptor {

    @Name("HttpRequest")
    @Category({"Jarvis", "Request"})
    @StackTrace(false)
    static class HttpRequestEvent extends Event {
        @Label("Request Path")
        private String requestURI;

        @Label("QueryString")
        private String queryString;
    }

    private HttpRequestEvent event;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        event = new HttpRequestEvent();
        event.begin();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        event.requestURI = request.getRequestURI();
        event.queryString = request.getQueryString();
        event.end();
        event.commit();
    }
}