package domain.model.metadata;

import java.util.HashSet;
import java.util.Set;

public class Language {
    private static final Set<String> rtlLanguages = new HashSet<>(Set.of(
            "ara", // Arabic
            "fas", "per", // Persian / Farsi
            "urd", // Urdu
            "pus", // Pashto
            "kur", // Kurdish
            "snd", // Sindhi
            "uig", // Uyghur
            "yid"  // Yiddish
    ));

    private final String language;
    private final boolean isRTL;

    public Language(String language) {
        if (language == null || language.isBlank()) {
            language = "eng";
        }

        this.language = normalizeLanguage(language);

        this.isRTL = rtlLanguages.contains(this.language);
    }

    public boolean isRTL() {
        return isRTL;
    }

    public String getLanguageString() {
        return language;
    }

    public String toString() {
        return language;
    }

    private String normalizeLanguage(String lang) {
        lang = lang.trim().toLowerCase();

        return switch (lang) {
            case "english", "eng", "en" -> "eng";

            case "arabic", "ara", "ar" -> "ara";

            case "japanese", "jpn", "ja" -> "jpn";

            case "french", "fra", "fre", "fr" -> "fra";

            case "german", "deu", "ger", "de" -> "deu";

            case "spanish", "spa", "es" -> "spa";

            case "persian", "farsi", "fas", "per", "fa" -> "fas";

            default -> lang.length() >= 3 ? lang.substring(0, 3) : lang;
        };
    }
}