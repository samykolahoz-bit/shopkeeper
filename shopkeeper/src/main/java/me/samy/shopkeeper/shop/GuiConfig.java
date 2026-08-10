package me.samy.shopkeeper.shop;

import java.util.HashMap;
import java.util.Map;

public class GuiConfig {
    public String title = "&8Shop";
    public int rows = 6;

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("rows", rows);
        return m;
    }

    public static GuiConfig fromMap(Map<String, Object> m) {
        GuiConfig g = new GuiConfig();
        if (m.containsKey("title")) g.title = String.valueOf(m.get("title"));
        if (m.containsKey("rows")) g.rows = Integer.parseInt(String.valueOf(m.get("rows")));
        return g;
    }
}