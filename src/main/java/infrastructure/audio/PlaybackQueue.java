package infrastructure.audio;

import domain.model.media.Track;
import java.util.*;

public class PlaybackQueue {
    private final Deque<Track> trackQueue = new ArrayDeque<>();
    private final Deque<Track> history = new ArrayDeque<>();
    private final List<Track> originalOrder = new ArrayList<>();
    private final List<Track> shuffledOrder = new ArrayList<>();
    private final Set<Track> trackSet = new HashSet<>();

    private Track currentTrack;
    private boolean shuffle = false;
    private boolean loopQueue = false;

    public void add(Track track) {
        if (track == null) return;
        if (trackSet.add(track)) {
            originalOrder.add(track);
            if (shuffle) {
                shuffledOrder.add(track);
            }
            trackQueue.add(track);
        }
    }

    public void addAll(List<Track> tracks) {
        if (tracks == null) return;
        for (Track t : tracks) {
            add(t);
        }
    }

    public void clear() {
        trackQueue.clear();
        history.clear();
        originalOrder.clear();
        shuffledOrder.clear();
        trackSet.clear();
        currentTrack = null;
    }

    public Track next() {
        if (trackQueue.isEmpty()) {
            if (loopQueue) {
                reset();
            } else {
                return null;
            }
        }

        if (!trackQueue.isEmpty()) {
            if (currentTrack != null) {
                history.push(currentTrack);
            }
            currentTrack = trackQueue.poll();
        }
        return currentTrack;
    }

    public Track prev() {
        if (!history.isEmpty()) {
            if (currentTrack != null) {
                trackQueue.addFirst(currentTrack);
            }
            currentTrack = history.pop();
            return currentTrack;
        }
        return null;
    }

    public Track peekNext() {
        if (!trackQueue.isEmpty()) {
            return trackQueue.peek();
        }
        if (loopQueue && !originalOrder.isEmpty()) {
            List<Track> activeOrder = shuffle ? shuffledOrder : originalOrder;
            return activeOrder.isEmpty() ? null : activeOrder.get(0);
        }
        return null;
    }

    public Track peekPrev() {
        return history.peek();
    }

    public void remove(Track track) {
        if (track == null) return;
        trackQueue.remove(track);
        originalOrder.remove(track);
        shuffledOrder.remove(track);
        history.remove(track);
        trackSet.remove(track);
        if (track.equals(currentTrack)) {
            currentTrack = null;
        }
    }

    public void reset() {
        trackQueue.clear();
        history.clear();

        if (shuffle) {
            shuffledOrder.clear();
            shuffledOrder.addAll(originalOrder);
            Collections.shuffle(shuffledOrder);
            trackQueue.addAll(shuffledOrder);
        } else {
            trackQueue.addAll(originalOrder);
        }

        if (currentTrack != null) {
            trackQueue.remove(currentTrack);
        }
    }

    public void setupNavigationContext(Track selected, List<Track> fullList) {
        clear();
        addAll(fullList);
        this.currentTrack = selected;

        trackQueue.clear();
        history.clear();

        List<Track> activeOrder = shuffle ? shuffledOrder : originalOrder;
        int index = activeOrder.indexOf(selected);
        if (index == -1) return;

        for (int i = index + 1; i < activeOrder.size(); i++) {
            trackQueue.add(activeOrder.get(i));
        }

        for (int i = index - 1; i >= 0; i--) {
            history.add(activeOrder.get(i));
        }
    }

    public void setShuffle(boolean enable) {
        if (this.shuffle == enable) return;
        this.shuffle = enable;

        if (shuffle) {
            shuffledOrder.clear();
            shuffledOrder.addAll(originalOrder);
            Collections.shuffle(shuffledOrder);
            shuffledOrder.remove(currentTrack);
            shuffledOrder.addFirst(currentTrack);
        }

        trackQueue.clear();
        List<Track> activeOrder = shuffle ? shuffledOrder : originalOrder;
        for (Track t : activeOrder) {
            if (!history.contains(t) && !t.equals(currentTrack)) {
                trackQueue.add(t);
            }
        }
    }

    public List<Track> getQueuedTracks() {
        return shuffle ? new ArrayList<>(shuffledOrder) : new ArrayList<>(originalOrder);
    }

    public int getNumberOfTracks() {
        return originalOrder.size();
    }

    public boolean isShuffleEnabled() {
        return shuffle;
    }

    public void setLoopQueue(boolean enable) {
        this.loopQueue = enable;
    }

    public boolean isLoopQueueEnabled() {
        return loopQueue;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track track) {
        this.currentTrack = track;
    }

    public Deque<Track> getHistory() {
        return history;
    }

    public boolean isQueueEmpty() {
        return trackQueue.isEmpty();
    }

    @Override
    public String toString() {
        return "PlaybackQueue{" +
                "currentTrack=" + currentTrack +
                ", trackQueue=" + trackQueue +
                ", history=" + history +
                ", originalOrder=" + originalOrder +
                ", shuffle=" + shuffle +
                ", loopQueue=" + loopQueue +
                '}';
    }
}