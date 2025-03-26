package hipravin.jarvis.bookstore.load;

import hipravin.jarvis.bookstore.load.model.Book;

import java.nio.file.Path;

public interface BookReader {
    Book read(Path file);
    Book read(byte[] documentBinaryContent, String title);

}
