<?xml version="1.0"?>
<xsl:stylesheet 
 version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:variable name="fn_list" select="//fn"/>

<!-- <xsl:variable name="fig_list" select="//fig"/> -->

<xsl:variable name="lists" select="//list"/>

<xsl:variable name="tbl_list" select="//table"/>

<xsl:variable name="box_list" select="//bx"/>

<xsl:template match="/">
<PAPER><xsl:apply-templates select="article"/></PAPER>
</xsl:template>

<xsl:template match="article">
<METADATA>
<FILENO>
<!-- Not exactly local filename, but looks like a unique identifier. CJR 131205 -->
<xsl:value-of select="id"/>
</FILENO>
<xsl:apply-templates select="pubfm"/>
</METADATA>
<xsl:apply-templates select="fm"/>
<xsl:apply-templates select="bdy"/>
<xsl:apply-templates select="bm"/>
</xsl:template>

<xsl:template match="pubfm">
<PAPERTYPE>article</PAPERTYPE>
<JOURNAL>
<NAME>
<xsl:apply-templates select="jtl"/>
</NAME>
<YEAR>
<!-- Copyright date not publication date CJR 141205 -->
<xsl:value-of select="cpg/cpy"/>
</YEAR>
<VOLUME>
<xsl:apply-templates select="vol"/>
</VOLUME>
<ISSUE>
<xsl:apply-templates select="iss"/>
</ISSUE>
<PAGES>
<xsl:apply-templates select="pp"/>
</PAGES>
</JOURNAL>
</xsl:template>

<!-- Template for PP element allows extension with CNT (page count) element. CJR 141205 -->
<xsl:template match="pp">
<xsl:value-of select="concat(spn,'-',epn)"/>
</xsl:template>

<xsl:template match="fm">
<TITLE>
<xsl:apply-templates select="atl"/>
</TITLE>
<xsl:apply-templates select="aug"/>
<xsl:apply-templates select="abs"/>
</xsl:template>

<xsl:template match="aug">
<AUTHORLIST>
<xsl:apply-templates select="au"/>
</AUTHORLIST>
</xsl:template>

<xsl:template match="au">
<AUTHOR>
<xsl:variable name="aid" select="orf/@rid"/>
<xsl:attribute name="ID">
<xsl:value-of select="position()"/>
</xsl:attribute>
<NAME>
<xsl:apply-templates select="fnm"/>
</NAME>
<PLACE>
<xsl:apply-templates select="../aff[oid/@id=$aid]"/>
</PLACE>
<SURNAME>
<xsl:apply-templates select="snm"/>
</SURNAME>
<INITIAL>
<xsl:apply-templates select="inits"/>
</INITIAL>
</AUTHOR>
</xsl:template>

<xsl:template match="abs">
<ABSTRACT><xsl:apply-templates select="./p/node()"/></ABSTRACT>
</xsl:template>

<xsl:template match="bdy[sec]">
<BODY>
<xsl:apply-templates select="fp"/>
<xsl:apply-templates select="sec"/>
</BODY>
</xsl:template>

<xsl:template match="bdy">
<BODY>
<xsl:apply-templates select="fp"/>
<DIV>
<HEADER/>
<xsl:apply-templates select="p"/>
</DIV>
</BODY>
</xsl:template>

<xsl:template match="fp">
<DIV>
<HEADER/>
<xsl:apply-templates/>
</DIV>
</xsl:template>

<xsl:template match="sec">
<DIV DEPTH="{@level}">
<xsl:apply-templates/>
</DIV>
</xsl:template>

<xsl:template match="sectitle">
<HEADER><xsl:apply-templates/></HEADER>
</xsl:template>

<xsl:template match="p">
<P>
<xsl:apply-templates/>
</P>
</xsl:template>

<xsl:template match="listr">
<xsl:variable name="list_id" select="@rid"/>
<xsl:apply-templates select="$lists[@id=$list_id]"/>
</xsl:template>

<xsl:template match="list[@type='bullet']">
<LIST TYPE="bullet" ID="{@id}"><xsl:apply-templates/></LIST>
</xsl:template>

<xsl:template match="list">
<LIST TYPE="number" ID="{@id}"><xsl:apply-templates/></LIST>
</xsl:template>

<xsl:template match="li">
<LI><xsl:apply-templates/></LI>
</xsl:template>

