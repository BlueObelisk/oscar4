<?xml version="1.0"?>
<xsl:stylesheet 
 version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:variable name="fn_list" select="//footnote"/>

<xsl:variable name="tbl_list" select="//table-entry"/>

<xsl:variable name="sch_list" select="//scheme"/>

<xsl:variable name="fig_list" select="//figure|//plate"/>

<xsl:variable name="cht_list" select="//chart"/>

<xsl:variable name="ref_list" select="//citgroup"/>

<xsl:template match="/">
<PAPER><xsl:apply-templates select="article"/></PAPER>
</xsl:template>

<xsl:template match="article">
<METADATA>
<xsl:apply-templates select="art-admin"/>
<xsl:apply-templates select="published"/>
</METADATA>
<xsl:apply-templates select="art-front"/>
<BODY>
      <xsl:if test="art-body">
	<xsl:apply-templates select="art-body"/>
      </xsl:if>
</BODY>
<xsl:apply-templates select="art-back"/>
</xsl:template>

<xsl:template match="art-admin">
<FILENO>
<xsl:apply-templates select="ms-id"/>
</FILENO>
<xsl:if test="doi">
<DOI>
<xsl:apply-templates select="doi"/>
</DOI>
</xsl:if>
</xsl:template>

<xsl:template match="published">
<xsl:if test="@type='print'">
<PAPERTYPE>article</PAPERTYPE>
<JOURNAL>
<NAME>
<xsl:apply-templates select="journalref"/>
</NAME>
<YEAR>
<xsl:value-of select="pubfront/date/year"/>
</YEAR>
<VOLUME>
<xsl:apply-templates select="volumeref"/>
</VOLUME>
<ISSUE>
<xsl:apply-templates select="issueref"/>
</ISSUE>
<PAGES>
<xsl:value-of select="concat(pubfront/fpage,'-',pubfront/lpage)"/>
</PAGES>
</JOURNAL>
</xsl:if>
</xsl:template>

<xsl:template match="journalref[link]">
<xsl:apply-templates select="link"/>
</xsl:template>

<xsl:template match="journalref">
<xsl:apply-templates select="title"/>
</xsl:template>

<xsl:template match="issueref[link]">
<xsl:apply-templates select="link"/>
</xsl:template>

<xsl:template match="issueref">
<xsl:apply-templates select="issueno"/>
</xsl:template>

<xsl:template match="volumeref[link]">
<xsl:apply-templates select="link"/>
</xsl:template>

<xsl:template match="volumeref">
<xsl:apply-templates select="volumeno"/>
</xsl:template>

<xsl:template match="art-front">
<xsl:apply-templates select="titlegrp"/>
<xsl:apply-templates select="authgrp"/>
    <xsl:if test="abstract">
      <xsl:element name="ABSTRACT">
	<xsl:for-each select="abstract">
	    <xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
	  <xsl:apply-templates/>
	</xsl:for-each>
      </xsl:element>
    </xsl:if>
</xsl:template>

<xsl:template match="titlegrp">
<xsl:element name="TITLE">
<xsl:if test="title/@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="title/@nodeID"/></xsl:attribute></xsl:if>
<xsl:apply-templates select="title"/>
</xsl:element>
</xsl:template>

<xsl:template match="authgrp">
<AUTHORLIST>
<xsl:apply-templates select="author"/>
</AUTHORLIST>
</xsl:template>

<xsl:template match="author">
<AUTHOR>
<xsl:attribute name="ID">
<xsl:value-of select="position()"/>
</xsl:attribute>
<xsl:apply-templates select="person/persname"/>
</AUTHOR>
</xsl:template>

<xsl:template match="persname">
<NAME>
<xsl:apply-templates select="fname"/>
<SURNAME>
<xsl:value-of select="surname"/>
</SURNAME>
</NAME>
</xsl:template>

<xsl:template match="fname">
    <xsl:choose>
      <xsl:when test="self::node()[sup]">
	<xsl:apply-templates select="./sup/node()"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="art-body">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="section|news-section">
    <xsl:call-template name="sect2div">
      <xsl:with-param name="embed" select="1"/>
    </xsl:call-template>
