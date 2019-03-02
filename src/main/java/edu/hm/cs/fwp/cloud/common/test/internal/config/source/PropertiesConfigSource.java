package edu.hm.cs.fwp.cloud.common.test.internal.config.source;

import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesConfigSource implements ConfigSource, Serializable {

    private static final long serialVersionUID = -1530120546425910758L;

    private static final String CONFIG_ORDINAL_KEY = "config_ordinal";
    private static final String CONFIG_ORDINAL_DEFAULT_VALUE = "100";

    private final Map<String, String> properties;
    private final String source;
    private final int ordinal;

    public PropertiesConfigSource(URL url) throws IOException {
        this.source = url.toString();
        try (InputStream in = url.openStream()) {
            Properties p = new Properties();
            p.load(in);
            this.properties = new HashMap(p);
        }
        this.ordinal = Integer.valueOf(this.properties.getOrDefault(CONFIG_ORDINAL_KEY, CONFIG_ORDINAL_DEFAULT_VALUE));
    }

    public PropertiesConfigSource(Properties properties, String source) {
        this.properties = new HashMap(properties);
        this.source = source;
        this.ordinal = Integer.valueOf(properties.getProperty(CONFIG_ORDINAL_KEY, CONFIG_ORDINAL_DEFAULT_VALUE));
    }

    public PropertiesConfigSource(Map<String, String> properties, String source, int ordinal) {
        this.properties = new HashMap(properties);
        this.source = source;
        if (properties.containsKey(CONFIG_ORDINAL_KEY)) {
            this.ordinal = Integer.valueOf(properties.getOrDefault(CONFIG_ORDINAL_KEY, CONFIG_ORDINAL_DEFAULT_VALUE));
        } else {
            this.ordinal = ordinal;
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public int getOrdinal() {
        return this.ordinal;
    }

    @Override
    public String getValue(String s) {
        return this.properties.get(s);
    }

    @Override
    public String getName() {
        return "PropertiesConfigSource[source=" + this.source + "]";
    }

    @Override
    public String toString() {
        return getName();
    }
}
