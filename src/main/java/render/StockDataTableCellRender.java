package render;

import com.intellij.ide.util.PropertiesComponent;
import utils.PinYinUtils;
import utils.WindowUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static constants.Constants.KEY_COLORFUL;

public class StockDataTableCellRender extends DefaultTableCellRenderer {

    private boolean inited;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (inited) {
            return this;
        }

        inited = true;

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        PropertiesComponent pc = PropertiesComponent.getInstance();
        if (pc.getBoolean(KEY_COLORFUL)) {
            this.setValue(((WindowUtils.StockTableHeaders) value).getDisplayName());
        } else {
            this.setValue(PinYinUtils.toPinYin(((WindowUtils.StockTableHeaders) value).getDisplayName()));
        }

        return this;
    }
}
