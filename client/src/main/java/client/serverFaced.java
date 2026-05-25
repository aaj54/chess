package client;

import com.google.gson.Gson;
import model.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collection;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson Gson = new Gson();

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
        record loginRequest(String username, String password) {}
        var request = buildRequest("POST", "/session", new loginRequest(username,password), null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws Exception
    {
        var path = String.format("/pet/%s", authToken);
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
        return res.games();
        }

    public int createGame(String authToken, String gameName) throws Exception
    {
        record CreateGameRequest(String gameName) {}
        record CreateGameResponse(int gameID) {}
        var request = buildRequest("POST", "/game", new CreateGameRequest(gameName), authToken);
        var response = sendRequest(request);
        var res = handleResponse(response, CreateGameResponse.class);
        return res.gameID();
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, body != null
                        ? BodyPublishers.ofString(Gson.toJson(body))
                        : BodyPublishers.noBody());
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (authToken != null) {
            request.header("authorization", authToken);
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
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



