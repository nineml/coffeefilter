<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:t="https://github.com/invisibleXML/ixml/test-catalog"
                xmlns:r='https://nineml.org/ns/test-results'
                expand-text="yes"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:import href="utils.xsl"/>

<xsl:output method="html" html-version="5" encoding="utf-8" indent="no"/>

<xsl:param name="test-report.xml" select="'/tmp/x/xml-report.xml'"/>
<xsl:param name="test-report" select="doc($test-report.xml)"/>

<xsl:variable name="seropts"
              select="map{'method':'xml','indent':true()}"/>

<xsl:template match="/">
  <xsl:variable name="expanded" select="t:expanded-catalog(t:test-catalog)"/>
  <xsl:if test="not(t:unique-names($expanded))">
    <xsl:message terminate='yes' select="'Fix test names.'"/>
  </xsl:if>

  <xsl:variable name="merged" select="t:merge-catalog($expanded/t:test-catalog)"/>

  <xsl:result-document href="/tmp/merged.xml"
                       method="xml" indent="yes">
    <xsl:sequence select="$merged"/>
  </xsl:result-document>

  <xsl:for-each select="$test-report//r:test-case">
    <xsl:variable name="id" select="generate-id(.)"/>
    <xsl:if test="empty($merged//*[@xml:id=$id])">
      <xsl:choose>
        <xsl:when test="@name">
          <xsl:message>Report for unknown test {../@name}/{@name}</xsl:message>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message>Report for unknown grammar-test in {../@name}</xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:for-each>

  <html>
    <head>
      <title>CoffeeFilter Test Report</title>
      <link rel="stylesheet" href="catalog.css"/>
    </head>
    <body id="home">
      <div class="breadcrumbs">
        <img class="logotype" src="img/CoffeeFilter.svg"/>
      </div>
      <xsl:apply-templates select="$merged/t:test-catalog"/>
    </body>
  </html>
</xsl:template>

