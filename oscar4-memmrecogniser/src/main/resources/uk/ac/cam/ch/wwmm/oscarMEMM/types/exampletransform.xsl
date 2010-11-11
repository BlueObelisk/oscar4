<xsl:stylesheet 
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Example: removes ontology terms -->

<xsl:template match="ne[@type='ONT']">
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