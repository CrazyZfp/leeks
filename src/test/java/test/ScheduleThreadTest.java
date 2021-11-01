package test;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleThreadTest {

    @Test
    public void test() throws InterruptedException {

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> System.out.println("A"), 1, 1, TimeUnit.SECONDS);
        ses.scheduleAtFixedRate(() -> System.out.println("B"), 1, 1, TimeUnit.SECONDS);

        Thread.sleep(100000);

    }
}
