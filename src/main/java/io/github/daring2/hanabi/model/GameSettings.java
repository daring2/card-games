package io.github.daring2.hanabi.model;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hanabi.game-settings")
public class GameSettings {

    int minPlayers = 2;
    int maxPlayers = 5;
    int maxBlueTokens = 8;
    int maxRedTokens = 3;
    int maxFireworks = 5;

    public int getMinPlayers() {
        return minPlayers;
    }

    void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxBlueTokens() {
        return maxBlueTokens;
    }

    void setMaxBlueTokens(int maxBlueTokens) {
        this.maxBlueTokens = maxBlueTokens;
    }

    public int getMaxRedTokens() {
        return maxRedTokens;
    }

    void setMaxRedTokens(int maxRedTokens) {
        this.maxRedTokens = maxRedTokens;
    }

    public int getMaxFireworks() {
        return maxFireworks;
    }

    void setMaxFireworks(int maxFireworks) {
        this.maxFireworks = maxFireworks;
    }

}
