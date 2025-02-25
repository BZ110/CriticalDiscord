package org.criticalking.criticalDiscord;

import java.util.HashMap;
import java.util.Map;

public class Table {
    private String tableName;
    HashMap<String, String> columns = new HashMap<>();


    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(String item, String type) {
        this.columns.put(item, type);
    }

    public String getInitCommand() {
        // Start of the CREATE TABLE statement
        String a = "(";
        StringBuilder b = new StringBuilder();

        // Loop through the columns and build the column definitions
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            b.append(key).append(" ").append(value).append(", ");
        }

        // Remove the trailing comma and space
        if (!b.isEmpty()) b.setLength(b.length() - 2);

        // Close the parentheses
        String c = ")";

        // Concatenate the final SQL query string

        // Return the query
        return "CREATE TABLE IF NOT EXISTS " + tableName + a + b + c;
    }

    public String getTableName() {
        return tableName;
    }
}