</xsl:template>
<xsl:template match="subsect1|news-item">
    <xsl:call-template name="sect2div">
      <xsl:with-param name="embed" select="2"/>
    </xsl:call-template>
</xsl:template>
<xsl:template match="subsect2">
    <xsl:call-template name="sect2div">
      <xsl:with-param name="embed" select="3"/>
    </xsl:call-template>
</xsl:template>
<xsl:template match="subsect3">
    <xsl:call-template name="sect2div">
      <xsl:with-param name="embed" select="4"/>
    </xsl:call-template>
</xsl:template>
<xsl:template match="subsect4">
    <xsl:call-template name="sect2div">
      <xsl:with-param name="embed" select="5"/>
    </xsl:call-template>
</xsl:template>

<xsl:template name="sect2div">
    <xsl:param name="embed" select="1"/>
<DIV DEPTH="{$embed}">
<xsl:element name="HEADER">
<xsl:if test="title/@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="title/@nodeID"/></xsl:attribute></xsl:if>
<xsl:if test="@id">
	  <xsl:attribute name="ID">
	    <xsl:value-of select="@id"/>
	  </xsl:attribute>
</xsl:if>
<xsl:if test="no">
	  <xsl:attribute name="HEADER_MARKER">
	    <xsl:value-of select="no"/>
	  </xsl:attribute>
</xsl:if>
<xsl:apply-templates select="title"/>
<xsl:if test="citref">
<xsl:apply-templates select="citref"/>
</xsl:if>
</xsl:element>
<xsl:apply-templates select="node()[not(self::title or self::citref)]"/></DIV>
</xsl:template>

<xsl:template match="book-review">
    <REVIEW>
      <xsl:apply-templates/>
    </REVIEW>
</xsl:template>

<xsl:template match="title">
<xsl:apply-templates/>
</xsl:template>

<!-- <xsl:template match="equation[no]"> -->
<!--     <xsl:if test="eqntext"> -->
<!--       <XREF ID="{@id}" TYPE="THM-MARKER"/> -->
<!--     </xsl:if> -->
<!-- <IT><xsl:value-of select="st" /></IT> -->
<!-- </xsl:template> -->

<xsl:template match="equation[eqntext]">
	<xsl:apply-templates select="no"/>
	<xsl:apply-templates select="eqntext"/>
</xsl:template>

<xsl:template match="no">
	<NO>
    <xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
	<xsl:apply-templates/>
	</NO>
</xsl:template>

<xsl:template match="equation|ugraphic">
	<EQN/>
</xsl:template>

<xsl:template match="eqntext">
    <EQN>
    <xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
      <xsl:if test="local-name(..)='equation' and ../@id">
	<xsl:attribute name="ID">
	  <xsl:value-of select="../@id"/>
	</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </EQN>
</xsl:template>

  <xsl:template match="eqnref">
    <XREF TYPE="EQN" ID="{@idrefs}">
	<xsl:apply-templates />
    </XREF>
  </xsl:template>

<xsl:template match="no"/>

<!-- The following elements are not permitted to embed paragraphs. CJR 280306 -->
<xsl:template match="p[local-name(..)='abstract'
    or local-name(..)='ack'
    or local-name(..)='footnote'
    or local-name(..)='citext'
    or local-name(..)='eqntext'
    or local-name(..)='head'
    or local-name(..)='item']">
    <xsl:if test="position()!=1">
      <xsl:text>&#xA;</xsl:text>
      <SUBPAR/>
      <xsl:text>&#xA;</xsl:text>
    </xsl:if>
    <xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="p">
<xsl:element name="P">
<xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
<xsl:apply-templates/>
</xsl:element>
</xsl:template>

<xsl:template match="list">
<LIST TYPE="bullet"><xsl:apply-templates/></LIST>
</xsl:template>

<xsl:template match="item">
<LI><xsl:apply-templates/></LI>
</xsl:template>

<xsl:template match="head">
<HEAD><xsl:apply-templates/></HEAD>
</xsl:template>

<xsl:template match="deflist[parent::p]">
<DL><xsl:apply-templates/></DL>
</xsl:template>

