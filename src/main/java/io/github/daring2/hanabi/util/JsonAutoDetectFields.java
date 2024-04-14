package io.github.daring2.hanabi.util;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonAutoDetectFields {
}