<xsl:template match="bm">
<xsl:apply-templates select="ack"/>
<xsl:apply-templates select="bibl"/>
<FOOTNOTELIST>
<xsl:for-each select="$fn_list">
	<FOOTNOTE ID="{@id}">
	  <xsl:attribute name="MARKER">
	    <xsl:value-of select="position()"/>
	  </xsl:attribute>
	  <xsl:apply-templates select="./p/node()"/>
	</FOOTNOTE>
</xsl:for-each>
</FOOTNOTELIST>
<FIGURELIST>
<xsl:for-each select="objects/fig">
	<FIGURE ID="{@id}" MIMETYPE="{@type}" SRC="{@file}">
	  <xsl:apply-templates select="title"/>
	  <xsl:apply-templates select="caption"/>
	</FIGURE>
</xsl:for-each>
</FIGURELIST>
<TABLELIST>
<xsl:for-each select="$tbl_list">
	<TABLE>
	  <xsl:apply-templates />
	</TABLE>
</xsl:for-each>
<xsl:for-each select="$box_list">
	<TABLE>
	 <TGROUP>
	    <THEAD><ROW><ENTRY>
		  <xsl:apply-templates select="bxtitle/node()"/>
		</ENTRY></ROW></THEAD>
	    <TBODY><ROW><ENTRY>
		  <xsl:apply-templates select="node()[not(self::bxtitle)]"/>
	</ENTRY></ROW></TBODY></TGROUP>
	</TABLE>
</xsl:for-each>
</TABLELIST>
</xsl:template>

<xsl:template match="tgroup">
    <TGROUP>
    <xsl:apply-templates />
    </TGROUP>
</xsl:template>

<xsl:template match="thead">
    <THEAD>
      <xsl:apply-templates/>
    </THEAD>
</xsl:template>

<xsl:template match="tfoot">
    <TFOOT>
      <xsl:apply-templates/>
    </TFOOT>
</xsl:template>
<xsl:template match="tbody">
    <TBODY>
      <xsl:apply-templates/>
    </TBODY>
</xsl:template>

<xsl:template match="row">
    <ROW>
    <xsl:apply-templates />
    </ROW>
</xsl:template>

<xsl:template match="entry">
    <ENTRY>
    <xsl:apply-templates />
    </ENTRY>
</xsl:template>

<xsl:template match="title">
<TITLE><xsl:apply-templates/></TITLE>
</xsl:template>

<xsl:template match="fn|bx" />

<xsl:template match="fnr">
    <XREF TYPE="FN-REF" ID="{@id}">
      <xsl:attribute name="MARKER">
	<xsl:apply-templates/>
      </xsl:attribute>
    </XREF>
</xsl:template>

<xsl:template match="bxr">
    <XREF TYPE="FN-REF" ID="{@id}">
	<xsl:apply-templates/>
    </XREF>
</xsl:template>

<xsl:template match="fdr">
    <XREF TYPE="FD-REF" ID="{@rid}">
	<xsl:apply-templates/>
    </XREF>
</xsl:template>

<xsl:template match="figr">
    <XREF TYPE="FIGURE-REF" ID="{@rid}">
	<xsl:apply-templates/>
    </XREF>
</xsl:template>

<xsl:template match="ack">
<ACKNOWLEDGMENTS><xsl:apply-templates select="./p/node()"/></ACKNOWLEDGMENTS>
</xsl:template>

<xsl:template match="caption[p]">
<CAPTION><xsl:apply-templates select="./p/node()"/></CAPTION>
</xsl:template>

<xsl:template match="bibl">
<REFERENCELIST><xsl:apply-templates/></REFERENCELIST>
</xsl:template>

<xsl:template match="bib">
<REFERENCE ID="{@id}">
<xsl:apply-templates select="reftxt"/>
</REFERENCE>
</xsl:template>

<xsl:template match="reftxt">
<xsl:apply-templates select="refau"/>
    <TITLE>
      <xsl:apply-templates select="atl"/>
    </TITLE>
      <JOURNAL>
	<NAME>
	  <xsl:apply-templates select="jtl"/>
	</NAME>
	<YEAR>
	  <xsl:apply-templates select="cd"/>
	</YEAR>
	<VOLUME>
	  <xsl:apply-templates select="vid"/>
	</VOLUME>
	<PAGES>
	  <xsl:value-of select="concat(ppf,'-',ppl)"/>
	</PAGES>
    </JOURNAL>
</xsl:template>

