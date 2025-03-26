package hipravin.jarvis.bookstore.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class DirectoryUtil {
    private static final Logger log = LoggerFactory.getLogger(DirectoryUtil.class);

    private DirectoryUtil() {
    }

    public static List<Path> findFilesRecursively(Path root, String extension) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(extension);

        PathMatcher extensionMatcher = FileSystems.getDefault().getPathMatcher("glob:**.%s".formatted(extension));
        try (Stream<Path> walkRoot = Files.walk(root)) {
            return walkRoot
                    .filter(Files::isRegularFile)
                    .filter(extensionMatcher::matches)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * When any sub-path (either file or directory) under <code>dir</code> is modified then
     * <code>onUpdateConsumer</code> is called passing set of modified entries without recursive traversal.
     * This means that if file 'f1.txt' is modified at location 'dir/subpath1/f1.txt' then set will contain only 'subpath1'
     * but not 'subpath1/f1.txt'.
     *
     * @return Returns a runnable that needs to be submitted for execution.
     */
    public static Runnable watchForUpdatesRunnable(Path dir, Consumer<Set<Path>> onChangeConsumer) {
        final WatchService watchService = startWatchService(dir);

        return () -> watch(watchService, dir, onChangeConsumer);
    }

    private static void watch(WatchService watchService, Path dir, Consumer<Set<Path>> onChangeConsumer) {
        boolean valid = true;
        while (valid && !Thread.currentThread().isInterrupted()) {
            try {
                log.debug("Start watching updates on dir: {}", dir);

                WatchKey key = watchService.take();

                Set<Path> relativePaths = extractModifiedRelativePaths(key);
                log.debug("Updated paths (relative): {}", relativePaths);

                var absolutePaths = relativePaths.stream()
                        .map(dir::resolve)
                        .collect(Collectors.toSet());

                onChangeConsumer.accept(absolutePaths);

                valid = key.reset();
                if (!valid) {
                    log.warn("Stop watching dir '{}' due to key.reset() returned false", dir);
                }
            } catch (InterruptedException e) {
                log.warn("FileWatch has been terminated due to interrupt at dir '{}'", dir);
                Thread.currentThread().interrupt();
            }
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static WatchService startWatchService(Path dir) {
        try {
            WatchService watchService
                    = FileSystems.getDefault().newWatchService();

            dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            return watchService;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new UncheckedIOException(e);
        }
    }

    private static Set<Path> extractModifiedRelativePaths(WatchKey key) {
        return key.pollEvents()
                .stream().filter(e -> e.kind() != StandardWatchEventKinds.OVERFLOW)
                .map(e -> ((WatchEvent<Path>) e).context())
                .collect(Collectors.toSet());
    }

}
