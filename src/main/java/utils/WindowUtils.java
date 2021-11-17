package utils;

import org.apache.commons.lang3.StringUtils;
import render.OperationTableCellRender;
import render.StockDataTableCellRender;

import javax.swing.table.TableCellRenderer;
import java.util.function.Supplier;

/**
 * @Created by DAIE
 * @Date 2021/3/8 20:26
 * @Description leek面板TABLE工具类
 */
public class WindowUtils {
    //股票表头
    public static final String STOCK_TABLE_HEADER_KEY = "stock_table_header_key";


    public enum StockTableHeaders {
        STOCK_CODE("编码", StockDataTableCellRender::new),
        STOCK_NAME("名称", StockDataTableCellRender::new),
        STOCK_PRICE("当前价格", StockDataTableCellRender::new),
        STOCK_INCREASING("涨跌", StockDataTableCellRender::new),
        STOCK_INCREASING_PERCENT("涨幅", StockDataTableCellRender::new),
        STOCK_HIGHEST("最高价", StockDataTableCellRender::new),
        STOCK_LOWEST("最低价", StockDataTableCellRender::new),
        UPDATE_TIME("更新时间", StockDataTableCellRender::new),
        OPERATION("操作", OperationTableCellRender::new);

        private final String cnName;
        private final Supplier<TableCellRenderer> renderSupplier;

        StockTableHeaders(String cnName, Supplier<TableCellRenderer> renderSupplier) {
            this.cnName = cnName;
            this.renderSupplier = renderSupplier;
        }

        public String getCnName() {
            return this.cnName;
        }

        public static StockTableHeaders of(String cnName) {
            for (StockTableHeaders header : StockTableHeaders.values()) {
                if (StringUtils.equals(cnName, header.getCnName())) {
                    return header;
                }
            }
            return STOCK_CODE;
        }
    }
}
