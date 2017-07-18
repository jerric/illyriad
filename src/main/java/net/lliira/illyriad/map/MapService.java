package net.lliira.illyriad.map;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place for loading & launching activities.
 */
public class MapService {

    private static final String DEFAULT_PROPERTIES_FILE = "illyriad-map.properties";

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        if (args.length > 1) {
            System.err.println("Usage: map-crawler [<property_file>]");
            System.exit(-1);
        }

        MapService mapService = (args.length == 0) ? new MapService() : new MapService(args[1]);
    }

    private final Properties mProperties;

    private final Object mConnectionFactoryLock = new Object();
    private final Object mStorageLock = new Object();
    private final Object mTownMapperLock = new Object();
    private final Object mAuthenticatorLock = new Object();
    private final Object mMapCrawlerLock = new Object();
    private final Object mResourceAnalyzerLock = new Object();
    private final Object mFoodPlotFinderLock = new Object();
    private final Object mClosestPlotFinderLock = new Object();
    private final Object mResourceFinderLock = new Object();

    private ConnectionFactory mConnectionFactory;
    private Storage mStorage;
    private TownManager mTownManager;
    private Authenticator mAuthenticator;
    private CrawlerManager mCrawlerManager;
    private ResourceAnalyzer mResourceAnalyzer;
    private FoodPlotFinder mFoodPlotFinder;
    private ClosestPlotFinder mClosestPlotFinder;
    private ResourceFinder mResourceFinder;

    public MapService() throws IOException {
        this(DEFAULT_PROPERTIES_FILE);
    }

    public MapService(String propertiesFileName) throws IOException {
        mProperties = new Properties();
        mProperties.load(new FileReader(propertiesFileName));
    }

    public ConnectionFactory getConnectionFactory() throws ClassNotFoundException {
        if (mConnectionFactory == null) {
            synchronized (mConnectionFactoryLock) {
                if (mConnectionFactory == null) {
                    mConnectionFactory = new ConnectionFactory(mProperties);
                }
            }
        }
        return mConnectionFactory;
    }

    public Storage getStorage() throws ClassNotFoundException {
        if (mStorage == null) {
            synchronized (mStorageLock) {
                if (mStorage == null) {
                    mStorage = new Storage(getConnectionFactory());
                }
            }
        }
        return mStorage;
    }

    public TownManager getTownManager() throws ClassNotFoundException {
        if (mTownManager == null) {
            synchronized (mTownMapperLock) {
                if (mTownManager == null) {
                    mTownManager = new TownManager(getStorage());
                }
            }
        }
        return mTownManager;
    }

    public Authenticator getAuthenticator() {
        if (mAuthenticator == null) {
            synchronized (mAuthenticatorLock) {
                if (mAuthenticator == null) {
                    mAuthenticator = new Authenticator(mProperties);
                }
            }
        }
        return mAuthenticator;
    }

    public CrawlerManager getCrawlerManager() throws ClassNotFoundException {
        if (mCrawlerManager == null) {
            synchronized (mMapCrawlerLock) {
                if (mCrawlerManager == null) {
                    mCrawlerManager = new CrawlerManager(mProperties, getStorage(), getAuthenticator());
                }
            }
        }
        return mCrawlerManager;
    }

    public ResourceAnalyzer getResourceAnalyzer() throws ClassNotFoundException {
        if (mResourceAnalyzer == null) {
            synchronized (mResourceAnalyzerLock) {
                if (mResourceAnalyzer == null) {
                    mResourceAnalyzer = new ResourceAnalyzer(mProperties, getStorage(), getTownManager());
                }
            }
        }
        return mResourceAnalyzer;
    }

    public FoodPlotFinder getFoodPlotFinder() throws ClassNotFoundException {
        if (mFoodPlotFinder == null) {
            synchronized (mFoodPlotFinderLock) {
                if (mFoodPlotFinder == null) {
                    mFoodPlotFinder = new FoodPlotFinder(mProperties, getStorage());
                }
            }
        }
        return mFoodPlotFinder;
    }

    public ClosestPlotFinder getClosestPlotFinder() throws ClassNotFoundException {
        if (mClosestPlotFinder == null) {
            synchronized (mClosestPlotFinderLock) {
                if (mClosestPlotFinder == null) {
                    mClosestPlotFinder = new ClosestPlotFinder(getStorage(), getTownManager());
                }
            }
        }
        return mClosestPlotFinder;
    }

    public ResourceFinder getResourceFinder() throws ClassNotFoundException {
        if (mResourceFinder == null) {
            synchronized (mResourceFinderLock) {
                if (mResourceFinder == null) {
                    mResourceFinder = new ResourceFinder(mProperties, getStorage(), getTownManager());
                }
            }
        }
        return mResourceFinder;
    }
}
