package net.lliira.illyriad.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.lliira.illyriad.map.Constants.*;

/**
 * Crawls the whole world map, and store the results into database.
 *
 * CrawlerManager expects the following keys from properties: {@value TASK_POOL_SIZE_KEY}
 */
public class CrawlerManager {

    private static final String TASK_POOL_SIZE_KEY = "crawl.pool.size";

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerManager.class);

    private final int mTaskPoolSize;
    private final Storage mStorage;
    private final Authenticator mAuthenticator;

    public CrawlerManager(Properties properties, Storage storage, Authenticator authenticator) {
        mStorage = storage;
        mAuthenticator = authenticator;
        mTaskPoolSize = Integer.valueOf(properties.getProperty(TASK_POOL_SIZE_KEY));
    }

    public void crawl() throws IOException {
        long startTime = System.currentTimeMillis();
        // Login to the game and get the cookie
        Map<String, String> cookies = mAuthenticator.login();

        ExecutorService executorService = Executors.newFixedThreadPool(mTaskPoolSize);

        int minX = MIN_X + ZOOM_LEVEL;
        int minY = MIN_Y + ZOOM_LEVEL;
        for (int y = minY; y <= MAX_Y; y += ZOOM_LEVEL * 2 + 1) {
            for (int x = minX; x <= MAX_X; x += ZOOM_LEVEL * 2 + 1) {
                executorService.execute(new CrawlTask(mStorage, cookies, x, y));
            }
        }

        executorService.shutdown();
        while (true) {
            try {
                if (executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                    break;
                }
            } catch (InterruptedException e) {
                // do nothing, since we will still be in the loop, and try to wait again.
            }
            float elapsed = (System.currentTimeMillis() - startTime) / 1000F;
            LOG.debug("{} seconds passed, still waiting crawlers to finish.", elapsed);
        }

        float elapsed = (System.currentTimeMillis() - startTime) / 1000F;
        LOG.info("Totally spent {} seconds.", elapsed);
    }
}
