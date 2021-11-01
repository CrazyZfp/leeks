package utils;

import com.intellij.ide.util.PropertiesComponent;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfigUtil {
    private static List<String> getCodes(String key) {
        String value = PropertiesComponent.getInstance().getValue(key);
        if (StringUtils.isEmpty(value)) {
            return new ArrayList<>();
        }
        Set<String> set = new LinkedHashSet<>();
        String[] codes = value.split("[,，]");
        for (String code : codes) {
            if (!code.isEmpty()) {
                set.add(code.trim());
            }
        }
        return new ArrayList<>(set);
    }

    public static List<String> loadCoins() {
        return ConfigUtil.getCodes("key_coins");
    }

    public static List<String> loadFunds() {
        return ConfigUtil.getCodes("key_funds");
    }

    public static List<String> loadStocks() {
        return ConfigUtil.getCodes("key_stocks");
    }
}
