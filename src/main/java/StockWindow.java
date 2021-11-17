import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import handler.SinaStockHandler;
import handler.StockRefreshHandler;
import handler.TencentStockHandler;
import org.jetbrains.annotations.Nullable;
import render.OperationTableCellRender;
import utils.LogUtil;
import utils.PopupsUiUtil;
import utils.WindowUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static constants.Constants.KEY_COLORFUL;

public class StockWindow {
    private JPanel mPanel;

    static StockRefreshHandler handler;

    static JBTable table;
    static JLabel refreshTimeLabel;

    public JPanel getmPanel() {
        return mPanel;
    }

    static {
        refreshTimeLabel = new JLabel();
        refreshTimeLabel.setToolTipText("最后刷新时间");
        refreshTimeLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        table = new JBTable();
        //记录列名的变化
        table.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                String[] tableHeadChanges = Stream.iterate(0, i -> i + 1)
                        .limit(table.getColumnCount())
                        .map(table::getColumnName).toArray(String[]::new);

                PropertiesComponent instance = PropertiesComponent.getInstance();
                //将列名的修改放入环境中 key:stock_table_header_key
                instance.setValues(WindowUtils.STOCK_TABLE_HEADER_KEY, tableHeadChanges);
                //LogUtil.info(instance.getValue(WindowUtils.STOCK_TABLE_HEADER_KEY));
            }

        });

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (table.getSelectedRow() < 0) {
                    return;
                }
                TableColumn column = table.getColumnModel().getColumn(table.getSelectedColumn());
                if (column.getCellRenderer() instanceof OperationTableCellRender) {
                    ((OperationTableCellRender) column.getCellRenderer()).dispatchEvent(e);
                    return;
                }
                String code = String.valueOf(table.getModel().getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), handler.codeColumnIndex));//FIX 移动列导致的BUG
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
                    // 鼠标左键双击
                    try {
                        PopupsUiUtil.showImageByStockCode(code, PopupsUiUtil.StockShowType.min, new Point(e.getXOnScreen(), e.getYOnScreen()));
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                        LogUtil.info(ex.getMessage());
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    //鼠标右键
                    JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<PopupsUiUtil.StockShowType>("",
                            PopupsUiUtil.StockShowType.values()) {
                        @Override
                        public @NotNull String getTextFor(PopupsUiUtil.StockShowType value) {
                            return value.getDesc();
                        }

                        @Override
                        public @Nullable PopupStep onChosen(PopupsUiUtil.StockShowType selectedValue, boolean finalChoice) {
                            try {
                                PopupsUiUtil.showImageByStockCode(code, selectedValue, new Point(e.getXOnScreen(), e.getYOnScreen()));
                            } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                                LogUtil.info(ex.getMessage());
                            }
                            return super.onChosen(selectedValue, finalChoice);
                        }
                    }).show(RelativePoint.fromScreen(new Point(e.getXOnScreen(), e.getYOnScreen())));
                }
            }
        });
    }

    public StockWindow() {

        //切换接口
        handler = factoryHandler();

        AnActionButton suspendBtn = new AnActionButton("停止定时刷新", AllIcons.Actions.Pause) {
            int flag = 1;

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (flag == 1) {
                    handler.stopHandle();
                    e.getPresentation().setIcon(AllIcons.Toolwindows.ToolWindowRun);
                    e.getPresentation().setText("开启定时刷新");
                    this.updateButton(e);
                    flag = 0;
                } else {
                    handler.handle();
                    e.getPresentation().setIcon(AllIcons.Actions.Pause);
                    e.getPresentation().setText("停止定时刷新");
                    flag = 1;
                    this.updateButton(e);
                }
            }
        };

        AnActionButton refreshBtn = new AnActionButton("立即刷新", AllIcons.Actions.Refresh) {
            private long lastClickTime;

            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                if (System.currentTimeMillis() - lastClickTime > 2000) {
                    refresh();
                    this.lastClickTime = System.currentTimeMillis();
                }
            }
        };

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(table)
                .addExtraAction(refreshBtn)
                .addExtraAction(suspendBtn)
                .setToolbarPosition(ActionToolbarPosition.TOP);

        JPanel toolPanel = toolbarDecorator.createPanel();
        toolbarDecorator.getActionsPanel().add(refreshTimeLabel, BorderLayout.EAST);
        toolPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        mPanel.add(toolPanel, BorderLayout.CENTER);
        // 非主要tab，需要创建，创建时立即应用数据
        apply();
    }

    private static StockRefreshHandler factoryHandler() {

        if (Objects.nonNull(handler)) {
            handler.stopHandle();
        }

//        boolean useSinaApi = PropertiesComponent.getInstance().getBoolean("key_stocks_sina");

//        if (useSinaApi) {
        return new SinaStockHandler(table, refreshTimeLabel);
//        }
//        return new TencentStockHandler(table, refreshTimeLabel);
    }

    public static void apply() {
        if (handler != null) {
            handler = factoryHandler();
            PropertiesComponent instance = PropertiesComponent.getInstance();
            handler.setStriped(instance.getBoolean("key_table_striped"));
            handler.setThreadSleepTime(instance.getInt("key_stocks_thread_time", handler.getThreadSleepTime()));
            handler.refreshColorful(instance.getBoolean(KEY_COLORFUL));
            handler.clearRow();
            handler.setupTable();
            handler.handle();
        }
    }

    public static void refresh() {
        if (handler != null) {
            boolean colorful = PropertiesComponent.getInstance().getBoolean(KEY_COLORFUL);
            handler.refreshColorful(colorful);
            handler.refreshNow();
        }
    }
}
