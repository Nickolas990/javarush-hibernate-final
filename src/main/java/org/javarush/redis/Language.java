package org.javarush.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Language {
    private String language;
    private Boolean isOfficial;
    private BigDecimal percentage;

}
