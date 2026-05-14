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

public class Server {

    private final Javalin javalin;

    //set memory value from new data access elements
    MemoryUserDAO userDAO = new MemoryUserDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    MemoryGameDAO gameDAO = new MemoryGameDAO();

    //set class variables
    ClearSer clearService = new ClearSer(userDAO, authDAO, gameDAO);
    UserSer userService = new UserSer(userDAO, authDAO);

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        });

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
