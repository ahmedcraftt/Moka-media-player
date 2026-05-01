package infrastructure.media;

import entities.MediaType;
import entities.Track;
import entities.TrackMetadata;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class TrackFactory {

    private static final Set<String> SONG_KEYWORDS = new HashSet<>(Set.of("feat","ft","remix","official","song","acoustic","cover","music","album","lyrics"));
    private static final Set<String> PODCAST_KEYWORDS = new HashSet<>(Set.of("episode","ep","podcast","show","season","speech"));
    private static final Set<String> ABOOK_KEYWORDS = new HashSet<>(Set.of("chapter","part","volume","book","unabridged","narrated by","authored by","read","story","drama","act"));

    private static final Set<String> PODCAST_GENRES = Set.of("podcast", "speech", "talk", "interview", "radio","Entertainment");
    private static final Set<String> AUDIOBOOK_GENRES = Set.of("audiobook", "book", "spoken", "story","drama");
    private static final Set<String> SONG_GENRES = Set.of("pop", "rock", "hiphop", "rap", "metal", "edm", "jazz", "classical","music");

    private static final Pattern SONG_PATTERN = Pattern.compile(".*(feat\\.?|ft\\.?|remix|official\\s+audio|lyrics?).*");
    private static final Pattern PODCAST_PATTERN = Pattern.compile(".*(s\\d+e\\d+|episode\\s*\\d+|ep\\s*\\d+).*");
    private static final Pattern AUDIOBOOK_PATTERN = Pattern.compile(".*(chapter\\s*\\d+|part\\s*\\d+|volume\\s*\\d+|book\\s*\\d+).*");



    public static Track createTrack(Path path,MetaDataManager metaData){
        int songScore = 0;
        int abookScore = 0;
        int podcastScore = 0;

        int durationSeconds = metaData.getDuration(path);
        String filename = String.valueOf(path.getFileName());
        String genre = normalize(metaData.getGenre(path)).trim();
        String title = normalize(metaData.getTitle(path)).trim();
        String trackNumber = metaData.getTrackNumber(path);

        TrackMetadata trackMetadata = new TrackMetadata();
        trackMetadata.setDurationInSeconds(durationSeconds);
        trackMetadata.setGenre(genre);
        trackMetadata.setTitle(title);
        trackMetadata.setChapterCount(safeParseInt(trackNumber));

        if (!trackNumber.equalsIgnoreCase("unknown") || !trackNumber.isEmpty()){
            abookScore+=2;
            songScore-=2;
        }

        double songBoost = 0;
        double abookBoost = 0;
        double podcastBoost = 0;

        if (durationSeconds <= 300) {
            songBoost += 6;
            abookBoost-=1;
        } else if (durationSeconds <= 600) {
            songBoost += 3;
            abookBoost += 3;
        } else {
            songBoost -= 2;
        }

        if (durationSeconds >= 600 && durationSeconds <= 1800) {
            abookBoost += 6;
            podcastScore -=1;
        } else if (durationSeconds <= 3600) {
            abookBoost += 4;
            podcastBoost += 1;
        } else {
            abookBoost += 3;
        }

        if (durationSeconds >= 1800 && durationSeconds <= 7200) {
            podcastBoost += 5;
        } else if (durationSeconds > 7200) {
            podcastBoost += 6;
        }

        songScore += (int) songBoost;
        abookScore += (int) abookBoost;
        podcastScore += (int) podcastBoost;

        songScore += evalTokens(filename,SONG_KEYWORDS);
        podcastScore += evalTokens(filename,PODCAST_KEYWORDS);
        abookScore += evalTokens(filename,ABOOK_KEYWORDS);

        songScore += evalTokens(title,SONG_KEYWORDS);
        podcastScore += evalTokens(title,PODCAST_KEYWORDS);
        abookScore += evalTokens(title,ABOOK_KEYWORDS);

        songScore += evalTokens(genre,SONG_GENRES);
        podcastScore += evalTokens(genre,PODCAST_KEYWORDS);
        abookScore += evalTokens(genre,AUDIOBOOK_GENRES);

        if (SONG_PATTERN.matcher(filename).matches()) songScore += 3;
        if (PODCAST_PATTERN.matcher(filename).matches()) podcastScore += 3;
        if (AUDIOBOOK_PATTERN.matcher(filename).matches()) abookScore += 3;

        int max = Math.max(songScore, Math.max(podcastScore, abookScore));

        if (max<3) return new Track(filename,trackMetadata,path, MediaType.SONG);
        if (songScore>=max) return new Track(filename,trackMetadata,path,MediaType.SONG);
        if (podcastScore>=max) return new Track(filename,trackMetadata,path,MediaType.PODCAST);
        return new Track(filename,trackMetadata,path,MediaType.AUDIOBOOK);

    }

    private static int evalTokens(String string, Set<String> keywords){
        int score = 0;
        Set<String> tokens = getTokens(string);

        for (String t : tokens){
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
