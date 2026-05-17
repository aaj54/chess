package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JsonMapper;
import service.*;
import java.lang.reflect.Type;

public class Server {

    private final Javalin javalin;

    MemoryUserDAO userDAO = new MemoryUserDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    MemoryGameDAO gameDAO = new MemoryGameDAO();

    ClearSer clearService = new ClearSer(userDAO, authDAO, gameDAO);
    UserSer userService = new UserSer(userDAO, authDAO);
    GameSer gameService = new GameSer(gameDAO, authDAO);

    public Server() {
        Gson gson = new GsonBuilder().create();
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public String toJsonString(Object obj, Type type) {
                return gson.toJson(obj, type);
            }
            @Override
            public <T> T fromJsonString(String json, Type targetType) {
                return gson.fromJson(json, targetType);
            }
        };
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(gsonMapper);
        });
        registerEndpoints();
    }

    private void registerEndpoints() {
        javalin.delete("/db", this::handleClear);
        javalin.post("/user", this::handleRegister);
        javalin.post("/session", this::handleLogin);
        javalin.delete("/session", this::handleLogout);
        javalin.post("/game", this::handleCreateGame);
        javalin.get("/game", this::handleListGames);
        javalin.put("/game", this::handleJoinGame);
        javalin.exception(Exception.class, (e, ctx) ->
                ctx.status(500).json(new ErrorResp("Error: " + e.getMessage())));
    }

    private void handleClear(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.status(200).result("{}");
    }

    private void handleRegister(Context ctx) {
        try {
            RegRequest req = ctx.bodyAsClass(RegRequest.class);
            RegResult res = userService.register(req);
            ctx.status(200).json(res);
        } catch (DataAccessException e) {
            if ("bad request".equals(e.getMessage())) {
                ctx.status(400).json(new ErrorResp("Error: bad request"));
            } else {
                ctx.status(403).json(new ErrorResp("Error: already taken"));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResp("Error: " + e.getMessage()));
        }
    }

    private void handleLogin(Context ctx) {
        try {
            LoginUser req = ctx.bodyAsClass(LoginUser.class);
            LoginResult res = userService.login(req);
            ctx.status(200).json(res);
        } catch (DataAccessException e) {
            if ("bad request".equals(e.getMessage())) {
                ctx.status(400).json(new ErrorResp("Error: bad request"));
            } else {
                ctx.status(401).json(new ErrorResp("Error: unauthorized"));
            }
        }
    }

    private void handleLogout(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            userService.logout(authToken);
            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            ctx.status(401).json(new ErrorResp("Error: unauthorized"));
        }
    }

    private void handleCreateGame(Context ctx) {
        String authToken = ctx.header("authorization");
        CreateGameRequest req = ctx.bodyAsClass(CreateGameRequest.class);
        try {
            CreateGameResult res = gameService.createGame(authToken, req);
            ctx.status(200).json(res);
        } catch (DataAccessException e) {
            if ("unauthorized".equals(e.getMessage())) {
                ctx.status(401).json(new ErrorResp("Error: unauthorized"));
            } else {
                ctx.status(400).json(new ErrorResp("Error: bad request"));
            }
        }
    }

    private void handleListGames(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            ListGameRes res = gameService.listGames(authToken);
            ctx.status(200).json(res);
        } catch (DataAccessException e) {
            ctx.status(401).json(new ErrorResp("Error: unauthorized"));
        }
    }

    private void handleJoinGame(Context ctx) {
        String authToken = ctx.header("authorization");
        JoinGame req = ctx.bodyAsClass(JoinGame.class);
        try {
            gameService.joinGame(authToken, req);
            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            if ("unauthorized".equals(e.getMessage())) {
                ctx.status(401).json(new ErrorResp("Error: unauthorized"));
            } else if ("already taken".equals(e.getMessage())) {
                ctx.status(403).json(new ErrorResp("Error: already taken"));
            } else {
                ctx.status(400).json(new ErrorResp("Error: bad request"));
            }
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}