<xsl:template match="deflist">
<P><DL>
<xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
<xsl:apply-templates/></DL></P>
</xsl:template>

<xsl:template match="term">
<DT>
<xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
<xsl:apply-templates/></DT>
</xsl:template>

<xsl:template match="dd">
<DD>
<xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
<xsl:apply-templates/></DD>
</xsl:template>

<xsl:template match="figure|plate">
	<FIGURE/>
</xsl:template>

<xsl:template match="table-entry|scheme|chart"/>

<xsl:template match="footnote">
	<FOOTNOTE/>
</xsl:template>
 
<xsl:template match="fnoteref[parent::title]">
    <XREF TYPE="FOOTNOTE_MARKER" ID="{@idrefs}"/>
 </xsl:template>
 
<xsl:template match="fnoteref[ancestor::abstract]">
    <XREF TYPE="FOOTNOTE_MARKER" ID="{@idrefs}"/>
 </xsl:template>

<xsl:template match="fnoteref">
<SUP TYPE="FOOTNOTE_MARKER" ID="{@idrefs}"/>	
</xsl:template>

<xsl:template match="art-back">
<xsl:variable name="cn_list" select="descendant::citgroup[citext]"/>
<xsl:apply-templates/>
    <xsl:if test="(count($cn_list)+count($fn_list)) > 0">
<FOOTNOTELIST>
      <xsl:for-each select="$fn_list">
	<FOOTNOTE>
	  <xsl:attribute name="ID">
	    <xsl:value-of select="@id"/>
	  </xsl:attribute>
	  <xsl:attribute name="MARKER">
	    <xsl:value-of select="position()"/>
	  </xsl:attribute>
	      <xsl:apply-templates/>
	</FOOTNOTE>
      </xsl:for-each>
      <xsl:for-each select="$cn_list">
	  <FOOTNOTE ID="{@id}">
	    <xsl:attribute name="MARKER">
	      <xsl:value-of select="position()"/>
	    </xsl:attribute>
	      <xsl:apply-templates select="citext/node()"/>
	  </FOOTNOTE>
      </xsl:for-each>
    </FOOTNOTELIST>
    </xsl:if>
    <xsl:if test="(count($fig_list)+count($sch_list)+count($cht_list)) > 0">
    <FIGURELIST>
      <xsl:for-each select="$fig_list">
	<FIGURE ID="{@id}" SRC="{@src}" SEQ="figure">
	  <xsl:element name="TITLE">
	  <xsl:if test="title/@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="title/@nodeID"/></xsl:attribute></xsl:if>
	  <xsl:apply-templates select="title"/>
	  </xsl:element>
	</FIGURE>
      </xsl:for-each>
      <xsl:for-each select="$sch_list">
	<FIGURE ID="{@id}" SRC="{@src}" SEQ="scheme">
	  <xsl:element name="TITLE">
	  <xsl:if test="title/@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="title/@nodeID"/></xsl:attribute></xsl:if>
	  <xsl:apply-templates select="title"/>
	  </xsl:element>
	  <!-- No internal figure representation as yet CJR 071205 -->
	</FIGURE>
      </xsl:for-each>
      <xsl:for-each select="$cht_list">
	<FIGURE ID="{@id}" SRC="{@src}" SEQ="chart">
	  <xsl:element name="TITLE">
	  <xsl:if test="title/@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="title/@nodeID"/></xsl:attribute></xsl:if>
	  <xsl:apply-templates select="title"/>
	  </xsl:element>
	  <!-- No internal figure representation as yet CJR 071205 -->
	</FIGURE>
      </xsl:for-each>
    </FIGURELIST>
    </xsl:if>
    <xsl:if test="count($tbl_list) > 0">
    <TABLELIST>
      <xsl:for-each select="$tbl_list">
	  <xsl:apply-templates select="table"/>
      </xsl:for-each>
    </TABLELIST>
    </xsl:if>
</xsl:template>

<xsl:template match="table">
  <TABLE>
    <xsl:if test="../@id">
      <xsl:attribute name="ID">
	<xsl:value-of select="../@id"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="../title">
      <TITLE>
	<xsl:if test="../title/@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="../title/@nodeID"/></xsl:attribute></xsl:if>
	<xsl:apply-templates select="../title"/>
      </TITLE>
    </xsl:if>
    <xsl:apply-templates />
  </TABLE>
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


