<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ap="http://blackmesatech.com/2019/iXML/Aparecium"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:ixml="http://invisiblexml.org/NS"
                xmlns:r="https://nineml.org/ns/test-results"
                xmlns:t="https://github.com/cmsmcq/ixml-tests"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:output method="html" html-version="5" encoding="utf-8" indent="yes"/>

<xsl:param name="results.file" select="'../test-report.xml'"/>
<xsl:param name="results" select="doc($results.file)/*"/>

<xsl:mode on-no-match="shallow-copy"/>

<xsl:template match="/">
  <html>
    <head>
      <title>Test Report</title>
      <link rel="stylesheet" href="/css/report.css"/>
    </head>
    <body>
      <h1>Invisible XML Test Suite Report</h1>
      <dl>
        <dt>CoffeeFilter version:</dt>
        <dd><xsl:value-of select="$results/@coffeefilter-version"/></dd>
        <dt>CoffeeGrinder version:</dt>
        <dd><xsl:value-of select="$results/@coffeegrinder-version"/></dd>
        <dt>Date:</dt>
        <dd><xsl:value-of select="$results/@date"/></dd>
      </dl>

      <xsl:variable name="summary" as="element()*">
        <xsl:apply-templates mode="summary"/>
      </xsl:variable>

      <h2>
        <xsl:text>Tests: </xsl:text>
        <xsl:sequence select="count($summary/self::h:span)"/>
        <xsl:text>. Passing: </xsl:text>
        <xsl:sequence select="count($summary/self::h:span[.='PASS'])"/>
        <xsl:text>. </xsl:text>
        <xsl:if test="count($summary/self::h:span[.='FAIL']) gt 0">
          <xsl:text>Failing: </xsl:text>
          <xsl:sequence select="count($summary/self::h:span[.='FAIL'])"/>
          <xsl:text>. </xsl:text>
        </xsl:if>
        <xsl:if test="count($summary/self::h:span[.='SKIP']) gt 0">
          <xsl:text>Skipped: </xsl:text>
          <xsl:sequence select="count($summary/self::h:span[.='SKIP'])"/>
          <xsl:text>. </xsl:text>
        </xsl:if>
      </h2>

      <div class="title">Table of contents</div>
      <ul class="toc">
        <xsl:apply-templates mode="toc"/>
      </ul>
      <xsl:apply-templates/>
    </body>
  </html>
</xsl:template>

<xsl:template match="t:created|t:modified"/>

<xsl:template match="t:test-set-ref">
  <xsl:apply-templates select="doc(resolve-uri(@href, base-uri(.)))/*"/>
</xsl:template>

<xsl:template match="t:description">
  <div class="description">
    <xsl:apply-templates mode='html'/>
  </div>
</xsl:template>

<xsl:template match="t:ixml-grammar-ref">
  <div class="input ixml-grammar">
    <div class="title">Input grammar</div>
    <pre><xsl:sequence select="unparsed-text(resolve-uri(@href, base-uri(.)))"/></pre>
  </div>
</xsl:template>

<xsl:template match="t:ixml-grammar">
  <div class="input ixml-grammar">
    <div class="title">Input grammar</div>
    <pre><xsl:sequence select="."/></pre>
  </div>
</xsl:template>

<xsl:template match="t:vxml-grammar-ref">
  <div class="input vxml-grammar">
    <div>Input grammar</div>
    <pre><xsl:sequence select="unparsed-text(resolve-uri(@href, base-uri(.)))"/></pre>
  </div>
</xsl:template>

<xsl:template match="t:vxml-grammar">
  <div class="input vxml-grammar">
    <div>Input grammar</div>
    <pre>
      <xsl:sequence select="serialize(*, map {'method': 'xml', 'indent': true()})"/>
    </pre>
  </div>
</xsl:template>

<xsl:template match="t:test-string-ref">
  <div class="input ixml-input">
    <div>Input text</div>
    <pre><xsl:sequence select="unparsed-text(resolve-uri(@href, base-uri(.)))"/></pre>
  </div>
</xsl:template>

<xsl:template match="t:test-string">
  <div class="ixml-input">
    <p>Input text</p>
    <pre><xsl:sequence select="."/></pre>
  </div>
