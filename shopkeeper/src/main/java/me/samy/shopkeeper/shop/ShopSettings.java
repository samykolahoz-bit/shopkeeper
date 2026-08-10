package me.samy.shopkeeper.shop;

import java.util.HashMap;
import java.util.Map;

public class ShopSettings {
    public boolean invulnerable = true;
    public boolean preventMovement = true;
    public boolean preventDespawn = true;
    public boolean preventPush = true;
    public boolean preventAttack = true;

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("invulnerable", invulnerable);
        m.put("prevent-movement", preventMovement);
        m.put("prevent-despawn", preventDespawn);
        m.put("prevent-push", preventPush);
        m.put("prevent-attack", preventAttack);
        return m;
    }

    public static ShopSettings fromMap(Map<String, Object> m) {
        ShopSettings s = new ShopSettings();
        if (m.containsKey("invulnerable")) s.invulnerable = Boolean.parseBoolean(String.valueOf(m.get("invulnerable")));
        if (m.containsKey("prevent-movement")) s.preventMovement = Boolean.parseBoolean(String.valueOf(m.get("prevent-movement")));
        if (m.containsKey("prevent-despawn")) s.preventDespawn = Boolean.parseBoolean(String.valueOf(m.get("prevent-despawn")));
        if (m.containsKey("prevent-push")) s.preventPush = Boolean.parseBoolean(String.valueOf(m.get("prevent-push")));
        if (m.containsKey("prevent-attack")) s.preventAttack = Boolean.parseBoolean(String.valueOf(m.get("prevent-attack")));
        return s;
    }
}