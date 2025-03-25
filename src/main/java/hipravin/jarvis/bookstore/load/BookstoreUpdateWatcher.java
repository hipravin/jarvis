package hipravin.jarvis.bookstore.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BookstoreUpdateWatcher implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(BookstoreUpdateWatcher.class);

    final ExecutorService watchExecutorService = Executors.newSingleThreadExecutor();

    final BookstoreProperties bookstoreProperties;

    public BookstoreUpdateWatcher(BookstoreProperties bookstoreProperties) {
        this.bookstoreProperties = bookstoreProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent applicationReadyEvent) {
        Runnable watchRunnable = DirectoryUtil.watchForUpdatesRunnable(
                        bookstoreProperties.loaderRootPath(),
                        this::handleBookstoreUpdate);

        watchExecutorService.submit(watchRunnable);
    }

    void handleBookstoreUpdate(Set<Path> paths) {
        try {
            log.info("Bookstore paths updated: {}", paths);
//            articleInMemoryRepository.fillFromStorage(articleStorage);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() throws Exception {
        watchExecutorService.shutdownNow();
    }
}
