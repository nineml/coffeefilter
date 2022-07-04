package org.nineml.coffeefilter.util;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.exceptions.IxmlTreeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public abstract class XmlWriter {
    private enum XmlState {NOT_STARTED, IN_DOCUMENT, START_ELEMENT, CONTENT, OUT_DOCUMENT};
    private final HashMap<XmlQName, String> attributes;
    private final Stack<XmlQName> openElements;
    private final Stack<HashMap<String,String>> openNamespaces;
    private XmlState state;

    public XmlWriter() {
        openElements = new Stack<>();
        openNamespaces = new Stack<>();
        attributes = new HashMap<>();
        state = XmlState.NOT_STARTED;

        openNamespaces.push(new HashMap<>());
    }

    public void startDocument() {
        if (state != XmlState.NOT_STARTED) {
            throw IxmlTreeException.documentStarted();
        }
        state = XmlState.IN_DOCUMENT;
        writeStartDocument();
    }

    public void endDocument() {
        if (state == XmlState.NOT_STARTED || state == XmlState.OUT_DOCUMENT) {
            throw IxmlTreeException.documentNotStarted();
        }
        state = XmlState.OUT_DOCUMENT;
        writeEndDocument();
    }

    public void declareNamespace(String prefix, String uri) {
        if (prefix == null) {
            throw new NullPointerException("null prefix");
        }
        if (uri == null) {
            throw new NullPointerException("null uri");
        }
        if (!"".equals(prefix)) {
            try {
                TokenUtils.assertXmlName(prefix);
            } catch (IxmlException ex) {
                throw IxmlTreeException.invalidPrefix(prefix);
            }
        }
        if (openNamespaces.peek().containsKey(prefix)) {
            throw IxmlTreeException.namespaceRedefined(prefix);
        }
        openNamespaces.peek().put(prefix, uri);
    }

    public void startElement(String tag) {
        try {
            TokenUtils.assertXmlName(tag);
        } catch (IxmlException ex) {
            throw IxmlTreeException.invalidName(tag);
        }
        XmlQName name = parseName(tag);

        if (state == XmlState.NOT_STARTED) {
            startDocument();
        }

        writeStart();
        state = XmlState.START_ELEMENT;
        openElements.push(name);
        openNamespaces.push(new HashMap<>());
    }

    public void addAttribute(String name, String value) {
        if (name == null) {
            throw new NullPointerException("null name");
        }
        if (value == null) {
            value = "";
        }

        if (state != XmlState.START_ELEMENT) {
            throw IxmlTreeException.attributeNotAllowed();
        }

        try {
            TokenUtils.assertXmlName(name);
        } catch (IxmlException ex) {
            throw IxmlTreeException.invalidName(name);
        }

        XmlQName attrName = parseName(name);
        if (attributes.containsKey(attrName)) {
            throw IxmlTreeException.attributeRedefined(name);
        }

        attributes.put(attrName, value);
    }

    public void endElement(String tag) {
        XmlQName qname = parseName(tag);
        if (openElements.isEmpty()) {
            throw IxmlTreeException.unbalancedTags(tag);
        }
        if (!qname.equals(openElements.peek())) {
            throw IxmlTreeException.unbalancedTags(openElements.peek().toString(), tag);
        }
        endElement();
    }

    public void endElement() {
        if (state != XmlState.CONTENT && state != XmlState.START_ELEMENT) {
            throw IxmlTreeException.endElementNotAllowed();
        }
        if (openElements.isEmpty()) {
            throw IxmlTreeException.unbalancedTags();
        }
        writeStart();
        writeEnd();
        openElements.pop();
        openNamespaces.pop();
        for (String prefix : openNamespaces.peek().keySet()) {
            endPrefixMapping(prefix, openNamespaces.peek().get(prefix));
        }
        openNamespaces.peek().clear();
    }

    public void comment(String text) {
        writeStart();
        if (state != XmlState.CONTENT) {
            throw IxmlTreeException.commentNotAllowed();
        }
        writeComment(text);
    }

    public void processingInstruction(String name) {
        processingInstruction(name, "");
    }

    public void processingInstruction(String name, String data) {
        if (name == null) {
            throw new NullPointerException("null name");
        }
        if (data == null) {
            data = "";
        }

        try {
            TokenUtils.assertXmlName(name);
        } catch (IxmlException ex) {
            throw IxmlTreeException.invalidName(name);
        }

        writeStart();
        if (state != XmlState.CONTENT) {
            throw IxmlTreeException.processingInstructionNotAllowed();
        }
        writeProcessingInstruction(name, data);
    }

    public void text(String text) {
        writeStart();
        if (state != XmlState.CONTENT) {
            throw IxmlTreeException.textNotAllowed();
        }
        writeText(text);
    }

    public Map<String,String> getInScopeNamespaces() {
        HashMap<String,String> ns = new HashMap<>();
        for (HashMap<String, String> stackns : openNamespaces) {
            for (String prefix : stackns.keySet()) {
                ns.put(prefix, stackns.get(prefix));
            }
        }
        return ns;
    }

    private XmlQName parseName(String name) {
        final XmlQName qName;
        int pos = name.indexOf(':');
        if (pos >= 0) {
            String prefix = name.substring(0, pos);
            String local = name.substring(pos + 1);
            if ("".equals(prefix) || "".equals(local)) {
                throw IxmlTreeException.invalidName(name);
            }

            String uri = null;
            int depth = openNamespaces.size();
            while (depth > 0) {
                if (openNamespaces.get(depth-1).containsKey(prefix)) {
                    uri = openNamespaces.get(depth-1).get(prefix);
                    break;
                }
                depth--;
            }

            if (uri == null) {
                throw IxmlTreeException.invalidPrefix(name);
            }

            qName = new XmlQName(prefix, local, uri);
        } else {
            qName = new XmlQName(name);
        }
        return qName;
    }

    private void writeStart() {
        if (state != XmlState.START_ELEMENT || openElements.isEmpty()) {
            return;
        }

        // By the time writeStart() is called, we've always pushed the namespace
        // map for the next element. Look one below the top.
        int index = openNamespaces.size()-2;
        for (String prefix : openNamespaces.get(index).keySet()) {
            startPrefixMapping(prefix, openNamespaces.get(index).get(prefix));
        }

        writeStartElement(openElements.peek(), attributes);
        attributes.clear();
        state = XmlState.CONTENT;
    }

    private void writeEnd() {
        writeEndElement(openElements.peek());
        state = XmlState.CONTENT;
    }

    protected abstract void writeStartDocument();
    protected abstract void writeEndDocument();
    protected abstract void startPrefixMapping(String prefix, String uri);
    protected abstract void endPrefixMapping(String prefix, String uri);
    protected abstract void writeStartElement(XmlQName tag, Map<XmlQName, String> attributes);
    protected abstract void writeEndElement(XmlQName tag);
    protected abstract void writeText(String text);
    protected abstract void writeComment(String comment);
    protected abstract void writeProcessingInstruction(String name, String data);

    protected static class XmlQName implements Comparable<XmlQName> {
        public final String prefix;
        public final String localName;
        public final String namespaceURI;

        public XmlQName(String localname) {
            this.prefix = "";
            this.localName = localname;
            this.namespaceURI = "";
        }

        public XmlQName(String prefix, String localname, String namespaceURI) {
            this.prefix = prefix;
            this.localName = localname;
            this.namespaceURI = namespaceURI;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof XmlQName) {
               XmlQName other = (XmlQName) obj;
                return namespaceURI.equals(other.namespaceURI) && localName.equals(other.localName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return localName.hashCode() + (31 * namespaceURI.hashCode());
        }

        @Override
        public String toString() {
            if ("".equals(prefix)) {
                return localName;
            }
            return prefix + ":" + localName;
        }

        @Override
        public int compareTo(XmlQName qname) {
            return toString().compareTo(qname.toString());
        }
    }
}