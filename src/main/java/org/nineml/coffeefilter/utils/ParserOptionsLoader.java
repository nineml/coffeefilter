package org.nineml.coffeefilter.utils;

import org.nineml.coffeefilter.ParserOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ParserOptionsLoader {
    public ParserOptions loadOptions() {
        ParserOptions options = new ParserOptions();
        String name = "pixp.properties";
        try {
            InputStream stream = getClass().getResourceAsStream(name);
            if (stream != null) {
                Properties prop = new Properties();
                prop.load(stream);

                options.verbose = "true".equals(prop.getProperty("verbose", "false"));
                options.prettyPrint = "true".equals(prop.getProperty("pretty-print", "false"));
                options.ignoreTrailingWhitespace = "true".equals(prop.getProperty("ignore-trailing-whitespace", "false"));
                options.graphviz = prop.getProperty("graphviz");
            }
        } catch (IOException ex) {
            // nevermind
        }

        return options;
    }

}
