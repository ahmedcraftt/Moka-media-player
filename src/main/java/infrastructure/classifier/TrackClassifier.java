package infrastructure.classifier;

import domain.model.media.MediaType;
import domain.model.metadata.Metadata;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class TrackClassifier {

    private static final Set<String> SONG_KEYWORDS = new HashSet<>(Set.of("feat", "ft", "remix", "official", "song", "acoustic", "cover", "music", "album", "lyrics"));
    private static final Set<String> PODCAST_KEYWORDS = new HashSet<>(Set.of("episode", "ep", "podcast", "show", "season", "speech"));
    private static final Set<String> ABOOK_KEYWORDS = new HashSet<>(Set.of("chapter", "part", "volume", "book", "unabridged", "narrated by", "authored by", "read", "story", "drama", "act"));

    private static final Set<String> PODCAST_GENRES = Set.of("podcast", "speech", "talk", "interview", "radio", "Entertainment");
    private static final Set<String> AUDIOBOOK_GENRES = Set.of("audiobook", "book", "spoken", "story", "drama");
    private static final Set<String> SONG_GENRES = Set.of("pop", "rock", "hiphop", "rap", "metal", "edm", "jazz", "classical", "music");

    private static final Pattern SONG_PATTERN = Pattern.compile(".*(feat\\.?|ft\\.?|remix|official\\s+audio|lyrics?).*");
    private static final Pattern PODCAST_PATTERN = Pattern.compile(".*(s\\d+e\\d+|episode\\s*\\d+|ep\\s*\\d+).*");
    private static final Pattern AUDIOBOOK_PATTERN = Pattern.compile(".*(chapter\\s*\\d+|part\\s*\\d+|volume\\s*\\d+|book\\s*\\d+).*");

    public MediaType classify(Path path, Metadata metadata) {

        int songScore = 0;
        int podcastScore = 0;
        int abookScore = 0;

        int duration = metadata.getDurationInSeconds();

        String filename = path.getFileName().toString().toLowerCase();
        String title = safe(metadata.getTitle()).toLowerCase();
        String genre = safe(metadata.getGenre()).toLowerCase();

        // duration rules
        int songBoost = 0;
        int abookBoost = 0;
        int podcastBoost = 0;

        if (duration <= 300) {
            songBoost += 6;
            abookBoost -= 1;
        } else if (duration <= 600) {
            songBoost += 3;
            abookBoost += 3;
        } else {
            songBoost -= 2;
        }

        if (duration >= 600 && duration <= 1800) {
            abookBoost += 6;
            podcastScore -= 1;
        } else if (duration <= 3600) {
            abookBoost += 4;
            podcastBoost += 1;
        } else {
            abookBoost += 3;
        }

        if (duration >= 1800 && duration <= 7200) {
            podcastBoost += 5;
        } else if (duration > 7200) {
            podcastBoost += 6;
        }

        songScore += songBoost;
        abookScore += abookBoost;
        podcastScore += podcastBoost;

        // keyword rules
        songScore += evalTokens(filename, SONG_KEYWORDS);
        podcastScore += evalTokens(filename, PODCAST_KEYWORDS);
        abookScore += evalTokens(filename, ABOOK_KEYWORDS);

        songScore += evalTokens(title, SONG_KEYWORDS);
        podcastScore += evalTokens(title, PODCAST_KEYWORDS);
        abookScore += evalTokens(title, ABOOK_KEYWORDS);

        songScore += evalTokens(genre, SONG_GENRES);
        podcastScore += evalTokens(genre, PODCAST_GENRES);
        abookScore += evalTokens(genre, AUDIOBOOK_GENRES);

        // final decision
        int max = Math.max(songScore, Math.max(podcastScore, abookScore));

        if (max < 3) return MediaType.SONG;
        if (songScore >= max) return MediaType.SONG;
        if (podcastScore >= max) return MediaType.PODCAST;
        return MediaType.AUDIOBOOK;

    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static int evalTokens(String string, Set<String> keywords) {
        int score = 0;
        Set<String> tokens = getTokens(string);

        for (String t : tokens) {
            if (keywords.stream().anyMatch(t::contains)) {
                score += 4;
            }
        }
        return score;
    }

    private static String normalize(String string) {
        return string.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
    }

    private static Set<String> getTokens(String string) {
        return new HashSet<>(Arrays.asList(normalize(string).split("\\s+")));
    }

    private static int safeParseInt(String value) {
        try {
            return (value == null || value.isBlank()) ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