</xsl:template>

<xsl:function name="r:test-number" as="xs:integer">
  <xsl:param name="test" as="element()"/>
  <xsl:sequence select="count($test/preceding-sibling::t:grammar-test
                              | $test/preceding-sibling::t:test-case) + 1"/>
</xsl:function>

<xsl:function name="r:test-result-number" as="xs:integer">
  <xsl:param name="test" as="element()"/>
  <xsl:sequence select="count($test/preceding-sibling::t:result
                              | $test/preceding-sibling::t:app-info) + 1"/>
</xsl:function>

<xsl:function name="r:test-result">
  <xsl:param name="expected" as="element()"/>

  <xsl:variable name="test-number"
                select="r:test-number($expected/..)"/>

  <xsl:variable name="test-result-number"
                select="r:test-result-number($expected)"/>

  <xsl:variable name="set-name"
                select="$expected/ancestor::t:test-set/@name/string()"/>

  <xsl:variable name="tests"
                select="$results/r:test-set[@name=$set-name]/r:test-case"/>

  <xsl:variable name="result" 
                select="$tests[$test-number]"/>
  <xsl:sequence select="$result/r:result[$test-result-number]"/>
</xsl:function>

<xsl:template match="t:test-catalog">
  <div class="test-catalog" id="{generate-id(.)}">
    <div class="base-uri"><code><xsl:sequence select="base-uri(.)"/></code></div>
    <h1>
      <xsl:sequence select="@name/string()"/>
      <xsl:text>, </xsl:text>
      <xsl:sequence select="@release-date/string()"/>
    </h1>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="t:test-set">
  <div class="test-set" id="{generate-id(.)}">
    <h2>
      <xsl:sequence select="@name/string()"/>
    </h2>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="t:grammar-test">
  <div class="test-case grammar-test" id="{generate-id(.)}">
    <div class="title">Grammar test</div>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="t:test-case">
  <div class="test-case" id="{generate-id(.)}">
    <div class="title">
      <xsl:text>Test </xsl:text>
      <xsl:sequence select="@name/string()"/>
    </div>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="t:result">
  <xsl:variable name="actual" select="r:test-result(.)"/>

  <div class="result {$actual/@state}">
    <div class="expected">
      <pre class="debug">
        <xsl:sequence select="serialize(., map{'method':'xml','indent':true()})"/>
      </pre>
      <xsl:apply-templates/>
    </div>
    <div class="actual">
      <pre class="debug">
        <xsl:sequence select="serialize(
          $actual,
          map{'method':'xml', 'indent':true()})"/>
      </pre>
      <xsl:apply-templates select="$actual"/>
    </div>
  </div>
</xsl:template>

<xsl:template match="t:app-info">
  <xsl:variable name="actual" select="r:test-result(.)"/>

  <div class="result {$actual/@state}">
    <div class="title">With optional features</div>
    <div class="expected">
      <pre class="debug">
        <xsl:sequence select="serialize(., map{'method':'xml','indent':true()})"/>
      </pre>
      <div class="options">
        <xsl:apply-templates select="t:options"/>
      </div>
      <xsl:choose>
        <xsl:when test="count(t:assert-xml|t:assert-xml-ref) gt 1">
          <xsl:text>Expect one of:</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>Expect:</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:for-each select="t:assert-xml|t:assert-xml-ref">
        <xsl:if test="position() gt 1">
          <p><em>or</em></p>
        </xsl:if>
        <xsl:apply-templates select="."/>
      </xsl:for-each>
    </div>
    <div class="actual">
      <pre class="debug">
        <xsl:sequence select="serialize(
          $actual,
          map{'method':'xml', 'indent':true()})"/>
      </pre>
      <xsl:apply-templates select="$actual"/>
    </div>
  </div>
</xsl:template>

<xsl:template match="t:options">
  <xsl:if test="preceding-sibling::t:options">
    <p><em>or</em></p>
  </xsl:if>
  <ul>
    <xsl:for-each select="@*">
      <li>
        <xsl:if test="position() gt 1">
          <em>and </em>
        </xsl:if>
        <xsl:sequence select="node-name(.)"/>
        <xsl:text> = </xsl:text>
        <xsl:value-of select="."/>
      </li>
    </xsl:for-each>
  </ul>
