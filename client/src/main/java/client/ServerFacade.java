package client;

import com.google.gson.Gson;
import model.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collection;
import java.util.Map;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception
    {
        record RegRequest(String username, String password, String email) {}
        var request = buildRequest("POST", "/user", new RegRequest(username,password, email), null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception
    {
        record LoginRequest(String username, String password) {}
        var request = buildRequest("POST", "/session", new LoginRequest(username,password), null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws Exception
    {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void clear() throws Exception {
        var request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    public Collection<GameData> listGames(String authToken) throws Exception
    {
        record ListGamesResponse(Collection<GameData> games) {}
        var request = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(request);
        var res = handleResponse(response, ListGamesResponse.class);
        assert res != null;
        return res.games();
        }

    public int createGame(String authToken, String gameName) throws Exception
    {
        record CreateGameRequest(String gameName) {}
        record CreateGameResponse(int gameID) {}
        var request = buildRequest("POST", "/game", new CreateGameRequest(gameName), authToken);
        var response = sendRequest(request);
        var res = handleResponse(response, CreateGameResponse.class);
        assert res != null;
        return res.gameID();
    }

    public void joinGame(String authToken, String playerColor,  int gameID) throws Exception
    {
        record JoinGameRequest(String playerColor, int gameID) {}
        var request = buildRequest("PUT", "/game", new JoinGameRequest(playerColor, gameID), authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, body != null
                        ? BodyPublishers.ofString(gson.toJson(body))
                        : BodyPublishers.noBody());
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (authToken != null) {
            request.header("authorization", authToken);
        }
        return request.build();
    }


    private HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        return client.send(request, BodyHandlers.ofString());
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws Exception {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var error = gson.fromJson(response.body(), Map.class);
            String message = error != null ? (String) error.get("message") : "Error: " + status;
            throw new Exception(message);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}



