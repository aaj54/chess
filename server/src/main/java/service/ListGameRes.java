package service;

import model.GameData;

import java.util.Collection;

public record ListGameRes(
    Collection<GameData> allGames
)
{}
