<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method='text'/>

<!-- ================================================================== -->
<xsl:template match="file"><xsl:value-of select="tag/@swac_text"/><xsl:text>,</xsl:text><xsl:value-of select="tag/@swac_alphaidx"/><xsl:text>,TO TRANSLATE,</xsl:text><xsl:value-of select="@path" /><xsl:text>,</xsl:text><xsl:value-of select="tag/@swac_coll_section" /></xsl:template>

</xsl:stylesheet>


