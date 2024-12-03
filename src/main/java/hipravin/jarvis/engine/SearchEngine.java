package hipravin.jarvis.engine;

import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;

public interface SearchEngine {
    JarvisResponse search(JarvisRequest request);
}
