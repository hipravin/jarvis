package hipravin.jarvis.googlebooks;

import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;

public interface GoogleBooksApiClient {
    BooksVolumes search(String searchString);
}