<xsl:template match="t:test-catalog|t:test-set">
  <xsl:param name="path" as="xs:string" select="''"/>
  <xsl:param name="preamble" select="()"/>

  <xsl:variable name="level"
                select="min((count(ancestor::*),2))+1"/>

  <xsl:variable name="intro">
    <xsl:choose>
      <xsl:when test="empty(parent::*)">
        <xsl:element name="h{$level}" namespace="http://www.w3.org/1999/xhtml">
          <xsl:attribute name="class" select="'title'"/>
          <xsl:text>CoffeeFilter Test Report</xsl:text>
        </xsl:element>

        <xsl:variable name="rdate" select="xs:dateTime($test-report/*/@date)"/>

        <p>
          <xsl:text>Report generated on </xsl:text>
          <xsl:text>{format-dateTime($rdate, "[D01] [MNn,*-3] [Y0001] at [H01]:[m01]")}</xsl:text>
          <xsl:text> with CoffeeFilter version </xsl:text>
          <xsl:text>{$test-report/*/@coffeefilter-version/string()}</xsl:text>
          <xsl:text> (using CoffeeGrinder version </xsl:text>
          <xsl:text>{$test-report/*/@coffeegrinder-version/string()})</xsl:text>
          <xsl:text> from a test suite dated </xsl:text>
          <xsl:sequence select="t:date(@release-date)"/>
          <xsl:variable name="dates" as="xs:string*">
            <xsl:for-each select="//@release-date[. castable as xs:date]
                                  | //@on[. castable as xs:date]">
              <xsl:sort select="." order="descending"/>
              <xsl:sequence select="."/>
            </xsl:for-each>
          </xsl:variable>
          <xsl:variable name="most-recent" select="$dates[1]"/>
          <xsl:if test="@release-date != $most-recent">
            <xsl:text> ({t:date($most-recent)})</xsl:text>
          </xsl:if>
          <xsl:text>.</xsl:text>
        </p>

        <xsl:apply-templates select="t:created|t:modified|t:description"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="h{$level}" namespace="http://www.w3.org/1999/xhtml">
          <xsl:attribute name="class" select="'title'"/>
          <xsl:sequence select="string(@name)"/>
        </xsl:element>
        <xsl:if test="@release-date">
          <xsl:element name="h{$level + 1}" namespace="http://www.w3.org/1999/xhtml">
            <xsl:attribute name="class" select="'reldate'"/>
            <xsl:sequence select="t:date(@release-date)"/>

            <xsl:variable name="dates" as="xs:string*">
              <xsl:for-each select=".//@release-date[. castable as xs:date]
                                    | .//@on[. castable as xs:date]">
                <xsl:sort select="." order="descending"/>
                <xsl:sequence select="."/>
              </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="most-recent" select="$dates[1]"/>
            <xsl:if test="@release-date != $most-recent">
              <xsl:text> ({t:date($most-recent)})</xsl:text>
            </xsl:if>
          </xsl:element>
        </xsl:if>
        <xsl:apply-templates select="t:created|t:modified|t:description"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <main>
    <xsl:if test="empty(parent::*)">
      <img class="logo" src="{t:relative-root($path)}img/CoffeeFilter.svg"/>
    </xsl:if>
    <xsl:sequence select="$preamble"/>
    <xsl:sequence select="$intro"/>

    <xsl:if test="empty(parent::*)">
      <xsl:variable name="total" select="count(.//r:result)"/>
      <xsl:variable name="pass" select="count(.//r:result[@state='PASS'])"/>
      <xsl:variable name="fail" select="count(.//r:result[@state='FAIL'])"/>
      <xsl:variable name="skip" select="count(.//r:result[@state='SKIP'])"/>
      <xsl:variable name="tests" select="count(.//r:report)"/>
      <xsl:variable name="anomaly" select="$total - ($pass+$skip+$fail)"/>

      <p>
        <xsl:text>{$tests} tests</xsl:text>
        <xsl:if test="$total != $tests">
          <xsl:text> ({$total} results)</xsl:text>
        </xsl:if>
        <xsl:text>; {$pass} passed, </xsl:text>
        <xsl:text>{$skip} skipped; {$fail} failed. </xsl:text>
        <xsl:if test="$anomaly != 0">
          <xsl:text>{$anomaly} anomalous result{if ($anomaly gt 1) then 's' else ()}.</xsl:text>
        </xsl:if>
      </p>
      <details class="note">
        <summary>A note about this report</summary>
        <p>The structure of this version of the report mimics the online test suite catalog,
        using one HTML page for each
        test case and navigation pages from the root of the catalog.</p>
        <xsl:if test="$total != $tests">
          <p>Some tests are run more than once, with different application-specific
          options. That’s why the number of results does not equal the number
          of tests.</p>
        </xsl:if>
      </details>
    </xsl:if>

    <xsl:if test="t:test-catalog|t:test-set|t:grammar-test|t:test-case">
      <div class="toc">
        <h4>Table of contents</h4>
        <ul>
          <xsl:apply-templates
              select="t:test-catalog|t:test-set|t:grammar-test|t:test-case"
              mode="t:toc">
            <xsl:with-param name="show-tests" select="exists(t:grammar-test|t:test-case)"/>
            <xsl:with-param name="path" select="$path"/>
          </xsl:apply-templates>
        </ul>
      </div>
    </xsl:if>

    <xsl:if test="empty(parent::*)">
      <div class="index">
        <h4>Test index</h4>
        <p>Tests marked <span class="check-ok">✔</span> have valid grammars and
        matching inputs; tests marked <span class="check-ok">✘</span> have valid
        grammars but non-matching inputs; tests marked
        <span class="check-fail">✘</span> have invalid grammars
        or should raise dynamic errors.</p>
        <ul>
          <xsl:for-each select=".//t:test-case|.//t:grammar-test">
            <xsl:sort select="t:name(.) || ' ' || t:name(..)"/>
            <li>
              <span>
                <xsl:attribute name="class">
                  <xsl:choose>
                    <xsl:when test="r:report/r:result[@state='FAIL']">
                      <xsl:sequence select="'test-result fail'"/>
                    </xsl:when>
                    <xsl:when test="r:report/r:result[@state='SKIP']">
                      <xsl:sequence select="'test-result skip'"/>
                    </xsl:when>
                    <xsl:when test="r:report/r:result[@state='PASS']">
                      <xsl:sequence select="'test-result pass'"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:sequence select="'test-result anomalous'"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>

                <a href="{t:filepath(.)}.html">{t:name(.)}</a>
                <xsl:text> ({t:name(..)})</xsl:text>
              </span>

              <xsl:choose>
                <xsl:when test=".//t:assert-not-a-grammar
                                |.//t:assert-dynamic-error">
                  <span class="check-fail"> ✘</span>
                </xsl:when>
                <xsl:when test=".//t:assert-not-a-sentence">
                  <span class="check-ok"> ✘</span>
                </xsl:when>
                <xsl:otherwise>
                  <span class="check-ok"> ✔</span>
                </xsl:otherwise>
              </xsl:choose>
            </li>
          </xsl:for-each>
        </ul>
      </div>
    </xsl:if>
  </main>

  <xsl:for-each select="t:test-catalog|t:test-set">
    <xsl:variable name="path" select="t:filepath(.) || '/index.html'"/>
    <xsl:result-document href="{$path}">
      <html>
        <head>
          <title>Invisible XML Test Suite Catalog</title>
          <link rel="stylesheet" href="{t:relative-root($path)}catalog.css"/>
        </head>
        <body>
          <div class="breadcrumbs">
            <xsl:variable name="apath" select="t:filepath(.) || '/index.html'"/>
            <xsl:for-each select="ancestor::*">
              <xsl:choose>
                <xsl:when test="position() = 1">
                  <a href="{t:relative-root($apath)}{t:filepath(.)}">
                    <img class="logotype"
                         src="{t:relative-root($apath)}/img/CoffeeFilter.svg"/>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text> / </xsl:text>
                  <a href="{t:relative-root($apath)}{t:filepath(.)}">{string(@name)}</a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
            <xsl:text> / {string(@name)}</xsl:text>
          </div>
          <xsl:apply-templates select=".">
            <xsl:with-param name="path" select="$path"/>
            <xsl:with-param name="preamble">
              <xsl:sequence select="$preamble"/>
              <xsl:sequence select="$intro/*[@class='title']"/>
              <details>
                <xsl:if test="$intro/*[@class='reldate']">
                  <summary>
                    <xsl:sequence select="$intro/*[@class='reldate']/node()"/>
                  </summary>
                </xsl:if>
                <xsl:sequence select="$intro/*[not(@class='title' or @class='reldate')]"/>
              </details>
            </xsl:with-param>
          </xsl:apply-templates>
        </body>
      </html>
    </xsl:result-document>
  </xsl:for-each>

  <xsl:variable name="context"
                select="*[not(self::t:created
                          or self::t:modified
                          or self::t:description
                          or self::t:test-case
                          or self::t:grammar-test
                          or self::r:*)]"/>

  <xsl:for-each select="t:grammar-test|t:test-case">
    <xsl:variable name="path" select="t:filepath(.) || '.html'"/>
    <xsl:result-document href="{$path}">
      <html>
        <head>
          <title>Invisible XML Test Suite Catalog</title>
          <link rel="stylesheet" href="{t:relative-root($path)}catalog.css"/>
        </head>
        <body>
          <div class="breadcrumbs">
            <xsl:for-each select="ancestor::*">
              <xsl:choose>
                <xsl:when test="position() = 1">
                  <a href="{t:relative-root($path)}{t:filepath(.)}">
                    <img class="logotype"
                         src="{t:relative-root($path)}/img/CoffeeFilter.svg"/>
                  </a>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text> / </xsl:text>
                  <a href="{t:relative-root($path)}{t:filepath(.)}">{string(@name)}</a>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
            <xsl:text> / {t:name(.)}</xsl:text>
          </div>
          <main>
            <xsl:sequence select="$preamble"/>
            <xsl:sequence select="$intro"/>
            <xsl:apply-templates select="$context"/>
            <h1>Test case: {t:name(.)}</h1>
            <details>
              <p>Repository URI: <code>…/tests/{substring-after(base-uri(.), '/tests/')}</code></p>
            </details>
            <xsl:apply-templates select="t:created|t:modified|t:description"/>
            <xsl:apply-templates select="*[not(self::t:created or self::t:modified)]"/>
          </main>
          <!--
          <aside>TC:<pre><code>
            <xsl:sequence select="serialize(.,map{'method':'xml','indent':true()})"/>
          </code></pre></aside>
          -->
        </body>
      </html>
    </xsl:result-document>
  </xsl:for-each>

