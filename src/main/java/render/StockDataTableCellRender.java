package render;

import utils.WindowUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StockDataTableCellRender extends DefaultTableCellRenderer {

    private boolean inited;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (inited) {
            return this;
        }
        inited = true;
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.setValue(((WindowUtils.StockTableHeaders) value).getCnName());
        return this;
    }
}
