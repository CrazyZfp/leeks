package utils;

import org.apache.commons.lang3.StringUtils;
import render.OperationTableCellRender;
import render.StockDataTableCellRender;

import javax.swing.table.TableCellRenderer;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * @Created by DAIE
 * @Date 2021/3/8 20:26
 * @Description leek面板TABLE工具类
 */
public class WindowUtils {
    //基金表头
    public static final String FUND_TABLE_HEADER_KEY = "fund_table_header_key";
    public static final String FUND_TABLE_HEADER_VALUE = "编码,基金名称,估算净值,估算涨跌,更新时间,当日净值";
    //股票表头
    public static final String STOCK_TABLE_HEADER_KEY = "stock_table_header_key";
    public static final String STOCK_TABLE_HEADER_VALUE = "编码,股票名称,当前价,涨跌,涨跌幅,最高价,最低价,更新时间,操作";


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

        public TableCellRenderer getRenderInstance(){
            return this.renderSupplier.get();
        }
    }


    //货币表头
    public static final String COIN_TABLE_HEADER_KEY = "coin_table_header_key";
    public static final String COIN_TABLE_HEADER_VALUE = "编码,名称,当前价,更新时间";

    private static HashMap<String, String> remapPinYinMap = new HashMap<>();

    static {
        remapPinYinMap.put(PinYinUtils.toPinYin("编码"), "编码");
        remapPinYinMap.put(PinYinUtils.toPinYin("基金名称"), "基金名称");
        remapPinYinMap.put(PinYinUtils.toPinYin("估算净值"), "估算净值");
        remapPinYinMap.put(PinYinUtils.toPinYin("估算涨跌"), "估算涨跌");
        remapPinYinMap.put(PinYinUtils.toPinYin("更新时间"), "更新时间");
        remapPinYinMap.put(PinYinUtils.toPinYin("当日净值"), "当日净值");
        remapPinYinMap.put(PinYinUtils.toPinYin("股票名称"), "股票名称");
        remapPinYinMap.put(PinYinUtils.toPinYin("当前价"), "当前价");
        remapPinYinMap.put(PinYinUtils.toPinYin("涨跌"), "涨跌");
        remapPinYinMap.put(PinYinUtils.toPinYin("涨跌幅"), "涨跌幅");
        remapPinYinMap.put(PinYinUtils.toPinYin("最高价"), "最高价");
        remapPinYinMap.put(PinYinUtils.toPinYin("最低价"), "最低价");
        remapPinYinMap.put(PinYinUtils.toPinYin("名称"), "名称");
        remapPinYinMap.put(PinYinUtils.toPinYin("操作"), "操作");

    }


    /**
     * 通过列名 获取该TABLE的列的数组下标
     *
     * @param columnNames 列名数组
     * @param columnName  要获取的列名
     * @return 返回给出列名的数组下标 匹配失败返回-1
     */
    public static int getColumnIndexByName(String[] columnNames, String columnName) {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(columnName)) {
                return i;
            }
        }
        //考虑拼音编码

        return -1;
    }

    public static String remapPinYin(String pinyin) {
        return remapPinYinMap.getOrDefault(pinyin, pinyin);
    }


}