</xsl:template>

<!-- ============================================================ -->

<xsl:template match="t:test-catalog|t:test-set" mode="t:toc">
  <!-- hack about a bit so that we can compute relative paths back
       to the root of the hierarchy so that the location on a web
       server doesn't matter. -->
  <xsl:param name="show-tests" as="xs:boolean"/>
  <xsl:param name="path" as="xs:string"/>

  <xsl:variable name="single"
                select="not(t:test-set) and count(t:test-case|t:grammar-test) = 1"/>

  <xsl:variable name="next-level" as="element()*">
    <xsl:choose>
      <xsl:when test="$show-tests and not($single)">
        <xsl:sequence select="t:test-catalog|t:test-set|t:grammar-test|t:test-case"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="t:test-catalog|t:test-set"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="anchor" as="element()">
    <xsl:choose>
      <xsl:when test="$single">
        <a href="{t:relative-root($path)}{t:filepath(t:test-case|t:grammar-test)}.html">
          <xsl:text>{string(@name)}</xsl:text>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <a href="{t:relative-root($path)}{t:filepath(.)}/index.html">
          <xsl:text>{string(@name)}</xsl:text>
        </a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="total" select="count(.//r:report)"/>
  <xsl:variable name="pass" select="count(.//r:report[r:result[@state='PASS']])"/>
  <xsl:variable name="fail" select="count(.//r:report[r:result[@state='FAIL']])"/>
  <xsl:variable name="skip" select="count(.//r:report[r:result[@state='SKIP']])"/>
  <xsl:variable name="class" as="xs:string+">
    <xsl:sequence select="'test-result'"/>
    <xsl:if test="$pass eq $total">pass</xsl:if>
    <xsl:if test="$fail gt 0">fail</xsl:if>
    <xsl:if test="$skip gt 0">skip</xsl:if>
    <xsl:if test="($pass+$skip+$fail) != $total">anomalous</xsl:if>
  </xsl:variable>

  <li>
    <span class="{string-join($class,' ')}">
      <xsl:sequence select="$anchor"/>
      
      <xsl:text> (</xsl:text>
      <xsl:choose>
        <xsl:when test="$pass eq $total">
          <span>{$total} pass</span>
        </xsl:when>
        <xsl:otherwise>
          <span>{$pass} pass/{$skip} skip/{$fail} fail</span>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:if test="($pass+$skip+$fail) != $total"
              >; {$total} test{if ($total ne 1) then "s" else ()}</xsl:if>
      <xsl:text>)</xsl:text>
    </span>

    <xsl:if test="$next-level">
      <ul>
        <xsl:apply-templates select="$next-level" mode="t:toc">
          <xsl:with-param name="show-tests" select="$show-tests"/>
          <xsl:with-param name="path" select="$path"/>
        </xsl:apply-templates>
      </ul>
    </xsl:if>
  </li>
