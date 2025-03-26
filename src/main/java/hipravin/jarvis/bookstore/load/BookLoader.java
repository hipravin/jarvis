package hipravin.jarvis.bookstore.load;

import hipravin.jarvis.bookstore.load.model.Book;

import java.nio.file.Path;

public interface BookLoader {
    Book load(Path file);
    Book load(byte[] documentBinaryContent, String title);
}
