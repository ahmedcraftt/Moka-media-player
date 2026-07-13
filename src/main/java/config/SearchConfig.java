package config;

import gui.controllers.SearchEngine;

public final class SearchConfig {
    private static final SearchEngine DEFAULT_SEARCH_ENGINE = SearchEngine.GOOGLE;

    private SearchEngine preferredSearchEngine = DEFAULT_SEARCH_ENGINE;

    SearchEngine getPreferredSearchEngine() {
        return preferredSearchEngine;
    }

    void setPreferredSearchEngine(SearchEngine preferredSearchEngine) {
        this.preferredSearchEngine = preferredSearchEngine;
    }
}