</xsl:template>

<xsl:template match="t:assert-not-a-grammar">
  <div class="assert">
    <p>Expected: not an Invisible XML grammar.</p>
  </div>
</xsl:template>

<xsl:template match="t:assert-not-a-sentence">
  <div class="assert">
    <p>Expected: input does not match.</p>
  </div>
</xsl:template>

<xsl:template match="t:assert-xml">
  <div class="assert">
    <pre>
      <xsl:sequence select="serialize(*, map {'method': 'xml', 'indent': true()})"/>
    </pre>
  </div>
</xsl:template>

<xsl:template match="t:assert-xml-ref">
  <div class="assert">
    <pre>
      <xsl:sequence select="serialize(doc(resolve-uri(@href, base-uri(.)))/*,
                                      map {'method': 'xml', 'indent': true()})"/>
    </pre>
  </div>
</xsl:template>

<xsl:template match="t:*">
  <div>
    <xsl:sequence select="local-name(.)"/>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*">
  <div class="unmatched">
    <xsl:sequence select="'Q{' || namespace-uri(.) || '}' || local-name(.)"/>
  </div>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="html">
  <xsl:element namespace="http://www.w3.org/1999/xhtml" name="{local-name(.)}">
    <xsl:apply-templates mode="html"/>
  </xsl:element>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="r:result">
  <div class="test-result">
    <xsl:if test="@grammarParse ne '0' or @documentParse ne '0'">
      <div class="timing">
        <xsl:if test="@grammarParse ne '0'">
          <span>
            <xsl:text>Grammar parse: </xsl:text>
            <xsl:sequence select="@grammarParse/string()"/>
            <xsl:text>. </xsl:text>
          </span>
        </xsl:if>
        <xsl:if test="@documentParse ne '0'">
          <span>
            <xsl:text>Document parse: </xsl:text>
            <xsl:sequence select="@documentParse/string()"/>
            <xsl:text>. </xsl:text>
          </span>
        </xsl:if>
      </div>
    </xsl:if>
    <p>
      <xsl:text>Result: </xsl:text>
      <xsl:sequence select="@state/string()"/>
    </p>
    <xsl:apply-templates select="*[not(self::r:comparison)]"/>
    <div>
      <xsl:if test="count(r:comparison) gt 1">
        <xsl:attribute name="class" select="'comparisons'"/>
      </xsl:if>
      <xsl:apply-templates select="r:comparison"/>
    </div>
  </div>
</xsl:template>

<xsl:template match="r:comparison">
  <div class="comparison">
    <xsl:if test="count(../r:comparison) gt 1">
      <div class="name">
        <xsl:text>Alternative </xsl:text>
        <xsl:sequence select="count(preceding-sibling::r:comparison)+1"/>
      </div>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="../@state = 'PASS'">
        <xsl:apply-templates select="r:actual"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="r:expected"/>
        <xsl:apply-templates select="r:actual"/>
        <xsl:apply-templates select="r:message"/>
      </xsl:otherwise>
    </xsl:choose>
  </div>
</xsl:template>

<xsl:template match="r:expected">
  <div class="expected">
    <div>Expected:</div>
    <pre>
      <xsl:sequence select="serialize(*/*, map {'method': 'xml', 'indent': true()})"/>
    </pre>
  </div>
</xsl:template>

<xsl:template match="r:actual">
  <div class="actual">
    <div>Actual:</div>
    <pre>
      <xsl:sequence select="serialize(*/*, map {'method': 'xml', 'indent': true()})"/>
    </pre>
  </div>
</xsl:template>

<xsl:template match="r:message">
  <xsl:if test="not(normalize-space(.) = '')">
    <div class="message">
      <xsl:sequence select="normalize-space(.)"/>
    </div>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="t:test-catalog" mode="toc">
  <li>
    <a href="#{generate-id()}">
      <xsl:sequence select="@name/string()"/>
    </a>
    <xsl:variable name="subtoc" as="element()*">
      <xsl:apply-templates select="*" mode="toc"/>
    </xsl:variable>
    <xsl:if test="not(empty($subtoc))">
      <ul class="toc">
        <xsl:sequence select="$subtoc"/>
      </ul>
    </xsl:if>
  </li>
