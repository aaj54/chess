package dataaccess;

import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO {

    private final HashMap<Integer, GameData> games = new HashMap<>();

    public void clear() {
        games.clear(); //clear game
    }
}