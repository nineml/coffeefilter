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

<xsl:param name="user-css-links"
           select="'css/nineml.css css/coffeefilter.css'"/>

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
</xsl:template>

</xsl:stylesheet>
