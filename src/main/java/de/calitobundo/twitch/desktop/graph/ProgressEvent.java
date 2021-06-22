package de.calitobundo.twitch.desktop.graph;

@FunctionalInterface
public interface ProgressEvent {
    
    void onProgress(double size, double total);
}
