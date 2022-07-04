package org.nineml.coffeefilter.util;

import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.xml.sax.Attributes;

import java.util.ArrayList;

public final class AttributeBuilder implements Attributes {
    public static final String logcategory = "Attributes";
    public static final Attributes EMPTY_ATTRIBUTES = new AttributeBuilder(new ParserOptions());
    private final ParserOptions options;
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<String> values = new ArrayList<>();
    private final ArrayList<String> namespaces = new ArrayList<>();

    public AttributeBuilder(ParserOptions options) {
        this.options = options;
    }

    public void addAttribute(String name, String value) {
        addAttribute("", name, value);
    }

    public void addAttribute(String ns, String name, String value) {
        if (ns == null) {
            throw new NullPointerException("Attribute namespace must not be null");
        }
        if (name == null) {
            throw new NullPointerException("Attribute name must not be null");
        }
        if (value == null) {
            throw new NullPointerException("Attribute value must not be null");
        }

        if (options.getAssertValidXmlCharacters()) {
            TokenUtils.assertXmlName(name);
            if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
                throw IxmlException.attributeNameForbidden(name);
            }
        }

        if (options.getAssertValidXmlCharacters()) {
            TokenUtils.assertXmlChars(value);
        }


        if (names.contains(name)) {
            /* Allow them if they're the same value? Not per the current spec...
            int pos = getIndex(ns, name);
            if (value.equals(getValue(pos))) {
                options.getLogger().debug(logcategory, "Duplicated attribute: %s", name);
            } else {
                throw IxmlException.repeatedAttribute(name);
            }
             */
            throw IxmlException.repeatedAttribute(name);
        }

        namespaces.add(ns);
        names.add(name);
        values.add(value);
    }

    @Override
    public int getLength() {
        return names.size();
    }

    @Override
    public String getURI(int index) {
        if (index >= names.size()) {
            return null;
        }
        return namespaces.get(index);
    }

    @Override
    public String getLocalName(int index) {
        if (index >= names.size()) {
            return null;
        }
        String name = names.get(index);
        if (name.contains(":")) {
            return name.substring(name.indexOf(":")+1);
        }
        return name;
    }

    @Override
    public String getQName(int index) {
        return names.get(index);
    }

    @Override
    public String getType(int index) {
        return "CDATA";
    }

    @Override
    public String getValue(int index) {
        return values.get(index);
    }

    @Override
    public int getIndex(String uri, String localName) {
        for (int pos = 0; pos < names.size(); pos++) {
            if (namespaces.get(pos).equals(uri) && getLocalName(pos).equals(localName)) {
                return pos;
            }
        }
        return -1;
    }

    @Override
    public int getIndex(String qName) {
        for (int pos = 0; pos < names.size(); pos++) {
            if (names.get(pos).equals(qName)) {
                return pos;
            }
        }
        return -1;
    }

    @Override
    public String getType(String uri, String localName) {
        return "CDATA";
    }

    @Override
    public String getType(String qName) {
        return "CDATA";
    }

    @Override
    public String getValue(String uri, String localName) {
        int pos = getIndex(uri, localName);
        if (pos >= 0) {
            return values.get(pos);
        }
        return null;
    }

    @Override
    public String getValue(String qName) {
        int pos = getIndex(qName);
        if (pos >= 0) {
            return values.get(pos);
        }
        return null;
    }
}