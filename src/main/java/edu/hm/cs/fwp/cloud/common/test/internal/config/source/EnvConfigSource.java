package edu.hm.cs.fwp.cloud.common.test.internal.config.source;

import edu.hm.cs.fwp.cloud.common.test.internal.config.spi.ConfigSource;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public final class EnvConfigSource implements ConfigSource, Serializable {

    private static final long serialVersionUID = 4577136784726659703L;

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(System.getenv());
    }

    @Override
    public int getOrdinal() {
        return 300;
    }

    @Override
    public String getValue(String name) {
        return System.getenv(name);
    }

    @Override
    public String getName() {
        return "EnvConfigSource";
    }
}
