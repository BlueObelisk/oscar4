<xsl:stylesheet 
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="PubmedArticle">
	<PAPER>
		<METADATA>
			<FILENO><xsl:value-of select="MedlineCitation/PMID"/></FILENO>
			<xsl:for-each select="PubmedData/ArticleIdList/ArticleId[@IdType='doi']">
				<DOI><xsl:value-of select="."/></DOI>
			</xsl:for-each>
			<xsl:for-each select="MedlineCitation/Article/PublicationTypeList/PublicationType">
				<PAPERTYPE><xsl:value-of select="."/></PAPERTYPE>
			</xsl:for-each>
			<JOURNAL>
				<NAME><xsl:value-of select="MedlineCitation/Article/Journal/Title"/></NAME>
				<YEAR><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/PubDate/Year"/></YEAR>
				<VOLUME><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Volume"/></VOLUME>
				<ISSUE><xsl:value-of select="MedlineCitation/Article/Journal/JournalIssue/Issue"/></ISSUE>
				<PAGES><xsl:value-of select="MedlineCitation/Article/Pagination/MedlinePgn"/></PAGES>
			</JOURNAL>
			<xsl:for-each select="MedlineCitation/MeshHeadingList">
			<CLASSIFICATION>
				<xsl:for-each select="MeshHeading/DescriptorName">
					<KEYWORD><xsl:value-of select="."/></KEYWORD>
				</xsl:for-each>
			</CLASSIFICATION>
			</xsl:for-each>			
		</METADATA>
		<CURRENT_AUTHORLIST>
			<xsl:for-each select="MedlineCitation/Article/AuthorList/Author">
				<CURRENT_AUTHOR>
					<NAME><xsl:value-of select="ForeName"/><xsl:text> </xsl:text><SURNAME><xsl:value-of select="LastName"/></SURNAME></NAME>					
				</CURRENT_AUTHOR>
			</xsl:for-each>
		</CURRENT_AUTHORLIST>
		<CURRENT_TITLE><xsl:value-of select="MedlineCitation/Article/ArticleTitle"/></CURRENT_TITLE>
		<ABSTRACT><xsl:value-of select="MedlineCitation/Article/Abstract/AbstractText"/></ABSTRACT>
	</PAPER>
</xsl:template>

 </xsl:stylesheet>