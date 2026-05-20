package service;

import dataaccess.*;

public class ClearSer {

    //generate constructor
    private final DataAccess dataAccess;

    public ClearSer(DataAccess dataAccess) {

        this.dataAccess = dataAccess;
    }

    //clear data function
    public void clear() throws DataAccessException {

        dataAccess.clear();
    }
}