</xsl:template>

<xsl:template match="t:grammar-test|t:test-case" mode="t:toc">
  <!-- hack about a bit so that we can compute relative paths back
       to the root of the hierarchy so that the location on a web
       server doesn't matter. -->
  <xsl:param name="path" as="xs:string"/>

  <li>
    <span>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="r:report/r:result[@state='FAIL']">
            <xsl:sequence select="'test-result fail'"/>
          </xsl:when>
          <xsl:when test="r:report/r:result[@state='SKIP']">
            <xsl:sequence select="'test-result skip'"/>
          </xsl:when>
          <xsl:when test="r:report/r:result[@state='PASS']">
            <xsl:sequence select="'test-result pass'"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="'test-result anomalous'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <a href="{t:relative-root($path)}{t:filepath(.)}.html">
        <xsl:text>{t:name(.)}</xsl:text>
      </a>
    </span>
  </li>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="r:report">
  <xsl:param name="result" select="()"/>
  <xsl:if test="exists($result)">
    <h1>Test report</h1>
    <xsl:apply-templates select="$result"/>
  </xsl:if>
</xsl:template>

<xsl:template match="r:result[@state='PASS']" priority="10">
  <p class="test-result pass">PASS</p>
</xsl:template>

<xsl:template match="r:result[@state='FAIL']" priority="10">
  <p class="test-result fail">FAIL</p>

  <xsl:variable name="expected-codes"
                select="distinct-values(.//*/@error-code ! tokenize(., '\s+'))"/>

  <xsl:choose>
    <xsl:when test="@error-code
                    and exists($expected-codes)
                    and not(@error-code = $expected-codes)">
      <p>
        <xsl:text>Incorrect error code; </xsl:text>
        <xsl:sequence select="t:error-code(@error-code)"/>
        <xsl:text> returned, but </xsl:text>
        <xsl:choose>
          <xsl:when test="count($expected-codes) = 1">
            <xsl:sequence select="t:error-code($expected-codes)"/>
            <xsl:text> expected.</xsl:text>
          </xsl:when>
          <xsl:when test="count($expected-codes) = 2">
            <xsl:sequence select="t:error-code($expected-codes[1])"/>
            <xsl:text> or </xsl:text>
            <xsl:sequence select="t:error-code($expected-codes[2])"/>
            <xsl:text> expected.</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="distinct-values($expected-codes)">
              <xsl:if test="position() gt 1">, </xsl:if>
              <xsl:if test="position() eq last()"> or </xsl:if>
              <xsl:sequence select="t:error-code(.)"/>
            </xsl:for-each>
            <xsl:text> expected.</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </p>
    </xsl:when>
    <xsl:otherwise>
      <p>Unexpected result:</p>
      <pre><code><xsl:sequence select="serialize(node(), map{'method':'xml','indent':true()})"/>
      </code></pre>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="r:result[@state='FAIL'][r:comparison]" priority="20">
  <p class="test-result fail">FAIL</p>
  <xsl:apply-templates select="r:comparison"/>
