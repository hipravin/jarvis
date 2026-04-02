package hipravin.jarvis.bookstore.load;

public interface BookstoreLoadService {
    void loadAll();

    void handleUpdate(DirectoryUtil.ChangeEvent directoryChangeEvent);
}
