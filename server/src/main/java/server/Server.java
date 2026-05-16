package server;

import io.javalin.*;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.DataAccessException;
import service.ClearSer;
import service.RegRequest;
import service.RegResult;
import service.UserSer;
import service.ErrorResp;
import service.LoginUser;
import service.LoginResult;
import service.CreateGameResult;
import service.CreateGameRequest;
import service.GameSer;
import service.ListGameRes;

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
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        //clear
        javalin.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        });

        //new user
        javalin.post("/user", ctx -> {

            RegRequest req = ctx.bodyAsClass(RegRequest.class);

            try {
                RegResult res = userService.register(req);
                ctx.status(200);
                ctx.json(res);
            } catch (DataAccessException e) {
                ctx.status(403);
                ctx.json(new ErrorResp("Error: already taken"));
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(new ErrorResp("Error: " + e.getMessage()));
            }
        });

        //login
        javalin.post("/session", ctx -> {
            LoginUser req = ctx.bodyAsClass(LoginUser.class);
            try {
                LoginResult res = userService.login(req);
                ctx.status(200);
                ctx.json(res);
            } catch (DataAccessException e) {
                ctx.status(401);
                ctx.json(new ErrorResp("Error: unauthorized"));
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
