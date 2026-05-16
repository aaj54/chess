package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import service.ClearSer;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.ErrorResp;
import service.GameSer;
import service.ListGameRes;
import service.LoginResult;
import service.LoginUser;
import service.RegRequest;
import service.RegResult;
import service.UserSer;
import java.lang.reflect.Type;

public class Server {

    private final Javalin javalin;

    //set memory value from new data access elements
    MemoryUserDAO userDAO = new MemoryUserDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    MemoryGameDAO gameDAO = new MemoryGameDAO();

    //set class variables
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

        //clear
        javalin.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        });

        //new user
        javalin.post("/user", ctx -> {
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
        });

        //login
        javalin.post("/session", ctx -> {
            try {
                LoginUser req = ctx.bodyAsClass(LoginUser.class);
                LoginResult res = userService.login(req);
                ctx.status(200);
                ctx.json(res);
            } catch (DataAccessException e) {
                if ("bad request".equals(e.getMessage())) {
                    ctx.status(400);
                    ctx.json(new ErrorResp("Error: bad request"));
                } else {
                    ctx.status(401);
                    ctx.json(new ErrorResp("Error: unauthorized"));
                }
            }
        });

        //logout
        javalin.delete("/session", ctx -> {
            String authToken = ctx.header("authorization");
            try {
                userService.logout(authToken);
                ctx.status(200);
                ctx.result("{}");
            } catch (DataAccessException e) {
                ctx.status(401);
                ctx.json(new ErrorResp("Error: unauthorized"));
            }
        });

        //create game
        javalin.post("/game", ctx -> {
            String authToken = ctx.header("authorization");
            CreateGameRequest req = ctx.bodyAsClass(CreateGameRequest.class);
            try {
                CreateGameResult res = gameService.createGame(authToken, req);
                ctx.status(200);
                ctx.json(res);
            } catch (DataAccessException e) {
                if (e.getMessage().equals("unauthorized")) {
                    ctx.status(401);
                    ctx.json(new ErrorResp("Error: unauthorized"));
                } else {
                    ctx.status(400);
                    ctx.json(new ErrorResp("Error: bad request"));
                }
            }
        });

        //list game
        javalin.get("/game", ctx -> {
            String authToken = ctx.header("authorization");
            try {
                ListGameRes res = gameService.listGames(authToken);
                ctx.status(200);
                ctx.json(res);
            } catch (DataAccessException e) {
                ctx.status(401);
                ctx.json(new ErrorResp("Error: unauthorized"));
            }
        });

        // Unhandled exceptions
        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(new ErrorResp("Error: " + e.getMessage()));
        });

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