</xsl:template>

<xsl:template match="r:result[@state='SKIP']" priority="10">
  <p class="test-result skip">This test was skipped.</p>
</xsl:template>

<xsl:template match="r:result">
  <p>Unexpected result:</p>
  <pre><code><xsl:sequence select="serialize(node(), map{'method':'xml','indent':true()})"/>
  </code></pre>
</xsl:template>

<!-- ============================================================ -->

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

<xsl:template match="*">
  <xsl:message select="'No template for ' || local-name(.)"/>
  <div style="color:red;">
    <xsl:text>&lt;{node-name(.)}&gt;</xsl:text>
  </div>
  <xsl:apply-templates/>
  <div style="color:red;">
    <xsl:text>&lt;/{node-name(.)}&gt;</xsl:text>
  </div>
</xsl:template>

<xsl:template name="t:metadata">
  <div class="metadata">
    <xsl:apply-templates select="t:created"/>
    <xsl:variable name="mods"
                  select="reverse(sort(t:modified, (), function($mod) { $mod/@on }))"/>
    <xsl:apply-templates select="t:modified[1]">
      <xsl:with-param name="mods" select="$mods[position() gt 1]"/>
    </xsl:apply-templates>
  </div>
</xsl:template>

<xsl:template match="t:created">
  <p>Created {t:date(@on)} by {string(@by)}</p>
</xsl:template>

<xsl:template match="t:modified">
  <xsl:param name="mods" as="element(t:modified)*"/>

  <details>
    <summary>Updated {t:date(@on)} by {string(@by)}</summary>
    <xsl:if test="@change">
      <p>{string(@change)}</p>
    </xsl:if>
    <xsl:apply-templates select="$mods[1]">
      <xsl:with-param name="mods" select="$mods[position() gt 1]"/>
    </xsl:apply-templates>
  </details>
</xsl:template>

<xsl:template match="t:ixml-grammar-ref">
  <details>
    <summary>Invisible XML Grammar</summary>
    <div class="grammar">
      <xsl:try>
        <pre>
          <code>
            <xsl:sequence select="unparsed-text(resolve-uri(@href, base-uri(.)))"/>
          </code>
        </pre>
        <xsl:catch>
          <p><em>Document could not be loaded (perhaps invalid XML characters?)</em></p>
        </xsl:catch>
      </xsl:try>
    </div>
  </details>
</xsl:template>

<xsl:template match="t:vxml-grammar-ref">
  <xsl:variable name="xml" select="doc(resolve-uri(@href, base-uri(.)))"/>
  <details>
    <summary>Invisible XML Grammar</summary>
    <div class="grammar">
      <pre>
        <code>
          <xsl:sequence select="serialize($xml,
                                map { 'method': 'xml',
                                      'omit-xml-declaration': true(),
                                      'indent': true() })"/>
        </code>
      </pre>
    </div>
  </details>
