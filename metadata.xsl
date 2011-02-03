<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
 <xsl:output method="xml" indent="yes"/>

	<xsl:template match="rom">
		<rom>
			<xsl:attribute name="id"><xsl:value-of select="romid/xmlid"/></xsl:attribute>			
			<xsl:if test="romid/internalidstring">
				<xsl:attribute name="internalidstring"><xsl:value-of select="romid/internalidstring"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/internalidaddress">
				<xsl:attribute name="internalidaddress"><xsl:value-of select="romid/internalidaddress"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/year">
				<xsl:attribute name="year"><xsl:value-of select="romid/year"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/market">
				<xsl:attribute name="market"><xsl:value-of select="romid/market"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/make">
				<xsl:attribute name="make"><xsl:value-of select="romid/make"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/model">
				<xsl:attribute name="model"><xsl:value-of select="romid/model"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/submodel">
				<xsl:attribute name="submodel"><xsl:value-of select="romid/submodel"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/transmission">
				<xsl:attribute name="transmission"><xsl:value-of select="romid/transmission"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/memmodel">
				<xsl:attribute name="memmodel"><xsl:value-of select="romid/memmodel"/></xsl:attribute>
			</xsl:if>			
			<xsl:if test="romid/flashmethod">
				<xsl:attribute name="flashmethod"><xsl:value-of select="romid/flashmethod"/></xsl:attribute>
			</xsl:if>
							
			<xsl:for-each select="scaling">
				<scaling>
					<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>					
					<xsl:if test="@units">
						<xsl:attribute name="units"><xsl:value-of select="@units"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@toexpr">
						<xsl:attribute name="toexpr"><xsl:value-of select="@toexpr"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@frexpr">
						<xsl:attribute name="frexpr"><xsl:value-of select="@frexpr"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@format">
						<xsl:attribute name="format"><xsl:value-of select="@format"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@min">
						<xsl:attribute name="min"><xsl:value-of select="@min"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@max">
						<xsl:attribute name="max"><xsl:value-of select="@max"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@inc">
						<xsl:attribute name="inc"><xsl:value-of select="@inc"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@storagetype">
						<xsl:attribute name="storagetype"><xsl:value-of select="@storagetype"/></xsl:attribute>
					</xsl:if>
					<xsl:if test="@endian">
						<xsl:attribute name="endian"><xsl:value-of select="@endian"/></xsl:attribute>
					</xsl:if>
			
					<xsl:for-each select="data">
						<data>
							<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
							<xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
						</data>
					</xsl:for-each>
					
				</scaling>
			</xsl:for-each>
							
			<xsl:for-each select="table">
				<table>
					<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>					
					<xsl:if test="@type">
						<xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute>
					</xsl:if>					
					<xsl:if test="@address">
						<xsl:attribute name="address"><xsl:value-of select="@address"/></xsl:attribute>
					</xsl:if>					
					<xsl:if test="@category">
						<xsl:attribute name="category"><xsl:value-of select="@category"/></xsl:attribute>
					</xsl:if>							
					<xsl:if test="@scaling">
						<xsl:attribute name="scaling"><xsl:value-of select="@scaling"/></xsl:attribute>
					</xsl:if>					
					<xsl:if test="@elements">
						<xsl:attribute name="elements"><xsl:value-of select="@elements"/></xsl:attribute>
					</xsl:if>	
					<xsl:if test="description">
						<xsl:attribute name="description"><xsl:value-of select="description"/></xsl:attribute>
					</xsl:if>	
	
					<xsl:for-each select="table">
						<table>	
						
							<xsl:choose>	
								<xsl:when test="@type='X Axis'">
									<xsl:attribute name="axis">x</xsl:attribute>	
									<xsl:attribute name="type">axis</xsl:attribute>
									<xsl:attribute name="static">false</xsl:attribute>
									<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
								</xsl:when>
								<xsl:when test="@type='Static X Axis'">
									<xsl:attribute name="axis">x</xsl:attribute>	
									<xsl:attribute name="type">axis</xsl:attribute>
									<xsl:attribute name="static">true</xsl:attribute>
									<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
								</xsl:when>
								<xsl:when test="@type='Y Axis'">
									<xsl:attribute name="axis">y</xsl:attribute>	
									<xsl:attribute name="type">axis</xsl:attribute>
										<xsl:attribute name="static">false</xsl:attribute>
									<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
								</xsl:when>
								<xsl:when test="@type='Static Y Axis'">
									<xsl:attribute name="axis">y</xsl:attribute>	
									<xsl:attribute name="type">axis</xsl:attribute>
									<xsl:attribute name="static">true</xsl:attribute>
									<xsl:attribute name="id"><xsl:value-of select="@name"/></xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<xsl:when test="@name='X'">
											<xsl:attribute name="axis">x</xsl:attribute>
										</xsl:when>
										<xsl:when test="@name='Y'">
											<xsl:attribute name="axis">y</xsl:attribute>
										</xsl:when>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>	
							
							<xsl:if test="@address">
								<xsl:attribute name="address"><xsl:value-of select="@address"/></xsl:attribute>
							</xsl:if>							
							<xsl:if test="@category">
								<xsl:attribute name="category"><xsl:value-of select="@category"/></xsl:attribute>
							</xsl:if>							
							<xsl:if test="@scaling">
								<xsl:attribute name="scaling"><xsl:value-of select="@scaling"/></xsl:attribute>
							</xsl:if>	
							<xsl:if test="@elements">
								<xsl:attribute name="elements"><xsl:value-of select="@elements"/></xsl:attribute>
							</xsl:if>								
							<xsl:if test="description">
								<xsl:attribute name="description"><xsl:value-of select="description"/></xsl:attribute>
							</xsl:if>	
							
							<xsl:for-each select="data">
								<data>
									<xsl:value-of select="/rom/table/table/data"/>
								</data>
							</xsl:for-each>							
						</table>
					</xsl:for-each>					
				</table>
			</xsl:for-each>			
		</rom>
	</xsl:template>	
</xsl:stylesheet> 