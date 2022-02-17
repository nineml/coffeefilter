package org.nineml.coffeefilter.trees;

public class CsvColumn {
    protected final String name;
    protected String datatype;
    protected String header;

    public CsvColumn(String name) {
        if (name == null) {
            throw new NullPointerException("Column name cannot be null");
        }
        this.name = name;
        this.datatype = null;
        this.header = name;
    }

    public String getName() {
        return name;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        if (header == null) {
            throw new NullPointerException("Column header cannot be null");
        }
        this.header = header;
    }

    @Override
    public String toString() {
        String value = name;
        if (datatype != null && !"string".equals(datatype)) {
            value += ": " + datatype;
        }
        if (!name.equals(header)) {
            value += " (" + header + ")";
        }
        return value;
    }
}
