package io.github.daring2.hanabi.model;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("hanabi.game-settings")
public class GameSettings {

    int minPlayers = 2;
    int maxPlayers = 5;

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

}