</xsl:template>

<xsl:template match="t:ixml-grammar">
  <details>
    <summary>Invisible XML Grammar</summary>
    <div class="grammar">
      <pre>
        <code>
          <xsl:sequence select="string(.)"/>
        </code>
      </pre>
    </div>
  </details>
</xsl:template>

<xsl:template match="t:test-string|t:test-string-ref">
  <xsl:variable name="input" as="xs:string">
    <xsl:try>
      <xsl:sequence select="if (self::t:test-string)
                            then string(.)
                            else unparsed-text(resolve-uri(@href, base-uri(.)))"/>
      <xsl:catch>
        <p><em>Document could not be loaded (perhaps invalid XML characters?)</em></p>
      </xsl:catch>
    </xsl:try>
  </xsl:variable>
  <details>
    <summary>
      <xsl:text>Input string ({string-length($input)}</xsl:text>
      <xsl:text> character{if (string-length($input) ne 1) then 's' else ()})</xsl:text>
    </summary>
    <div class="test-string">
      <pre>
        <code>
          <xsl:sequence select="$input"/>
        </code>
      </pre>
    </div>
  </details>
</xsl:template>

<xsl:template match="t:assert-xml-ref">
  <xsl:variable name="xml" select="doc(resolve-uri(@href, base-uri(.)))"/>
  <div class="xml-result">
    <pre>
      <code>
        <xsl:sequence select="serialize($xml,
                              map { 'method': 'xml',
                                    'omit-xml-declaration': true(),
                                    'indent': true() })"/>
      </code>
    </pre>
  </div>
</xsl:template>

<xsl:template match="t:assert-xml">
  <div class="xml-result">
    <pre>
      <code>
        <xsl:sequence select="serialize(*,
                              map { 'method': 'xml',
                                    'omit-xml-declaration': true(),
                                    'indent': true() })"/>
      </code>
    </pre>
  </div>
</xsl:template>

<xsl:template match="t:assert-dynamic-error">
  <p class="dynamic-error">
    <xsl:text>Raises a dynamic error</xsl:text>
    <xsl:call-template name="t:error-codes"/>
    <xsl:text>.</xsl:text>
  </p>
</xsl:template>

<xsl:template match="t:assert-not-a-sentence">
  <p class="not-a-sentence">The input does not match the grammar.</p>
</xsl:template>

<xsl:template match="t:assert-not-a-grammar">
  <p class="not-a-grammar">
    <xsl:text>The grammar is invalid. Raises a static error</xsl:text>
    <xsl:call-template name="t:error-codes"/>
    <xsl:text>.</xsl:text>
  </p>
</xsl:template>

<xsl:template name="t:error-codes">
  <xsl:variable name="codes" select="tokenize(@error-code, '\s+')"/>
  <xsl:choose>
    <xsl:when test="empty($codes)"/>
    <xsl:when test="count($codes) eq 1">
      <xsl:if test="normalize-space($codes) != 'none'">
        <xsl:text>: </xsl:text>
        <xsl:sequence select="t:error-code($codes)"/>
      </xsl:if>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>: one of </xsl:text>
      <xsl:for-each select="$codes">
        <xsl:if test="position() gt 1">, </xsl:if>
        <xsl:sequence select="t:error-code(.)"/>
      </xsl:for-each>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="t:result">
  <details>
    <summary>Expected result{if (count(*) gt 1) then 's (one of)' else ()}</summary>
    <div>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="t:assert-not-a-grammar|t:assert-not-a-sentence|t:assert-dynamic-error">
            <xsl:sequence select="'result unsuccessful'"/>
          </xsl:when>
          <xsl:otherwise>result</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:apply-templates/>
    </div>
  </details>
  <xsl:apply-templates select="../r:report">
    <xsl:with-param name="result" select="../r:report/r:result[1]"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="t:app-info">
  <details class="app-info">
    <summary>Application specific extension</summary>
    <div>
      <xsl:apply-templates select="t:options"/>
      <xsl:if test="not(empty(*[not(self::t:options or self::r:*)]))">
        <div>
          <xsl:attribute name="class">
            <xsl:choose>
              <xsl:when test="t:assert-not-a-grammar|t:assert-not-a-sentence|t:assert-dynamic-error">
                <xsl:sequence select="'result unsuccessful'"/>
              </xsl:when>
              <xsl:otherwise>result</xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <h4>Expected result{if (count(*[not(self::t:options or self::r:*)]) gt 1) then 's (one of)' else ()}</h4>
          <xsl:apply-templates select="*[not(self::t:options or self::r:*)]"/>
        </div>
      </xsl:if>
    </div>
  </details>
  <xsl:variable name="pos" select="count(preceding-sibling::t:app-info)+2"/>
  <xsl:apply-templates select="../r:report">
    <xsl:with-param name="result" select="../r:report/r:result[$pos]"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="t:options">
  <div class="app-info-options">
    <ul>
      <xsl:for-each select="@*">
        <li>
          <code>
            <xsl:text>Q{{</xsl:text>
            <xsl:sequence select="namespace-uri(.)"/>
            <xsl:text>}}{local-name(.)}</xsl:text>
          </code>
          <xsl:text> = </xsl:text>
          <code>
            <xsl:sequence select="string(.)"/>
          </code>
        </li>
      </xsl:for-each>
    </ul>
  </div>