<xsl:template match="ack">
<ACKNOWLEDGMENTS><xsl:apply-templates/></ACKNOWLEDGMENTS>
</xsl:template>

<xsl:template match="biblist">
<REFERENCELIST><xsl:apply-templates/></REFERENCELIST>
</xsl:template>

<xsl:template match="citgroup">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="citext"/>

<xsl:template match="journalcit">
<REFERENCE ID="{../@id}">
<!--       <xsl:if test="../citext"> -->
<!-- 	<XREF TYPE="FOOTNOTE_MARKER" ID="{../@id}"> -->
<!-- 	  <xsl:attribute name="MARKER"> -->
<!-- 	    <xsl:value-of select="count(preceding::citext)"/> -->
<!-- 	  </xsl:attribute> -->
<!-- 	</XREF> -->
<!--       </xsl:if> -->
      <AUTHORLIST>
	<xsl:apply-templates select="citauth"/>
      </AUTHORLIST>
      <xsl:element name="TITLE">
	<xsl:if test="arttitle/@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="arttitle/@nodeID"/></xsl:attribute></xsl:if>
	<xsl:apply-templates select="arttitle"/>
      </xsl:element>
      <JOURNAL>
	<NAME>
	  <xsl:apply-templates select="title"/>
	</NAME>
	<YEAR>
	  <xsl:apply-templates select="year"/>
	</YEAR>
	<xsl:if test="volumeno">
	<VOLUME>
	  <xsl:apply-templates select="volumeno"/>
	</VOLUME>
	</xsl:if>
	<xsl:if test="issueno">
	<ISSUE>
	  <xsl:apply-templates select="issueno"/>
	</ISSUE>
	</xsl:if>
	<xsl:if test="pages">
	<PAGES>
	  <xsl:apply-templates select="pages"/>
	</PAGES>
	</xsl:if>
      </JOURNAL>	
</REFERENCE>
</xsl:template>

<xsl:template match="citation">
<REFERENCE ID="{../@id}">
<!--       <xsl:if test="../citext"> -->
<!-- 	<XREF TYPE="FOOTNOTE_MARKER" ID="{../@id}"> -->
<!-- 	  <xsl:attribute name="MARKER"> -->
<!-- 	    <xsl:value-of select="count(preceding::citext)"/> -->
<!-- 	  </xsl:attribute> -->
<!-- 	</XREF> -->
<!--       </xsl:if> -->
      <AUTHORLIST>
	<xsl:apply-templates select="citauth"/>
      </AUTHORLIST>
      <xsl:for-each select="title">
	      <xsl:element name="TITLE">
		<xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
		<xsl:apply-templates/>
      </xsl:element>
      </xsl:for-each>
      <DATE>
	<xsl:apply-templates select="year"/>
      </DATE>	
</REFERENCE>
</xsl:template>

<xsl:template match="pages[lpage]">
<xsl:value-of select="concat(fpage,'-',lpage)"/>
</xsl:template>

<xsl:template match="citauth">
<AUTHOR>
    <NAME><xsl:apply-templates/></NAME>
</AUTHOR>
</xsl:template>

