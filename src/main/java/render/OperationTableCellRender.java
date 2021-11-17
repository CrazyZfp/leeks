package render;


import com.intellij.ide.util.PropertiesComponent;
import utils.ConfigUtil;
import utils.WindowUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class OperationTableCellRender extends DefaultTableCellRenderer {

    private boolean inited;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (inited) {
            return this;
        }
        inited = true;

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.setIcon((Icon) value);
        this.setEnabled(true);
        this.setValue(null);
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

                Object code = table.getModel().getValueAt(row, table.getColumn(WindowUtils.StockTableHeaders.STOCK_CODE.getCnName()).getModelIndex());
                ConfigUtil.removeStocks(String.valueOf(code));
                ((DefaultTableModel) table.getModel()).removeRow(row);

                updateUI();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        this.setEnabled(true);

        return this;
    }
}
