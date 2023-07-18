<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:f="http://docbook.org/ns/docbook/functions"
                xmlns:fp="http://docbook.org/ns/docbook/functions/private"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:m="http://docbook.org/ns/docbook/modes"
                xmlns:mp="http://docbook.org/ns/docbook/modes/private"
                xmlns:t="http://docbook.org/ns/docbook/templates"
                xmlns:tp="http://docbook.org/ns/docbook/templates/private"
                xmlns:v="http://docbook.org/ns/docbook/variables"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="3.0">

<xsl:import href="../website/docbook.xsl"/>

<xsl:variable name="classlist" select="doc('../build/classlist.xml')/*"/>

<!-- ============================================================ -->

<xsl:template match="db:productname" mode="m:titlepage"
              expand-text="yes">
  <div class="versions">
    <p class="app">
      <xsl:text>CoffeeFilter </xsl:text>
      <a href="/test-report/">version {../db:productnumber/string()}</a>
    </p>
    <p class="lib">
      <xsl:text>(Based on CoffeeGrinder </xsl:text>
      <xsl:sequence select="../db:bibliomisc[@role='coffeegrinder']/string()"/>
      <xsl:text>.)</xsl:text>
    </p>
  </div>
</xsl:template>

<xsl:template match="*" mode="m:html-head-links">
  <xsl:next-match/>
  <link rel="shortcut icon" href="icon/CoffeeFilter.png"/>

  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <link rel="stylesheet" href="css/nineml.css"/>
  <link rel="stylesheet" href="css/coffeefilter.css"/>
</xsl:template>

<xsl:template match="db:classname" mode="m:docbook">
  <xsl:variable name="fq" select="string(.)"/>
  <xsl:variable name="parts" select="tokenize(., '\.')"/>
  <xsl:variable name="name" select="$parts[last()]"/>

  <xsl:variable name="class"
                select="if ($classlist/class[@fq=$fq])
                        then $classlist/class[@fq=$fq]
                        else $classlist/class[@name=$name]"/>

  <xsl:choose>
    <xsl:when test="count($class) = 1">
      <xsl:variable name="link" select="'/apidoc/'||$class/@path"/>
      <a href="{$link}">
        <xsl:call-template name="t:inline">
          <xsl:with-param name="namemap" select="'code'"/>
          <xsl:with-param name="content">
            <xsl:sequence select="$name"/>
          </xsl:with-param>
        </xsl:call-template>
      </a>
    </xsl:when>
    <xsl:when test="count($class) gt 1">
      <xsl:message select="'Ambiguous:', $fq"/>
      <xsl:next-match/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:next-match/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
