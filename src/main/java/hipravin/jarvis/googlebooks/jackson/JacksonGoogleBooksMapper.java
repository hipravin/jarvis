package hipravin.jarvis.googlebooks.jackson;

import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;

public interface JacksonGoogleBooksMapper {
    BooksVolumes readBooksVolumes(String contentJson);
}