<xsl:template match="surname">
<SURNAME><xsl:apply-templates/></SURNAME>
</xsl:template>

  <xsl:template match="citref">
    <xsl:variable name="citid" select="@idrefs"/>
    <REF TYPE="P">
      <xsl:variable name="citstr" select="."/>
      <xsl:attribute name="text">
	<xsl:value-of select="$citstr"/>
      </xsl:attribute>
      <xsl:variable name="all_refs">
	<xsl:for-each select="$ref_list">
	  <xsl:variable name="citnum" select="substring-after(@id,'cit')"/>
	  <xsl:variable name="citsuff" select="substring-after($citstr,$citnum)"/>
	  <xsl:if test="contains($citid,@id) and
	    (starts-with($citsuff,',') or
	    string-length($citsuff) = 0)">
	    <xsl:call-template name="get-id"/>
	  </xsl:if>
	</xsl:for-each>
      </xsl:variable>
      <xsl:attribute name="ID">
	<xsl:value-of select="normalize-space($all_refs)"/>
      </xsl:attribute>
      <xsl:apply-templates />
    </REF>
  </xsl:template>

  <xsl:template name="get-id">
    <xsl:if test="journalcit|citation">
      <xsl:value-of select="@id"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:for-each select="citgroup">
      <xsl:call-template name="get-id"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="compoundref">
    <XREF TYPE="COMPOUND" ID="{@idrefs}">
	<xsl:apply-templates />
    </XREF>
  </xsl:template>

  <xsl:template match="compoundgrp"/>

  <xsl:template match="it">
    <IT>
      <xsl:apply-templates />
    </IT>
  </xsl:template>

  <xsl:template match="roman">
    <ROMAN>
      <xsl:apply-templates />
    </ROMAN>
  </xsl:template>

  <xsl:template match="bo">
    <B>
      <xsl:apply-templates />
    </B>
  </xsl:template>

  <xsl:template match="ul">
    <UL>
      <xsl:apply-templates />
    </UL>
  </xsl:template>

  <xsl:template match="bi">
    <B><xsl:element name="IT">
	<xsl:if test="@nodeID"><xsl:attribute name="nodeID"><xsl:value-of select="@nodeID"/></xsl:attribute></xsl:if>
      <xsl:apply-templates />
    </xsl:element></B>
  </xsl:template>

  <xsl:template match="sup">
    <SP>
      <xsl:apply-templates />
    </SP>
  </xsl:template>

  <xsl:template match="inf">
    <SB>
      <xsl:apply-templates />
    </SB>
  </xsl:template>

  <xsl:template match="figref">
  	<XREF TYPE="FIGURE-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="schemref">
  	<XREF TYPE="SCHEME-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="tableref">
  	<XREF TYPE="TABLE-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="eqnref">
  	<XREF TYPE="EQUATION-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="boxref">
  	<XREF TYPE="BOX-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="plateref">
  	<XREF TYPE="PLATE-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="chartref">
  	<XREF TYPE="CHART-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="textref">
  	<XREF TYPE="TEXT-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <xsl:template match="affref">
  	<XREF TYPE="AFF-REF" ID="{@idrefs}"><xsl:apply-templates /></XREF> 
  </xsl:template>

  <!-- Things found in RSC papers: do these better -->    
  <xsl:template match="stack">
  	<STACK>
  		<xsl:apply-templates/>
  	</STACK>
  </xsl:template>
  
  <xsl:template match="above">
  	<SP>
  		<xsl:apply-templates/>  	
  	</SP>
  </xsl:template>

  <xsl:template match="below">
  	<SB>
  		<xsl:apply-templates/>  	
  	</SB>  
  </xsl:template>

  <!-- small capitals, need support for this -->
  <xsl:template match="scp">
  	<SCP>
  		<xsl:apply-templates/>  	
  	</SCP>  
  </xsl:template>
  
  <!-- sansserif, need support for this -->
  <xsl:template match="sansserif">
  	<SANS>
  		<xsl:apply-templates/>
  	</SANS>
  </xsl:template>
  
  <xsl:template match="editor">
  	<EDITOR>
  		<xsl:apply-templates/>
  	</EDITOR>
  </xsl:template>
  
  <xsl:template match="url">
  	<URL>
  		<xsl:attribute name="HREF"><xsl:value-of select="@url"/></xsl:attribute>
  		<xsl:apply-templates/>
  	</URL>
  </xsl:template>

  <xsl:template match="email">
  	<URL>
  		<xsl:attribute name="HREF">mailto:<xsl:value-of select="."/></xsl:attribute>
  		<xsl:apply-templates/>
  	</URL>
  </xsl:template>
  
  <!-- Inserted as a proxy for processing instructions by Oscar -->
  <xsl:template match="pi-proxy">
  	<PI/>
  </xsl:template>

  <xsl:template match="processing-instruction()">
    <xsl:copy/>
  </xsl:template>

</xsl:stylesheet>

