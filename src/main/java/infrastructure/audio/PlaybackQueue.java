package infrastructure.audio;

import entities.Track;

import java.util.*;

public class PlaybackQueue {
    /**
     * Manages playback order, history, and navigation for audio tracks.
     *
     * <p>This class maintains three main structures:</p>
     * <ul>
     *   <li><b>trackQueue</b> → upcoming tracks (forward navigation)</li>
     *   <li><b>history</b> → previously played tracks (backward navigation)</li>
     *   <li><b>originalOrder</b> → base ordering used for reset/shuffle</li>
     * </ul>
     *
     * <h2>Behavior</h2>
     * <ul>
     *   <li>next() advances forward and pushes current track into history</li>
     *   <li>prev() pulls from history and restores current track to queue</li>
     *   <li>reset() rebuilds the queue from original order (with optional shuffle)</li>
     * </ul>
     *
     * <h2>Notes</h2>
     * <ul>
     *   <li>Duplicate tracks are prevented using an internal Set</li>
     *   <li>Shuffle only affects future playback, not history</li>
     *   <li>Looping is controlled externally via AudioPlayer</li>
     * </ul>
     */

    private final Deque<Track> trackQueue = new ArrayDeque<>();
    private final Deque<Track> history = new ArrayDeque<>();
    private final List<Track> originalOrder = new ArrayList<>();
    private final Set<Track> trackSet = new HashSet<>();
    private Track currentTrack;
    private boolean shuffle = false;
    private boolean loopQueue = false;

    public void add(Track track) {
        if (track == null) return;
        if (trackSet.add(track)) {
            trackQueue.add(track);
            originalOrder.add(track);
        }
    }

    public void addAll(List<Track> tracks) {
        if (tracks == null || tracks.isEmpty()) return;
        for (Track t : tracks) {
            if (trackSet.add(t)) {
                trackQueue.add(t);
                originalOrder.add(t);
            }
        }
    }
    public void clear(){
        trackQueue.clear();
        history.clear();
        originalOrder.clear();
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

    public boolean isQueueEmpty() {
        return trackQueue.isEmpty();
    }

    public void remove(Track track) {
        if (track != null) {
            trackQueue.remove(track);
            originalOrder.remove(track);
            history.remove(track);
            trackSet.remove(track);
            if (track.equals(currentTrack)) {
                currentTrack = null;
            }
        }
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Only used internally by AudioPlayer during initialization
     */
    public void setCurrentTrack(Track track){
        this.currentTrack=track;
    }

    /**
     Enabling shuffle rebuilds queue and clears history
     */
    public void setShuffle(boolean enable) {
        if (shuffle == enable) return;
        shuffle = enable;
        reset();
    }

    public boolean isShuffleEnabled() {
        return shuffle;
    }

    public void clearHistory(){
        history.clear();
    }

    public void pushHistory(Track track) {
        history.push(track);
        System.out.println("push to history:" + track);
    }

    public void removeFromQueue(Track t){
        trackQueue.remove(t);
    }

    public void setLoopQueue(boolean enable) {
        loopQueue = enable;
    }

    public boolean isLoopQueueEnabled() {
        return loopQueue;
    }

    /**
     * Rebuilds the queue from the original track order.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Clears current queue and history</li>
     *   <li>Repopulates queue from originalOrder</li>
     *   <li>Applies shuffle if enabled</li>
     *   <li>Preserves currentTrack (excluded from queue)</li>
     * </ul>
     */
    public void reset() {
        Track oldCurrent = currentTrack;

        trackQueue.clear();
        history.clear();

        List<Track> temp = new ArrayList<>(originalOrder);
        if (shuffle) {
            Collections.shuffle(temp);
        }

        if (oldCurrent != null) {
            temp.remove(oldCurrent);
        }

        trackQueue.addAll(temp);

        if (oldCurrent != null) {
            currentTrack = oldCurrent;
        }
    }

    @Override
    public String toString() {
        return "PlaybackQueue{" +
                "\ncurrentTrack=" + currentTrack +
                ", \ntrackQueue=" + trackQueue +
                ", \nhistory=" + history +
                ", \noriginalOrder=" + originalOrder +
                ", \ntrackSet=" + trackSet +
                ", \nshuffle=" + shuffle +
                ", \nloopQueue=" + loopQueue +
                '}';
    }
}
