package org.nineml.coffeefilter.util;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.exceptions.IxmlTreeException;

import java.util.*;

public class XmlStringWriter extends XmlWriter {
    private final StringBuilder xml;
    private final HashMap<String,String> namespaces;

    public XmlStringWriter() {
        super();
        xml = new StringBuilder();
        namespaces = new HashMap<>();
    }

    public String getXml() {
        return xml.toString();
    }

    protected void writeStartDocument() {
        // nop;
    }

    protected void writeEndDocument() {
        xml.append("\n");
    }

    protected void startPrefixMapping(String prefix, String uri) {
        namespaces.put(prefix, uri);
    }

    protected void endPrefixMapping(String prefix, String uri) {
        // nop
    }

    protected void writeStartElement(XmlQName tag, Map<XmlQName, String> attributes) {
        xml.append("<");
        if (!"".equals(tag.prefix)) {
            xml.append(tag.prefix).append(":").append(tag.localName);
        } else {
            xml.append(tag.localName);
        }

        if (!namespaces.isEmpty()) {
            // Cheap and cheerful
            int pos = 0;
            String[] prefixes = new String[namespaces.size()];
            for (String prefix : namespaces.keySet()) {
                prefixes[pos++] = prefix;
            }
            Arrays.sort(prefixes);

            for (String prefix : prefixes) {
                xml.append(" ");
                if ("".equals(prefix)) {
                    xml.append("xmlns").append("=");
                } else {
                    xml.append("xmlns:").append(prefix).append("=");
                }
                String value = namespaces.get(prefix);
                char quote = value.contains("'") ? '"' : '\'';
                xml.append(quote).append(escapeCharacters(value, quote)).append(quote);
            }
            namespaces.clear();
        }

        if (!attributes.isEmpty()) {
            // Cheap and cheerful
            int pos = 0;
            XmlQName[] names = new XmlQName[attributes.size()];
            for (XmlQName name : attributes.keySet()) {
                names[pos++] = name;
            }
            Arrays.sort(names);

            for (XmlQName attr : names) {
                xml.append(" ");
                if (!"".equals(attr.prefix)) {
                    xml.append(attr.prefix).append(":").append(attr.localName);
                } else {
                    xml.append(attr.localName);
                }
                xml.append("=");
                String value = attributes.get(attr);
                char quote = value.contains("'") ? '"' : '\'';
                xml.append(quote).append(escapeCharacters(value, quote)).append(quote);
            }
        }
        xml.append(">");
    }

    protected void writeEndElement(XmlQName tag) {
        xml.append("</");
        if (!"".equals(tag.prefix)) {
            xml.append(tag.prefix).append(":").append(tag.localName);
        } else {
            xml.append(tag.localName);
        }
        xml.append(">");
    }

    protected void writeText(String text) {
        xml.append(escapeCharacters(text));
    }

    protected void writeComment(String comment) {
        xml.append("<!--").append(escapeCharacters(comment)).append("-->");
    }

    protected void writeProcessingInstruction(String name, String data) {
        xml.append("<?").append(name);
        if (!"".equals(data)) {
            xml.append(" ").append(escapeCharacters(data));
        }
        xml.append("?>");
    }

    protected String escapeCharacters(String text) {
        return escapeCharacters(text, ' ');
    }

    protected String escapeCharacters(String text, int quote) {
        StringBuilder sb = new StringBuilder();
        for (int cp : text.codePoints().toArray()) {
            try {
                TokenUtils.assertXmlChars(cp);
            } catch (IxmlException ex) {
                throw IxmlTreeException.invalidText(new StringBuilder().appendCodePoint(cp).toString());
            }

            if (cp < ' ') {
                sb.append(String.format("&#x%x;", cp));
            } else {
                switch (cp) {
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '\'':
                        if (cp == quote) {
                            sb.append("&apos;");
                        } else {
                            sb.appendCodePoint(cp);
                        }
                        break;
                    case '"':
                        if (cp == quote) {
                            sb.append("&quot;");
                        } else {
                            sb.appendCodePoint(cp);
                        }
                        break;
                    default:
                        sb.appendCodePoint(cp);
                        break;
                }
            }
        }
        return sb.toString();
    }
}