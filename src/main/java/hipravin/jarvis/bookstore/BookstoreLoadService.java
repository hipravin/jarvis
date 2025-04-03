package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.load.DirectoryUtil;

public interface BookstoreLoadService {
    void loadAll();

    void handleUpdate(DirectoryUtil.ChangeEvent directoryChangeEvent);
}
