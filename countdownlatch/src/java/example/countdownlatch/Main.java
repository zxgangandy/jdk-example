package example.countdownlatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    private static List<BaseHealthChecker> services;
    private static CountDownLatch latch;

    public static void main(String[] args) {
        boolean result = false;
        try {
            result = checkExternalServices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Services validation completed !! Result was =>" + result);
    }


    public static boolean checkExternalServices() throws Exception {
        latch = new CountDownLatch(3);
        services = new ArrayList<BaseHealthChecker>();
        services.add(new NetworkHealthChecker(latch));
        services.add(new CacheHealthChecker(latch));
        services.add(new DatabaseHealthChecker(latch));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(services.size(), services.size(),
                10, TimeUnit.SECONDS, new LinkedBlockingQueue());

        for (final BaseHealthChecker v : services) {
            executor.execute(v);
        }

        latch.await();

        for (final BaseHealthChecker v : services) {
            if (!v.isServiceUp()) {
                return false;
            }
        }

        executor.shutdown();

        return true;
    }
}
