package hipravin.jarvis.openapi;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Tag(name = "search")
public @interface TagSearch {
}
