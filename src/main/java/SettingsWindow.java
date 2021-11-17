import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static constants.Constants.*;

public class SettingsWindow implements Configurable {
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JTextArea textAreaStock;
    /**
     * 使用tab界面，方便不同的设置分开进行控制
     */
    private JSpinner spinnerStock;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Leeks";
    }

    @Override
    public @Nullable JComponent createComponent() {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        String value_stock = instance.getValue(KEY_STOCKS);
        textAreaStock.setText(value_stock);
        spinnerStock.setModel(new SpinnerNumberModel(Math.max(1, instance.getInt(KEY_STOCK_REFRESH_INTERVAL, 10)), 1, Integer.MAX_VALUE, 1));
        return panel1;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        instance.setValues(KEY_STOCKS, textAreaStock.getText().split("[,，\\s]+"));
        instance.setValue(KEY_STOCK_REFRESH_INTERVAL, spinnerStock.getValue().toString());
        StockWindow.apply();
    }
}
