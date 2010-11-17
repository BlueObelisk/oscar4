<xsl:stylesheet 
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Example: removes "point" elements. Not useful, but does test the 
XSL-on-data mechanisms -->

<xsl:template match="point">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*">
	<xsl:copy>
		<xsl:for-each select="@*">
			<xsl:copy/>
		</xsl:for-each>
		<xsl:apply-templates/>
	</xsl:copy>
</xsl:template>

 </xsl:stylesheet>