</xsl:template>

<xsl:template match="t:test-set" mode="toc">
  <xsl:variable name="subtoc" as="element()*">
    <xsl:apply-templates select="*" mode="toc"/>
  </xsl:variable>

  <li>
    <a href="#{generate-id(.)}">
      <xsl:if test="count($subtoc) = 1">
        <xsl:variable name="results" as="xs:string*">
          <xsl:for-each select=".//t:result|.//t:app-info">
            <xsl:variable name="actual" select="r:test-result(.)"/>
            <xsl:sequence select="$actual/@state/string()"/>
          </xsl:for-each>
        </xsl:variable>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$results = 'FAIL'">fail</xsl:when>
            <xsl:when test="$results = 'SKIP'">skip</xsl:when>
            <xsl:otherwise>pass</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </xsl:if>
      <xsl:sequence select="@name/string()"/>
    </a>

    <xsl:choose>
      <xsl:when test="empty($subtoc)"/>
      <xsl:when test="$subtoc/self::h:li">
        <ul class="toc">
          <xsl:sequence select="$subtoc"/>
        </ul>
      </xsl:when>
      <xsl:when test="count($subtoc) gt 1">
        <ul class="toc">
          <li>
            <xsl:for-each select="$subtoc">
              <xsl:if test="position() gt 1">, </xsl:if>
              <xsl:sequence select="."/>
            </xsl:for-each>
          </li>
        </ul>
      </xsl:when>
    </xsl:choose>
  </li>
</xsl:template>

<xsl:template match="t:grammar-test" mode="toc">
  <xsl:variable name="results" as="xs:string*">
    <xsl:for-each select=".//t:result|.//t:app-info">
      <xsl:variable name="actual" select="r:test-result(.)"/>
      <xsl:sequence select="$actual/@state/string()"/>
    </xsl:for-each>
  </xsl:variable>

  <span>
    <xsl:attribute name="class">
      <xsl:choose>
        <xsl:when test="$results = 'FAIL'">fail</xsl:when>
        <xsl:when test="$results = 'SKIP'">skip</xsl:when>
        <xsl:otherwise>pass</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <a href="#{generate-id(.)}">
      <xsl:text>grammar test</xsl:text>
    </a>
  </span>
</xsl:template>
    
<xsl:template match="t:test-case" mode="toc">
  <xsl:variable name="results" as="xs:string*">
    <xsl:for-each select=".//t:result|.//t:app-info">
      <xsl:variable name="actual" select="r:test-result(.)"/>
      <xsl:sequence select="$actual/@state/string()"/>
    </xsl:for-each>
  </xsl:variable>

  <span>
    <xsl:attribute name="class">
      <xsl:choose>
        <xsl:when test="$results = 'FAIL'">fail</xsl:when>
        <xsl:when test="$results = 'SKIP'">skip</xsl:when>
        <xsl:otherwise>pass</xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <a href="#{generate-id(.)}">
      <xsl:sequence select="@name/string()"/>
    </a>
  </span>
</xsl:template>

<xsl:template match="*" mode="toc">
  <xsl:apply-templates select="*" mode="toc"/>
</xsl:template>

<xsl:template match="t:test-set-ref" mode="toc">
  <xsl:apply-templates select="doc(resolve-uri(@href, base-uri(.)))/*"
                       mode="toc"/>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="t:result|t:app-info" mode="summary">
  <xsl:variable name="actual" select="r:test-result(.)"/>
  <span>
    <xsl:sequence select="$actual/@state/string()"/>
  </span>
</xsl:template>

<xsl:template match="*" mode="summary">
  <xsl:apply-templates select="*" mode="summary"/>
</xsl:template>

<xsl:template match="t:test-set-ref" mode="summary">
  <xsl:apply-templates select="doc(resolve-uri(@href, base-uri(.)))/*"
                       mode="summary"/>
</xsl:template>

</xsl:stylesheet>