<xsl:template match="refau">
<AUTHORLIST>
<xsl:if test="count(snm)=count(fnm)">
	<xsl:for-each select="snm">
	  <xsl:variable name="sname_pos" select="position()"/>
	  <AUTHOR>
	    <NAME>
	      <xsl:value-of select="../fnm[position()=$sname_pos]"/>
	      <SURNAME>
		<xsl:value-of select="."/>
	      </SURNAME>
	    </NAME>
	  </AUTHOR>
	</xsl:for-each>
</xsl:if>
</AUTHORLIST>
</xsl:template>

  <xsl:template match="i">
    <IT>
      <xsl:apply-templates />
    </IT>
  </xsl:template>

  <xsl:template match="italic">
    <IT>
      <xsl:apply-templates />
    </IT>
  </xsl:template>

  <xsl:template match="b">
    <B>
      <xsl:apply-templates />
    </B>
  </xsl:template>

  <xsl:template match="bold">
    <B>
      <xsl:apply-templates />
    </B>
  </xsl:template>

  <xsl:template match="bi">
    <B><IT>
      <xsl:apply-templates />
    </IT></B>
  </xsl:template>

  <xsl:template match="super">
    <SP>
      <xsl:apply-templates />
    </SP>
  </xsl:template>

  <xsl:template match="sup">
    <SP>
      <xsl:apply-templates />
    </SP>
  </xsl:template>

  <xsl:template match="sub">
    <SB>
      <xsl:apply-templates />
    </SB>
  </xsl:template>

  <xsl:template match="inf">
    <SB>
      <xsl:apply-templates />
    </SB>
  </xsl:template>

  <xsl:template match="un">
    <UN>
      <xsl:apply-templates />
    </UN>
  </xsl:template>

  <xsl:template match="sc">
    <SCP>
      <xsl:apply-templates />
    </SCP>
  </xsl:template>

  <xsl:template match="f">
    <EQN>
      <xsl:apply-templates />
    </EQN>
  </xsl:template>

  <xsl:template match="fd">
    <EQN ID="{@id}">
      <xsl:apply-templates />
    </EQN>
  </xsl:template>

  <xsl:template match="mathimg">
    <XREF TYPE="IMG-REF" ID="{@entname}"/>
  </xsl:template>

  <xsl:template match="bibr">
    <REF TYPE="P" ID="{@rid}"/>
  </xsl:template>

  <xsl:template match="bibrinl">
    <REF TYPE="P" ID="{@rid}">
      <xsl:apply-templates/>
    </REF>
  </xsl:template>

  <xsl:template match="company">
    <XREF TYPE="COMPANY-REF" ID="{@id}">
      <xsl:apply-templates/>
    </XREF>
  </xsl:template>

  <xsl:template match="prod">
    <XREF TYPE="PRODUCT-REF" ID="{@companyid}">
      <xsl:apply-templates/>
    </XREF>
  </xsl:template>

  <xsl:template match="sir">
    <XREF TYPE="SUPPLEMENT-REF" ID="{@rid}">
      <xsl:apply-templates/>
    </XREF>
  </xsl:template>

  <xsl:template match="tablr">
    <XREF TYPE="TABLE-REF" ID="{@rid}">
      <xsl:apply-templates/>
    </XREF>
  </xsl:template>

  <xsl:template match="illusr">
    <XREF TYPE="ILLUSTRATION-REF" ID="{@rid}">
      <xsl:apply-templates/>
    </XREF>
  </xsl:template>

  <xsl:template match="xref">
    <XREF TYPE="EXTERNAL-REF" ID="{@extrefid}">
      <xsl:apply-templates/>
    </XREF>
  </xsl:template>

  <xsl:template match="cty">
    <CITY>
      <xsl:apply-templates/>
    </CITY>
  </xsl:template>

  <xsl:template match="cny">
    <COUNTRY>
      <xsl:apply-templates/>
    </COUNTRY>
  </xsl:template>

  <xsl:template match="url">
    <URL HREF="{.}">
      <xsl:apply-templates/>
    </URL>
  </xsl:template>

  <xsl:template match="comment()">
    <xsl:copy/>
  </xsl:template>


</xsl:stylesheet>

<!-- Keep this comment at the end of the file
Local variables:
mode: xml
sgml-omittag:nil
sgml-shorttag:nil
sgml-namecase-general:nil
sgml-general-insert-case:lower
sgml-minimize-attributes:nil
sgml-always-quote-attributes:t
sgml-indent-step:2
sgml-indent-data:t
sgml-parent-document:nil
sgml-exposed-tags:nil
sgml-local-catalogs:nil
sgml-local-ecat-files:nil
End:
-->
