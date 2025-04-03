package hipravin.jarvis.bookstore.load;

import hipravin.jarvis.bookstore.BookstoreLoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BookstoreUpdateWatcher implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(BookstoreUpdateWatcher.class);

    private final ExecutorService watchExecutorService = Executors.newSingleThreadExecutor();

    private final BookstoreProperties bookstoreProperties;
    private final BookstoreLoadService bookstoreLoadService;

    public BookstoreUpdateWatcher(BookstoreProperties bookstoreProperties, BookstoreLoadService bookstoreLoadService) {
        this.bookstoreProperties = bookstoreProperties;
        this.bookstoreLoadService = bookstoreLoadService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent applicationReadyEvent) {
        Runnable watchRunnable = DirectoryUtil.watchForUpdatesRunnable(
                bookstoreProperties.loaderRootPath(),
                this::handleBookstoreUpdate);

        watchExecutorService.submit(watchRunnable);
    }

    private void handleBookstoreUpdate(List<DirectoryUtil.ChangeEvent> changeEvents) {
        for (DirectoryUtil.ChangeEvent changeEvent : changeEvents) {
            bookstoreLoadService.handleUpdate(changeEvent);

        }
    }

    @Override
    public void destroy() throws Exception {
        watchExecutorService.shutdownNow();
    }
}
