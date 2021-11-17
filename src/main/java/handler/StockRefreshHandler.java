package handler;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import bean.StockBean;
import org.apache.commons.lang3.ArrayUtils;
import render.OperationTableCellRender;
import utils.ConfigUtil;
import utils.WindowUtils;
import utils.WindowUtils.StockTableHeaders;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;

public abstract class StockRefreshHandler extends DefaultTableModel {
    private static String[] COLUMNS;
    /**
     * 存放【编码】的位置，更新数据时用到
     */
    public int codeColumnIndex;

    private JBTable table;
    private boolean colorful = true;

    static {
        PropertiesComponent instance = PropertiesComponent.getInstance();
        String[] headers = instance.getValues(WindowUtils.STOCK_TABLE_HEADER_KEY);
        if (ArrayUtils.isEmpty(headers)) {
            headers = Arrays.stream(StockTableHeaders.values()).map(StockTableHeaders::getCnName).toArray(String[]::new);
            instance.setValues(WindowUtils.STOCK_TABLE_HEADER_KEY, headers);
        }
        COLUMNS = headers;
    }


    /**
     * 更新数据的间隔时间（秒）
     */
    protected volatile int threadSleepTime = 10;

    public StockRefreshHandler(JBTable table) {
        this.table = table;
        table.setAutoResizeMode(JBTable.AUTO_RESIZE_OFF);
        // Fix tree row height
        FontMetrics metrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(Math.max(table.getRowHeight(), metrics.getHeight()));
        table.setModel(this);
        refreshColorful(!colorful);
    }

    public void refreshColorful(boolean colorful) {
        if (this.colorful == colorful) {
            return;
        }
        this.colorful = colorful;
        setColumnIdentifiers(COLUMNS);

        // 刷新表头
        this.table.getColumn(StockTableHeaders.OPERATION.getCnName()).setCellRenderer(new OperationTableCellRender());

        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(this);
        Comparator<Object> doubleComparator = (o1, o2) -> {
            Double v1 = NumberUtils.toDouble(StringUtils.remove((String) o1, '%'));
            Double v2 = NumberUtils.toDouble(StringUtils.remove((String) o2, '%'));
            return v1.compareTo(v2);
        };
        StockTableHeaders[] headersForSort = new StockTableHeaders[]{StockTableHeaders.STOCK_PRICE,
                StockTableHeaders.STOCK_INCREASING, StockTableHeaders.STOCK_INCREASING_PERCENT,
                StockTableHeaders.STOCK_HIGHEST, StockTableHeaders.STOCK_LOWEST
        };

        Arrays.stream(headersForSort)
                .map(StockTableHeaders::getCnName)
                .map(header -> ArrayUtils.indexOf(COLUMNS, header))
                .filter(index -> index >= 0)
                .forEach(index -> rowSorter.setComparator(index, doubleComparator));
        table.setRowSorter(rowSorter);
        columnColors(colorful);
    }

    /**
     * 从网络更新数据
     */
    public abstract void handle();

    public abstract void refreshNow();

    /**
     * 设置表格条纹（斑马线）<br>
     *
     * @param striped true设置条纹
     * @throws RuntimeException 如果table不是{@link JBTable}类型，请自行实现setStriped
     */
    public void setStriped(boolean striped) {
        if (table != null) {
            table.setStriped(striped);
        } else {
            throw new RuntimeException("table不是JBTable类型，请自行实现setStriped");
        }
    }

    public void setupTable() {
        for (String s : ConfigUtil.loadStocks()) {
            updateData(new StockBean(s));
        }
    }

    /**
     * 停止从网络更新数据
     */
    public abstract void stopHandle();

    private void columnColors(boolean colorful) {
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                double temp = NumberUtils.toDouble(StringUtils.remove(Objects.toString(value), "%"));
                if (temp > 0) {
                    if (colorful) {
                        setForeground(JBColor.RED);
                    } else {
                        setForeground(JBColor.DARK_GRAY);
                    }
                } else if (temp < 0) {
                    if (colorful) {
                        setForeground(JBColor.GREEN);
                    } else {
                        setForeground(JBColor.GRAY);
                    }
                } else if (temp == 0) {
                    Color orgin = getForeground();
                    setForeground(orgin);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        int columnIndex1 = ArrayUtils.indexOf(COLUMNS, StockTableHeaders.STOCK_INCREASING.getCnName());
        int columnIndex2 = ArrayUtils.indexOf(COLUMNS, StockTableHeaders.STOCK_INCREASING_PERCENT.getCnName());
        table.getColumn(getColumnName(columnIndex1)).setCellRenderer(cellRenderer);
        table.getColumn(getColumnName(columnIndex2)).setCellRenderer(cellRenderer);
    }

    protected void updateData(StockBean bean) {
        if (bean.getCode() == null) {
            return;
        }
        Vector<Object> convertData = convertData(bean);
        if (convertData == null) {
            return;
        }
        // 获取行
        int index = findRowIndex(codeColumnIndex, bean.getCode());
        if (index >= 0) {
            updateRow(index, convertData);
        } else {
            addRow(convertData);
        }
    }

    /**
     * 参考源码{@link DefaultTableModel#setValueAt}，此为直接更新行，提高点效率
     *
     * @param rowIndex
     * @param rowData
     */
    protected void updateRow(int rowIndex, Vector<Object> rowData) {
        dataVector.set(rowIndex, rowData);
        // 通知listeners刷新ui
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * 参考源码{@link DefaultTableModel#removeRow(int)}，此为直接清除全部行，提高点效率
     */
    public void clearRow() {
        int size = dataVector.size();
        if (0 < size) {
            dataVector.clear();
            // 通知listeners刷新ui
            fireTableRowsDeleted(0, size - 1);
        }
    }

    /**
     * 查找列项中的valueName所在的行
     *
     * @param columnIndex 列号
     * @param value       值
     * @return 如果不存在返回-1
     */
    protected int findRowIndex(int columnIndex, String value) {
        int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object valueAt = getValueAt(rowIndex, columnIndex);
            if (StringUtils.equalsIgnoreCase(value, valueAt.toString())) {
                return rowIndex;
            }
        }
        return -1;
    }

    private Vector<Object> convertData(StockBean stockBean) {
        if (stockBean == null) {
            return null;
        }
        // 与columnNames中的元素保持一致
        Vector<Object> v = new Vector<>(COLUMNS.length);
        for (String header : COLUMNS) {
            if (StringUtils.equals(StockTableHeaders.OPERATION.getCnName(), header)) {
                v.addElement(AllIcons.Welcome.Project.Remove);
            } else {
                v.addElement(stockBean.getValueByColumn(header));
            }
        }
        return v;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public int getThreadSleepTime() {
        return threadSleepTime;
    }

    public void setThreadSleepTime(int threadSleepTime) {
        this.threadSleepTime = threadSleepTime;
    }
}
