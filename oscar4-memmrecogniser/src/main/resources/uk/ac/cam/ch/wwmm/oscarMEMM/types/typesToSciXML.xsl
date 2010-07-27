<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" />
  <xsl:template match="/">
<PAPER><TITLE>Named Entity Types</TITLE><BODY><DIV>
<xsl:for-each select="//type">
<P>
<ne><xsl:attribute name="type"><xsl:value-of select="@name"/></xsl:attribute><xsl:value-of select="@name"/></ne>
<xsl:text> </xsl:text><xsl:value-of select="desc"/>
</P>
</xsl:for-each>
</DIV></BODY></PAPER>
</xsl:template>
</xsl:stylesheet>