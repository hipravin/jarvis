package hipravin.jarvis.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TraceHandler implements HandlerInterceptor {

    static final String REQUEST_ATTR_KEY = "jarvis.monitoring.HttpRequestEvent";
    @Name(HttpRequestEvent.NAME)
    @Category({"Jarvis", "Request"})
    @StackTrace(false)
    static class HttpRequestEvent extends Event {
        static final String NAME = "HttpRequest";

        @Label("Request Path")
        private String requestURI;

        @Label("Query String")
        private String queryString;

        @Label("HTTP Status")
        private int status;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var event = new HttpRequestEvent();
        if (event.isEnabled()) {
            event.begin();
            request.setAttribute(REQUEST_ATTR_KEY, event);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {

        if (request.getAttribute(REQUEST_ATTR_KEY) instanceof HttpRequestEvent event
            && event.isEnabled() && event.shouldCommit()) {
            event.requestURI = request.getRequestURI();
            event.queryString = request.getQueryString();
            event.status = response.getStatus();

            event.commit();
        }
    }
}