package info.lliira.illyriad.map;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.AuthenticatorManager;
import info.lliira.illyriad.map.analyze.ResourceSummarizer;
import info.lliira.illyriad.map.analyze.ValidPlotMarker;
import info.lliira.illyriad.map.crawl.MapCrawler;
import info.lliira.illyriad.map.storage.StorageFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class MapProcessor {

  public static void main(String[] args) throws IOException, SQLException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));

    var authenticator = new AuthenticatorManager(properties).first();
    var storageFactory = new StorageFactory(properties);

    var crawler = new MapCrawler(authenticator, storageFactory, properties);
    crawler.crawl();

    var summarizer = new ResourceSummarizer(properties, storageFactory);
    summarizer.run();

    var marker = new ValidPlotMarker(storageFactory);
    marker.run();

    //    var finder = new CandidateFinder(storageFactory);
    //    finder.run();
  }
}
