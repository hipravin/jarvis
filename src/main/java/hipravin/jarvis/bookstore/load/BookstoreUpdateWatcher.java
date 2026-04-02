package hipravin.jarvis.bookstore.load;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class BookstoreUpdateWatcher implements DisposableBean {
    private final ExecutorService watchExecutorService = Executors.newSingleThreadExecutor();

    private final BookstoreProperties bookstoreProperties;
    private final BookstoreLoadService bookstoreLoadService;
    private Future<?> watchRunnableFuture;

    public BookstoreUpdateWatcher(BookstoreProperties bookstoreProperties, BookstoreLoadService bookstoreLoadService) {
        this.bookstoreProperties = bookstoreProperties;
        this.bookstoreLoadService = bookstoreLoadService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        Runnable watchRunnable = DirectoryUtil.watchForUpdatesRunnable(
                bookstoreProperties.loaderRootPath(),
                this::handleBookstoreUpdate);

        watchRunnableFuture = watchExecutorService.submit(watchRunnable);
    }

    private void handleBookstoreUpdate(List<DirectoryUtil.ChangeEvent> changeEvents) {
        for (DirectoryUtil.ChangeEvent changeEvent : changeEvents) {
            bookstoreLoadService.handleUpdate(changeEvent);
        }
    }

    @Override
    public void destroy() {
        if(watchRunnableFuture != null) {
            watchRunnableFuture.cancel(true);
        }
        watchExecutorService.close();
    }
}
