<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	>
<!--  xmlns="http://www.w3.org/TR/xhtml1/strict" -->
	<xsl:output method="html" omit-xml-declaration="yes" encoding="UTF-8" indent="yes"/>

	<xsl:template match="/document">	
	    <div class="document">
			<xsl:apply-templates/>
		</div>
	</xsl:template>

	<xsl:template match="comment">
		<xsl:comment>
            <xsl:text> </xsl:text>
	  		<xsl:apply-templates/>
            <xsl:text> </xsl:text>
	  	</xsl:comment>
	</xsl:template>
	
	<xsl:template match="title">
	   <xsl:if test="name(..)='document'">
		   <h1 class="mainTitle">
     		 <xsl:apply-templates/>
		   </h1>
	   </xsl:if>
	   
		<xsl:if test="not(name(..)='document')">
			<xsl:element name="h{count(ancestor::section) + 1}">
				<xsl:attribute name="class">title</xsl:attribute>
				<xsl:if test="@refid">
					<a class="toc-backref" href="#{@refid}" id="{../@id}"><xsl:apply-templates/></a>
				</xsl:if>
				<xsl:if test="not(@refid)">
					<xsl:apply-templates/>
				</xsl:if>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="subtitle">
		<xsl:element name="h2">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<!-- just eat it -->
	<xsl:template match="substitution_definition">
	</xsl:template>
	
	<xsl:template match="docinfo">
	  <table class="docinfo" frame="void" rules="none">
		<col class="docinfo-name" />
		<col class="docinfo-content" />
		<tbody valign="top">
			<xsl:apply-templates/>
		</tbody>
	  </table>
	</xsl:template>
	
	<xsl:template match="organization|address|contact|version|revision|status|date|copyright">
		<tr>
			<th class="docinfo-name">
				<xsl:value-of select="name(.)"/> : 
			</th>
			<td class="docinfo-content">
				 <xsl:apply-templates/>
			</td>
		</tr>
	</xsl:template>
			
	<xsl:template match="author">
		<xsl:if test="not(../../authors)">
				<tr>
					<th class="docinfo-name">
						<xsl:value-of select="name(.)"/> :
					</th>
					<td class="docinfo-content">
						<xsl:apply-templates/>
					</td>
				</tr>
    	</xsl:if>
		<xsl:if test="../../authors">
			<xsl:variable name="num" select="position()"/>
			<xsl:if test="$num=1">
				<tr>
					<th class="docinfo-name">
						<xsl:value-of select="authors"/>authors :
					</th>
					<td class="docinfo-content">
						<xsl:apply-templates/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="$num>1">
				<tr>
					<th>
						
					</th>
					<td class="docinfo-content">
						<xsl:apply-templates/>
					</td>
				</tr>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	
		
	<xsl:template match="transition">
	  <hr/>
	</xsl:template>

	<xsl:template match="section">
	  <section id="{@id}">
	    <xsl:apply-templates/>
	  </section>
	</xsl:template>

    <xsl:template match="list_item/paragraph[1] | definition_list_item/*/paragraph[1] | field/*/paragraph[1] | option/*/paragraph[1]">
            <!--XXX - Unclear how to handle multi-paragraph list items.
             | Certainly when they're single paragraphs, we don't want them
             | wrapped in a <P> tag.  This seems to work okay.
             +-->
            <xsl:apply-templates/>
    </xsl:template>

	<xsl:template match="paragraph">
	  <p><xsl:apply-templates/></p>
	</xsl:template>

	<xsl:template match="reference">
		<xsl:if test="@refid">
			<a href="{@refuri}#{@refid}" id="{@id}"><xsl:apply-templates/></a>
		</xsl:if>
		<xsl:if test="not(@refid)">
			<a href="{@refuri}" id="{@id}"><xsl:apply-templates/></a>
		</xsl:if>
	</xsl:template>

	<xsl:template match="emphasis">
	  <em><xsl:apply-templates/></em>
	</xsl:template>
	
	<xsl:template match="strong">
	  <b><xsl:apply-templates/></b>
	</xsl:template>
	
	<xsl:template match="literal">
		<code><xsl:value-of select="text()"/></code>
	</xsl:template>
	
	<xsl:template match="literal_block">
		<pre class="literal_block"><xsl:value-of select="text()"/></pre>
	</xsl:template>

	<xsl:template match="bullet_list">
		<ul><xsl:apply-templates/></ul>
	</xsl:template>

	<xsl:template match="enumerated_list">
		<ol>
		  	<xsl:choose>
			  	<xsl:when test="@enumtype='arabic'">
			  		<xsl:attribute name="type">1</xsl:attribute>
			    </xsl:when>
				<xsl:when test="@enumtype='loweralpha'">
					<xsl:attribute name="type">a</xsl:attribute>
				</xsl:when>
				<xsl:when test="@enumtype='upperalpha'">
					<xsl:attribute name="type">A</xsl:attribute>
				</xsl:when>
	            <xsl:when test="@enumtype='lowerroman'">
	                    <xsl:attribute name="type">i</xsl:attribute>
	            </xsl:when>
	            <xsl:when test="@enumtype='upperroman'">
	                    <xsl:attribute name="type">I</xsl:attribute>
	            </xsl:when>
            </xsl:choose>
	        <xsl:copy-of select="@start"/>
			<xsl:apply-templates/>
		</ol>
	</xsl:template>

	<xsl:template match="list_item">
		<li><xsl:apply-templates/></li>
	</xsl:template>

	<xsl:template match="field_list">
		<div class="field_list"><xsl:apply-templates/></div>
	</xsl:template>

	<xsl:template match="field">

		<xsl:if test="not(../../docinfo)">
			<div class="field"><xsl:apply-templates/></div>
		</xsl:if>
		
		<xsl:if test="../../docinfo">
			<tr>
				<th class="docinfo-name">
					<xsl:value-of select="field_name/text()"/> :
				</th>
				<td>
					<xsl:apply-templates select="field_body/*"/>
				</td>
			</tr>
		</xsl:if>
		
	</xsl:template>

	<xsl:template match="field_name">
		<span class="field_name"><xsl:apply-templates/></span>
	</xsl:template>

	<xsl:template match="field_body">
		<span class="field_body"><xsl:apply-templates/></span>
	</xsl:template>

	<xsl:template match="definition_list">
		<dl class="definition_list"><xsl:apply-templates/></dl>
	</xsl:template>

	<xsl:template match="definition_list_item">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="term">
		<dt class="term"><xsl:apply-templates/><xsl:call-template name="classifier"/></dt>
	</xsl:template>

	<xsl:template name="classifier">
	    <xsl:for-each select="../classifier">
			<span class="classifier"><xsl:apply-templates/></span>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="classifier">
		<!-- do nothing -->
	</xsl:template>

	<xsl:template match="definition">
		<dd class="definition"><xsl:apply-templates/></dd>
	</xsl:template>

	<xsl:template match="image">
                <xsl:choose>
                    <xsl:when test="(@target) and (@align)">
                        <div class="align-{@align}" align="{@align}">
                            <a href="{@target}">
                                <xsl:call-template name="img" />
                            </a>
			</div>
                    </xsl:when>
                    <xsl:when test="@target">
                        <a href="{@target}">
                            <xsl:call-template name="img" />
                        </a>
                    </xsl:when>
                    <xsl:when test="@align">
                        <div class="align-{@align}" align="{@align}">
                            <xsl:call-template name="img" />
			</div>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="img" />
                    </xsl:otherwise>
                </xsl:choose>
	</xsl:template>

	<xsl:template name="img">
            <xsl:element name="img">
                <xsl:attribute name="alt"><xsl:value-of select="@alt"/></xsl:attribute>
                <xsl:attribute name="src"><xsl:value-of select="@uri"/></xsl:attribute>
                <xsl:if test="@width"><xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute></xsl:if>
                <xsl:if test="@height"><xsl:attribute name="height"><xsl:value-of select="@height"/></xsl:attribute></xsl:if>
                <xsl:apply-templates/>
            </xsl:element>
        </xsl:template>

	
	<xsl:template match="footer">
		<hr/>
		<p class="footer"><xsl:apply-templates/></p>
	</xsl:template>
	
	<xsl:template match="header">
		<p class="header"><xsl:apply-templates/></p>
		<hr/>
	</xsl:template>
	
	<!--
	 | Table
	 +-->
	<xsl:template match="table">
		<table border="1">
			<colgroup>
				<xsl:apply-templates select="tgroup/colspec"/>
			</colgroup>
			<xsl:apply-templates select="./tgroup/thead|./tgroup/tbody"/>
		</table>
	</xsl:template>

	<xsl:template match="tgroup/colspec">
		<col width="{@colwidth}%"/>
	</xsl:template>

	<xsl:template match="row">
		<tr><xsl:apply-templates/></tr>
	</xsl:template>

	<xsl:template match="thead">
		<thead><xsl:apply-templates/></thead>
	</xsl:template>

	<xsl:template match="thead/row/entry">
		<th>
			<xsl:if test="@morecols"><xsl:attribute name="colspan"><xsl:value-of select="@morecols+1"/></xsl:attribute></xsl:if>
			<xsl:if test="@morerows"><xsl:attribute name="rowspan"><xsl:value-of select="@morerows+1"/></xsl:attribute></xsl:if>
			<xsl:apply-templates/>
		</th>
	</xsl:template>

	<xsl:template match="tbody">
		<tbody><xsl:apply-templates/></tbody>
	</xsl:template>

	<xsl:template match="tbody/row/entry">
		<td>
			<xsl:if test="@morecols"><xsl:attribute name="colspan"><xsl:value-of select="@morecols+1"/></xsl:attribute></xsl:if>
			<xsl:if test="@morerows"><xsl:attribute name="rowspan"><xsl:value-of select="@morerows+1"/></xsl:attribute></xsl:if>
			<xsl:apply-templates/>
		</td>
	</xsl:template>
	
	<xsl:template match="admonition">
		<div class="admonition">
			<div class="{@class}">
				<p class="{title}">
					<xsl:apply-templates select="./title"/>
				</p>
				<p class="body">
					<xsl:apply-templates select="child::*[position()>1]"/>
				</p>
			</div>
		</div>
	</xsl:template>
	
	<xsl:template match="attention|caution|danger|error|hint|important|note|tip|warning">
		<div class="{name(.)}">
			<p class="title"><xsl:value-of select="name(.)"/> :</p>
			<p class="body">
				<xsl:apply-templates/>
			</p>
		</div>
	</xsl:template>
	
	<xsl:template match="block_quote">
		
		<blockquote>
			<xsl:if test="./attribution">
				<p><xsl:apply-templates select="child::*[position()=1]"/></p>
				<p class="attribution">
					<xsl:apply-templates select="./attribution"/>
				</p>
			</xsl:if>
			<xsl:if test="not(./attribution)">
				<xsl:apply-templates select="child::*"/>
			</xsl:if>
		</blockquote>
		
	</xsl:template>
	
	<xsl:template match="doctest_block">
		<pre class="doctest_block">
			<xsl:apply-templates/>
		</pre>
	</xsl:template>
	
	<xsl:template match="line_block">
		<div class="line_block">
			<xsl:apply-templates/>
		</div>
	</xsl:template>
	
	<xsl:template match="line">
		<div class="line">
			<xsl:apply-templates/>
		</div>
	</xsl:template>
	
	<xsl:template match="sidebar">
		<div class="sidebar">
			<p class="title">
				<xsl:apply-templates select="./title"/>
			</p>
			<xsl:if test="./subtitle">
				<p class="subtitle">
					<xsl:apply-templates select="./subtitle"/>
				</p>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="./subtitle">
					<xsl:apply-templates select="child::*[position()>2]"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="child::*[position()>1]"/>
				</xsl:otherwise>
			</xsl:choose>
			
		</div>
	</xsl:template>
	
	<xsl:template match="topic">
		<div class="topic">
			<p class="title">
				<xsl:apply-templates select="./title"/>
			</p>
			<xsl:apply-templates select="child::*[position()>1]"/>
		</div>
	</xsl:template>
	
	<xsl:template match="option_list">
		<table class="option_list">
			<col class="option" />
			<col class="description" />
			<tbody valign="top">
				<xsl:apply-templates/>
			</tbody>
		</table>
	</xsl:template>
	
	<xsl:template match="option_list_item">
		<tr>
			<td class="option-group">
				<kbd>
					<xsl:apply-templates select="./option_group/option"/>
				</kbd>
			</td>
			<td>
				<xsl:apply-templates select="./description"/>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template match="option">
		<span class="option">
			<xsl:value-of select="option_string/text()"/>
			<xsl:value-of select="./option_argument/@delimiter"/>
			<xsl:apply-templates select="./option_argument"/>
		</span>
	</xsl:template>
	
	<xsl:template match="option_argument">
		<var>
			<xsl:value-of select="text()"/>,
		</var>
	</xsl:template>
		
	<xsl:template match="footnote">
		<table class="footnote" frame="void" id="{@id}" rules="none">
			<colgroup>
				<col class="label"/>
				<col/>
			</colgroup>
			<tbody valign="top">
				<tr>
					<td class="label">
						<a class="backref" href="#{@backrefs}" name="{id}">
							[<xsl:value-of select="label"/>]
						</a>
					</td>
					<td>
						<!--
	 					| <xsl:value-of select="child::*[position()>1]"/>
						 +-->
						<xsl:apply-templates select="child::*[position()>1]"/>
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>
	
	<xsl:template match="footnote_reference">
		<a class="footnote_reference" href="#{@refid}" id="{@id}" name="{@id}">
			[<xsl:value-of select="text()"/>]
		</a>
	</xsl:template>
	
</xsl:stylesheet>