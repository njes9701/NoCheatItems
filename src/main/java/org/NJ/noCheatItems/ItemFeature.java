package org.NJ.noCheatItems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public record ItemFeature(
    Material material, 
    String name, 
    List<String> lore, 
    Map<String, Integer> enchants,
    Map<String, Collection<AttributeModifier>> attributes,
    boolean unbreakable
) {
    public static ItemFeature from(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return new ItemFeature(item.getType(), null, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(), false);

        // 提取屬性修改器 (Attribute Modifiers)
        Map<String, Collection<AttributeModifier>> attrs = Collections.emptyMap();
        if (meta.hasAttributeModifiers()) {
            attrs = new HashMap<>();
            var modifiers = meta.getAttributeModifiers();
            for (Attribute attr : modifiers.keySet()) {
                attrs.put(attr.name(), modifiers.get(attr));
            }
        }

        return new ItemFeature(
            item.getType(),
            meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : null,
            meta.hasLore() ? meta.getLore().stream().map(ChatColor::stripColor).toList() : Collections.emptyList(),
            meta.getEnchants().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getKey().getKey(), Map.Entry::getValue)),
            attrs,
            meta.isUnbreakable()
        );
    }

    public boolean matches(ItemFeature other) {
        if (other == null) return false;
        
        // 基礎特徵比對
        if (this.material != other.material) return false;
        if (!Objects.equals(this.name, other.name)) return false;
        if (!this.lore.equals(other.lore)) return false;
        if (!this.enchants.equals(other.enchants)) return false;
        if (this.unbreakable != other.unbreakable) return false;

        // 屬性修改器比對 (忽略 UUID，比對名稱、數值與操作方式)
        return compareAttributes(this.attributes, other.attributes);
    }

    private boolean compareAttributes(Map<String, Collection<AttributeModifier>> a, Map<String, Collection<AttributeModifier>> b) {
        if (a.size() != b.size()) return false;
        for (String key : a.keySet()) {
            if (!b.containsKey(key)) return false;
            Collection<AttributeModifier> modsA = a.get(key);
            Collection<AttributeModifier> modsB = b.get(key);
            if (modsA.size() != modsB.size()) return false;

            // 這裡進行簡化的內容比對，不比對 UUID (因為 UUID 可能會變)
            for (AttributeModifier ma : modsA) {
                boolean found = false;
                for (AttributeModifier mb : modsB) {
                    if (ma.getAmount() == mb.getAmount() && ma.getOperation() == mb.getOperation() && ma.getName().equals(mb.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }
        return true;
    }
}