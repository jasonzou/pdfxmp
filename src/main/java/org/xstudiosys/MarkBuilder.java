/*
 * Copyright 2009 xstudiosys.org (email: support@xstudiosys.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.xstudiosys.pdfxmp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
 
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;

 
import java.util.GregorianCalendar;

public class MarkBuilder {

	private static URI DOI_RESOLVER;
	static {
		try {
			DOI_RESOLVER = new URI("http://dx.doi.org/");
		} catch (URISyntaxException e) {
			/* Not possible. */
		}
	}
	
	public void onComplete(PDDocument document) {
		  try {
					 
		  PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentInformation info = document.getDocumentInformation();
		  
		  XMPMetadata metadata = new XMPMetadata();
		  
		  XMPSchemaPDF pdfSchema = metadata.addPDFSchema();
		  pdfSchema.setKeywords( info.getKeywords() );
		  pdfSchema.setProducer( info.getProducer() );
 
		  XMPSchemaBasic basicSchema = metadata.addBasicSchema();
		  basicSchema.setModifyDate( info.getModificationDate() );
		  basicSchema.setCreateDate( info.getCreationDate() );
		  basicSchema.setCreatorTool( info.getCreator() );
		  basicSchema.setMetadataDate( new GregorianCalendar() );
		  
        XMPSchemaDublinCore dcSchema = metadata.addDublinCoreSchema();
		  dcSchema.setTitle( info.getTitle() );
		  dcSchema.addCreator( "PDFBox" );
		  dcSchema.setDescription( info.getSubject() );
		  
        PDMetadata metadataStream = new PDMetadata(document);
		  metadataStream.importXMPMetadata( metadata );
		  catalog.setMetadata( metadataStream );
		  } catch (Exception e) {
             e.printStackTrace();
		  }
	}
	
	
	public static String getUrlForDoi(String doi) {
        return DOI_RESOLVER.resolve(doi).toString();
    }

}
