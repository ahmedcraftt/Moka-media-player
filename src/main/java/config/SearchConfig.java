package config;

import gui.models.SearchEngine;

public final class SearchConfig {
    private static final SearchEngine DEFAULT_SEARCH_ENGINE = SearchEngine.GOOGLE;

    private SearchEngine preferredSearchEngine = DEFAULT_SEARCH_ENGINE;

    public SearchEngine getPreferredSearchEngine() {
        return preferredSearchEngine;
    }

    public void setPreferredSearchEngine(SearchEngine preferredSearchEngine) {
        this.preferredSearchEngine = preferredSearchEngine;
    }
}
