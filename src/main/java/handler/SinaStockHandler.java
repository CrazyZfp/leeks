package handler;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import bean.StockBean;
import com.intellij.ui.table.JBTable;
import org.apache.commons.collections.CollectionUtils;
import utils.ConfigUtil;
import utils.HttpClientPool;
import utils.LogUtil;
import utils.ThreadPoolUtil;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinaStockHandler extends StockRefreshHandler {
    private static final String URL = "https://hq.sinajs.cn/list=";
    private static final Pattern DEFAULT_STOCK_PATTERN = Pattern.compile("var hq_str_(\\w+?)=\"(.*?)\";");
    private final JLabel refreshTimeLabel;

    private static ScheduledExecutorService mSchedulerExecutor;

    public SinaStockHandler(JBTable table, JLabel label) {
        super(table);
        this.refreshTimeLabel = label;
    }

    @Override
    public void handle() {
        if (Objects.isNull(mSchedulerExecutor) || mSchedulerExecutor.isShutdown()) {
            mSchedulerExecutor = Executors.newSingleThreadScheduledExecutor();
            mSchedulerExecutor.scheduleAtFixedRate(getWork(), 0, threadSleepTime, TimeUnit.SECONDS);
        }
    }

    @Override
    public void refreshNow() {
        ThreadPoolUtil.execute(getWork());
        this.handle();
    }

    private Runnable getWork() {
        return () -> pollStock(ConfigUtil.loadStocks());
    }

    private void pollStock(List<String> code) {

        if (CollectionUtils.isEmpty(code)) {
            return;
        }

        String params = Joiner.on(",").join(code);
        try {
            String res = HttpClientPool.getHttpClient().get(URL + params);
//            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"));
//            System.out.printf("%s,%s%n", time, res);
            handleResponse(res);
        } catch (Exception e) {
            LogUtil.info(e.getMessage());
        }
    }

    public void handleResponse(String response) {
        List<String> refreshTimeList = new ArrayList<>();
        for (String line : response.split("\n")) {
            Matcher matcher = DEFAULT_STOCK_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String code = matcher.group(1);
            String[] split = matcher.group(2).split(",");
            if (split.length < 32) {
                continue;
            }
            StockBean bean = new StockBean(code);
            bean.setName(split[0]);
            BigDecimal now = new BigDecimal(split[3]);
            BigDecimal yesterday = new BigDecimal(split[2]);
            BigDecimal diff = now.add(yesterday.negate());

            bean.setNow(now.toString());
            bean.setChange(diff.toString());
            BigDecimal percent = diff.divide(yesterday, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.TEN)
                    .multiply(BigDecimal.TEN)
                    .setScale(2, RoundingMode.HALF_UP);
            bean.setChangePercent(percent.toString());
            bean.setTime(Strings.repeat("0", 8) + split[31]);
            bean.setMax(split[4]);
            bean.setMin(split[5]);
            updateData(bean);
            refreshTimeList.add(split[31]);
        }

        String text = refreshTimeList.stream().sorted().findFirst().orElse("");
        SwingUtilities.invokeLater(() -> refreshTimeLabel.setText(text));
    }

    @Override
    public void stopHandle() {
        if (Objects.nonNull(mSchedulerExecutor)) {
            mSchedulerExecutor.shutdown();
        }
        LogUtil.info("leeks stock 自动刷新关闭!");
    }
}
