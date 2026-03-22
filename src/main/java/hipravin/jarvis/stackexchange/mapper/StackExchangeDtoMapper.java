package hipravin.jarvis.stackexchange.mapper;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.dto.*;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import org.springframework.stereotype.Component;

@Component
public class StackExchangeDtoMapper {

    public Excerpt mapToDto(SearchExcerpt se) {
        return new Excerpt(InformationSource.STACKEXCHANGE,
                new HeaderBlock(Icons.STACKEXCHANGE_ICON, stackOverflowLink(se)),
                new TextBlock(TextFormat.HTML, collapseNewLInes(se.excerpt())));
    }

    String collapseNewLInes(String text) {
        return text.replaceAll("\\R{2,}", "\n\n");
    }

    Link stackOverflowLink(SearchExcerpt se) {
        String href = (se.questionId() != null)
                ? "https://stackoverflow.com/questions/%d/".formatted(se.questionId())
                : null;

        return new Link(se.title(), href);
    }
}