</xsl:template>

<xsl:template match="t:description"/>
<xsl:template match="t:description[empty(preceding-sibling::t:description)]"
              priority="10">
  <xsl:variable name="description">
      <xsl:apply-templates select="node(), following-sibling::t:description/node()"
                           mode="html"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="parent::t:test-catalog">
      <div class="description">
        <h4>Description</h4>
        <xsl:sequence select="$description"/>
      </div>
    </xsl:when>
    <xsl:otherwise>
      <details>
        <summary>Description</summary>
        <div class="description">
          <xsl:sequence select="$description"/>
        </div>
      </details>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ============================================================ -->

<xsl:template match="*" mode="html">
  <xsl:element name="{local-name(.)}" namespace="http://www.w3.org/1999/xhtml">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="html"/>
  </xsl:element>
</xsl:template>

<!-- ============================================================ -->

<xsl:function name="t:merge-catalog" as="document-node()"
              xmlns:tp="https://github.com/invisibleXML/ixml/test-catalog">
  <xsl:param name="catalog" as="element(t:test-catalog)"/>
  <xsl:document>
    <xsl:apply-templates select="$catalog" mode="tp:merge"/>
  </xsl:document>
</xsl:function>

<xsl:mode name="tp:merge" on-no-match="shallow-copy"
          xmlns:tp="https://github.com/invisibleXML/ixml/test-catalog"/>

<xsl:template match="t:test-case" mode="tp:merge"
              xmlns:tp="https://github.com/invisibleXML/ixml/test-catalog">
  <xsl:variable name="set" select="string(../@name)"/>
  <xsl:variable name="case" select="string(@name)"/>

  <xsl:variable name="result"
                select="$test-report//r:test-set[@name=$set]/r:test-case[@name=$case]"/>
  <xsl:choose>
    <xsl:when test="empty($result)">
      <xsl:message>No report for {$set}/{$case}.</xsl:message>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="tp:merge"/>
        <r:report>
          <xsl:attribute name="xml:id" select="generate-id($result)"/>
          <xsl:copy-of select="$result/node()"/>
        </r:report>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="t:grammar-test" mode="tp:merge"
              xmlns:tp="https://github.com/invisibleXML/ixml/test-catalog">
  <xsl:variable name="set" select="string(../@name)"/>

  <xsl:variable name="result"
                select="$test-report//r:test-set[@name=$set]/r:test-case[not(@name)]"/>
  <xsl:choose>
    <xsl:when test="empty($result)">
      <xsl:message>No report for grammar-test in {$set}.</xsl:message>
    </xsl:when>
    <xsl:when test="count($result) gt 1">
      <xsl:message>Multiple reports for grammar-test in {$set}.</xsl:message>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="tp:merge"/>
        <r:report>
          <xsl:attribute name="xml:id" select="generate-id($result)"/>
          <xsl:copy-of select="$result/node()"/>
        </r:report>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- ============================================================ -->


</xsl:stylesheet>
