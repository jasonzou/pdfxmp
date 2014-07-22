package org.xstudiosys.pdfxmp;

import org.apache.jempbox.xmp.XMPSchema;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;

public class SchemaSet {
    
    private XMPSchema dc = new XMPSchemaDublinCore(null);
    private XMPSchema pdfx = new XMPSchemaPDF(null);
    
    public XMPSchema getDc() {
        return dc;
    }
       
    public XMPSchema getPdfx() {
    	return pdfx;
    }

}
