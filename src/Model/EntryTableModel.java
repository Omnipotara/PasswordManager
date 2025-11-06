/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Omnix
 */
public class EntryTableModel extends AbstractTableModel {

    private List<PasswordEntry> entryList = new ArrayList<>();
    private String[] columns = {"Username", "Password"};
    
    public EntryTableModel(){
        
    }
    
    public EntryTableModel(List<PasswordEntry> entryList){
        this.entryList = entryList;
    }

    @Override
    public int getRowCount() {
        return entryList.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PasswordEntry entry = entryList.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return entry.getUsername();
            case 1:
                //String starredPassword = "*".repeat(entry.getPassword().length());
                String starredPassword = entry.getPassword();
                return starredPassword;
            default:
                return "N/A";
        }
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

}
