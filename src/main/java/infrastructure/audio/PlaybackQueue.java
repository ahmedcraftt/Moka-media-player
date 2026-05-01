package infrastructure.audio;

import entities.Track;

import java.util.*;

public class PlaybackQueue {
    private final Deque<Track> trackQueue = new ArrayDeque<>();
    private final Deque<Track> history = new ArrayDeque<>();
    private final List<Track> originalOrder = new ArrayList<>();
    private final Set<Track> trackSet = new HashSet<>(originalOrder);
    private Track currentTrack;
    private boolean shuffle = false;
    private boolean loopQueue = false;

    public void printStatus(){
        System.out.println("main queue:"+trackQueue);
        System.out.println("history queue:"+history);
        System.out.println("order list:"+originalOrder);
        System.out.println("track set:"+trackSet);
        System.out.println("shuffle:"+shuffle);
        System.out.println("loop:"+loopQueue);
        String current =(currentTrack!=null)?currentTrack.getMetadata().getTitle():"none";
        System.out.println("Current track:" + current);
    }

    public void add(Track track) {
        if (track == null) return;
        trackQueue.add(track);
        originalOrder.add(track);
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

    public boolean isEmpty(){
        return trackQueue.isEmpty();
    }

    public void shuffle() {
        List<Track> tempList = new ArrayList<>(trackQueue);
        Collections.shuffle(tempList);
        trackQueue.clear();
        trackQueue.addAll(tempList);
    }

    public void remove(Track track) {
        trackQueue.remove(track);
        originalOrder.remove(track);
        history.remove(track);
        trackSet.remove(track);

        if (track.equals(currentTrack)) {
            currentTrack = null;
        }

    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track track){
        this.currentTrack=track;
    }

    public void setShuffle(boolean enable) {
        if (shuffle == enable) return;

        shuffle = enable;
        reset();
    }

    public boolean isShuffleEnabled() {
        return shuffle;
    }

    public Deque<Track> getHistory() {
        return history;
    }

    public void clearHistory(){
        history.clear();
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

    public Track peak(){
       return trackQueue.peekFirst();
    }

}
