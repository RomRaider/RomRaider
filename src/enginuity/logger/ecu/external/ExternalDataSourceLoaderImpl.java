package enginuity.logger.ecu.external;

import enginuity.logger.ecu.definition.ExternalData;
import enginuity.logger.ecu.definition.ExternalDataImpl;
import enginuity.logger.ecu.definition.plugin.PluginFilenameFilter;
import enginuity.logger.ecu.exception.ConfigurationException;
import static enginuity.util.ParamChecker.isNullOrEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class ExternalDataSourceLoaderImpl implements ExternalDataSourceLoader {
    private List<ExternalData> externalDatas = new ArrayList<ExternalData>();

    public void loadFromExternalDataSources() {
        try {
            File pluginsDir = new File("./plugins");
            if (pluginsDir.exists() && pluginsDir.isDirectory()) {
                externalDatas = new ArrayList<ExternalData>();
                File[] pluginPropertyFiles = pluginsDir.listFiles(new PluginFilenameFilter());
                for (File pluginPropertyFile : pluginPropertyFiles) {
                    Properties pluginProps = new Properties();
                    FileInputStream inputStream = new FileInputStream(pluginPropertyFile);
                    try {
                        pluginProps.load(inputStream);
                        String datasourceClassName = pluginProps.getProperty("datasource.class");
                        if (!isNullOrEmpty(datasourceClassName)) {
                            try {
                                Class<?> dataSourceClass = getClass().getClassLoader().loadClass(datasourceClassName);
                                if (dataSourceClass != null && ExternalDataSource.class.isAssignableFrom(dataSourceClass)) {
                                    ExternalDataSource dataSource = (ExternalDataSource) dataSourceClass.newInstance();
                                    List<ExternalDataItem> dataItems = dataSource.getDataItems();
                                    for (ExternalDataItem dataItem : dataItems) {
                                        externalDatas.add(new ExternalDataImpl(dataItem));
                                    }
                                    System.out.println("Plugin loaded: " + dataSource.getName() + " v" + dataSource.getVersion());
                                }
                            } catch (Throwable t) {
                                System.out.println("Error loading external datasource: " + datasourceClassName + ", specified in: "
                                        + pluginPropertyFile.getAbsolutePath());
                                t.printStackTrace();
                            }
                        }
                    } finally {
                        inputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public List<ExternalData> getExternalDatas() {
        return externalDatas;
    }
}
