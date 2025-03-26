package hipravin.jarvis.bookstore.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DirectoryUtil {
    private static final Logger log = LoggerFactory.getLogger(DirectoryUtil.class);

    private DirectoryUtil() {
    }

    public record ChangeEvent(Path path, Kind kind) {
        public static Optional<ChangeEvent> from(Path dir, WatchEvent<Path> event) {
            return Kind.from(event.kind())
                    .map(kind -> new ChangeEvent(dir.resolve(event.context()), kind));
        }

        public enum Kind {
            MODIFY, CREATE, DELETE;

            public static Optional<Kind> from(WatchEvent.Kind<Path> kind) {
                if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                    return Optional.of(Kind.CREATE);
                } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
                    return Optional.of(Kind.MODIFY);
                } else if (StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                    return Optional.of(Kind.DELETE);
                } else {
                    log.debug("Unresolvable WatchEvent.Kind: {} / {}", kind.name(), kind.type());
                    return Optional.empty();
                }
            }
        }
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
    public static Runnable watchForUpdatesRunnable(Path dir, Consumer<List<ChangeEvent>> onChangeConsumer) {
        final WatchService watchService = startWatchService(dir);

        return () -> watch(watchService, dir, onChangeConsumer);
    }

    private static void watch(WatchService watchService, Path dir, Consumer<List<ChangeEvent>> onChangeConsumer) {
        boolean valid = true;
        while (valid && !Thread.currentThread().isInterrupted()) {
            try {
                log.debug("Start watching updates on dir: {}", dir);

                WatchKey key = watchService.take();

                List<ChangeEvent> changeEvents = toChangeEvents(dir, key);
                log.debug("Directory change events {}: {}", dir, changeEvents);
                onChangeConsumer.accept(changeEvents);

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

    private static List<ChangeEvent> toChangeEvents(Path dir, WatchKey key) {
        return key.pollEvents().stream()
                .filter(e -> e.kind() != StandardWatchEventKinds.OVERFLOW)
                .map(e -> ChangeEvent.from(dir, (WatchEvent<Path>) e))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
