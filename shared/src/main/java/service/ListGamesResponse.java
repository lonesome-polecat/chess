package service;

import model.GameData;

import java.util.List;

public record ListGamesResponse(List<GameData> games) {
}
