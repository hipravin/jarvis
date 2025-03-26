package hipravin.jarvis.bookstore;

import java.nio.file.Path;

public interface BookstoreLoadService {
    void loadAll();

    void handleUpdate(Path updated);
}
