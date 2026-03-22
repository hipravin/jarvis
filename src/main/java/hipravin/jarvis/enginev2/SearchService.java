package hipravin.jarvis.enginev2;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;

public interface SearchService {
    SearchResponse search(SearchRequest request);

    InformationSource getSource();

    class Order {
        public static final int ORDER_1 = 1;
        public static final int ORDER_2 = 2;
        public static final int ORDER_3 = 3;
        public static final int ORDER_4 = 4;
    }
}
