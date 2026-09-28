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

    public static Character.UnicodeScript detectMainScript(String text) {
        int arabic = 0;
        int latin = 0;

        for (int cp : text.codePoints().toArray()) {
            Character.UnicodeScript script = Character.UnicodeScript.of(cp);

            switch (script) {
                case ARABIC -> arabic++;
                case LATIN -> latin++;
            }
        }

        if (arabic > latin) {
            return Character.UnicodeScript.ARABIC;
        }

        if (latin > arabic) {
            return Character.UnicodeScript.LATIN;
        }

        return Character.UnicodeScript.UNKNOWN;
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