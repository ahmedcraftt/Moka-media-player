package config;

public final class AppConfig {

    private int configVersion = 1;
    private PlayerConfig playerConfig = new PlayerConfig();
    private UIConfig uiConfig = new UIConfig();
    private SearchConfig searchConfig = new SearchConfig();

    public PlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    public UIConfig getUIConfig() {
        return uiConfig;
    }

    public SearchConfig getSearchConfig() {
        return searchConfig;
    }

}
