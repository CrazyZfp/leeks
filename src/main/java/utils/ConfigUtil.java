package utils;

import com.intellij.ide.util.PropertiesComponent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static constants.Constants.*;

public class ConfigUtil {
    private static List<String> getCodes(String key) {
        String[] values = PropertiesComponent.getInstance().getValues(key);
        if (ArrayUtils.isEmpty(values)) {
            return new ArrayList<>();
        }

        return Arrays.stream(values).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    public static List<String> loadStocks() {
        return ConfigUtil.getCodes(KEY_STOCKS);
    }

    public static void removeStocks(String... codes) {
        List<String> stocks = loadStocks();
        stocks.removeIf(code -> ArrayUtils.contains(codes, code));
        PropertiesComponent.getInstance().setValues(KEY_STOCKS, stocks.toArray(new String[0]));
    }
}
