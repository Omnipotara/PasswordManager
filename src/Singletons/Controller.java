/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Singletons;

import Database.DBBroker;
import Model.User;

/**
 *
 * @author Omnix
 */
public class Controller {
    private static Controller instance;
    private DBBroker dbb;
    
    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    private Controller() {
        dbb = new DBBroker();
    }

    public int insertUser(User u) {
        return dbb.userExists(u.getUsername()) ? -1 : dbb.insertUser(u);
    }

    public User loginUser(String username, String password) {
        return dbb.selectUser(username, password);
    }
    
    
}
