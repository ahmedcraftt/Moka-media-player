package domain.model.metadata;

public class VideoMetadata {
    private int width;
    private int height;
    private String resolution;
    private int frameRate;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width < 0) width = 0;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height < 0) height = 0;
        this.height = height;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        if (resolution == null) resolution = "";
        this.resolution = resolution;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        if (frameRate < 0) frameRate = 0;
        this.frameRate = frameRate;
    }

}
