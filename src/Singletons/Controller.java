/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Singletons;

import Database.DBBroker;
import Model.PasswordEntry;
import Model.User;
import View.MainForm;
import java.util.List;

/**
 *
 * @author Omnix
 */
public class Controller {

    private static Controller instance;
    private DBBroker dbb;
    private MainForm mf;

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

    public List<PasswordEntry> selectEntries(User u) {
        return dbb.selectEntries(u);
    }

    public boolean insertEntry(PasswordEntry pe, User u) {
        return dbb.insertEntry(pe, u);
    }

    public MainForm getMf() {
        return mf;
    }

    public void setMf(MainForm mf) {
        this.mf = mf;
    }

    public boolean deleteEntry(PasswordEntry pe, User u) {
        return dbb.deleteEntry(pe, u);
    }

    public boolean updateEntry(PasswordEntry pe, User u) {
        return dbb.updateEntry(pe, u);
    }


}
