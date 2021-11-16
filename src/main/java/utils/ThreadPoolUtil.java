package utils;


import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    private static ThreadPoolExecutor TPE;

    static {
        TPE = new ThreadPoolExecutor(1, 2, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("CUZ-" + LocalDateTime.now().toString());
                    t.setDaemon(true);
                    return t;
                });
    }

    public static void execute(Runnable r) {
        TPE.execute(r);
    }
}
