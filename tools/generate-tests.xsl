<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:c="https://github.com/cmsmcq/ixml-tests"
                exclude-result-prefixes="#all"
                expand-text="yes"
                version="3.0">

<xsl:output method="text" encoding="utf-8"/>

<xsl:variable name="NL" select="'&#10;'"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/">
  <xsl:if test="not(c:test-catalog)">
    <xsl:message terminate="yes" select="'Expected a test catalog, got: ' || local-name(.)"/>
  </xsl:if>
  <xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="c:test-catalog|c:test-set|c:result">
  <xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="c:test-case">
  <xsl:variable name="set_name" select="ancestor::c:test-set[1]/@name/string()"/>
  <xsl:variable name="case_name" select="@name/string()"/>

  <xsl:variable name="set_id"
                select="translate($set_name, '- .', '___')"/>
  <xsl:variable name="case_id"
                select="translate($case_name, '- .', '___')"/>

  <xsl:text>    @Test{$NL}</xsl:text>
  <xsl:text>    public void test_{$set_id}_{$case_id}() {{{$NL}</xsl:text>
  <xsl:text>        Assert.assertTrue(pass("{$set_name}", "{$case_name}"));{$NL}</xsl:text>
  <xsl:text>    }}{$NL}{$NL}</xsl:text>
</xsl:template>

<xsl:template match="c:grammar-test">
  <xsl:variable name="set_name" select="ancestor::c:test-set[1]/@name/string()"/>

  <xsl:variable name="set_id"
                select="translate($set_name, '- .', '___')"/>

  <xsl:text>    @Test{$NL}</xsl:text>
  <xsl:text>    public void grammar_test_{$set_id}() {{{$NL}</xsl:text>
  <xsl:text>        Assert.assertTrue(pass("{$set_name}", null));{$NL}</xsl:text>
  <xsl:text>    }}{$NL}{$NL}</xsl:text>
</xsl:template>

<xsl:template match="c:test-set-ref">
  <xsl:choose>
    <xsl:when test="doc-available(resolve-uri(@href, base-uri(.)))">
      <xsl:apply-templates select="doc(resolve-uri(@href, base-uri(.)))"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>Missing test-set-ref: {resolve-uri(@href, base-uri(.))}{$NL}</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="c:description|c:created|c:modified
                     |c:assert-not-a-sentence|c:assert-not-a-grammar
                     |c:ixml-grammar-ref|c:vxml-grammar-ref"/>

<xsl:template match="*">
  <xsl:message select="'??? ' || local-name(.)"/>
</xsl:template>

</xsl:stylesheet>
