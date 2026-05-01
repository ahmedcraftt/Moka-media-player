package ui.controllers;

import entities.Track;

import infrastructure.audio.AudioPlayer;

public class ControllerServer {

    private Track selectedTrack;
    private AudioPlayer player;

    public Track getSelectedTrack() {
        System.out.println("get track");
        return selectedTrack;
    }

    public void setSelectedTrack(Track track) {
        System.out.println("set track:"+track.getMetadata().getTitle());
        this.selectedTrack =track;
    }
}