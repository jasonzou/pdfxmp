package org.xstudiosys.pdfxmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.util.PDFTextStripper;
import java.util.regex.*;

import java.text.SimpleDateFormat;
 
import java.util.Calendar;

public class PDFTextParser {
	// regex pattern from http://stackoverflow.com/questions/27910/finding-a-doi-in-a-document-or-page
	private static final String REGEXP_PLAINDOI = "\\b(10[.][0-9]{4,}(?:[.][0-9]+)*/(?:(?![\"&\\'])\\S)+)\\b";
	
	// Extract text from PDF Document
	static String pdftoText(String fileName) {
		PDFParser parser;
		String parsedText = null;;
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		File file = new File(fileName);
		if (!file.isFile()) {
			System.err.println("File " + fileName + " does not exist.");
			return null;
		}
		try {
			parser = new PDFParser(new FileInputStream(file));
		} catch (IOException e) {
			System.err.println("Unable to open PDF Parser. " + e.getMessage());
			return null;
		}
		try {
			parser.parse();
			cosDoc = parser.getDocument();
			pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument(cosDoc);
			pdfStripper.setStartPage(1);
			pdfStripper.setEndPage(5);
			parsedText = pdfStripper.getText(pdDoc);
		} catch (Exception e) {
			System.err.println("An exception occured in parsing the PDF Document."
							+ e.getMessage());
		} finally {
			try {
				if (cosDoc != null)
					cosDoc.close();
				if (pdDoc != null)
					pdDoc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return parsedText;
	}
	
	/* This function is a modified version of
	 * http://www.printmyfolders.com/extracting-phone-numbers.
	 * The pattern is used for DOI instead of phone numbers.
	 */ 
	static String pdfdoi(String fileName){
		 PDDocument pd;
		 String doi="";
		 try {
         //  PDF file from the phone numbers are extracted
         File input = new File(fileName);

         // StringBuilder to store the extracted text
         StringBuilder sb = new StringBuilder();
         pd = PDDocument.load(input);
         PDFTextStripper stripper = new PDFTextStripper();

         // Add text to the StringBuilder from the PDF
         sb.append(stripper.getText(pd));

         // Regex. For those who do not know. The Pattern refers to the format you are looking for.
         // In our example, we are looking for numbers with 10 digits with atleast one surrounding whitespaces
         // on both ends.
         Pattern p = Pattern.compile(REGEXP_PLAINDOI);

         // Matcher refers to the actual text where the pattern will be found
         Matcher m = p.matcher(sb);
			
			
         while (m.find()){
             // group() method refers to the next number that follows the pattern we have specified.
             if (doi==""){
					doi = m.group();
				 }
				 System.out.println(m.group());
				 
         }

         if (pd != null) {
             pd.close();
         }
		} catch (Exception e){
         e.printStackTrace();
      }
		return doi;
   }
	
	static void getPDFMeta(String fileName){
		try{
			PDDocument document = PDDocument.load(fileName);
			PDDocumentInformation info = document.getDocumentInformation();
			System.out.println("============= PDF Information ===============");
			System.out.println( "Page Count=" + document.getNumberOfPages() );
			System.out.println( "Title=" + info.getTitle() );
			System.out.println( "Author=" + info.getAuthor() );
			System.out.println( "Subject=" + info.getSubject() );
			System.out.println( "Keywords=" + info.getKeywords() );
			System.out.println( "Creator=" + info.getCreator() );
			System.out.println( "Producer=" + info.getProducer() );
			System.out.println( "Creation Date=" + formatDate(info.getCreationDate()) );
			System.out.println( "Modification Date=" + formatDate(info.getModificationDate()));
			System.out.println( "Trapped=" + info.getTrapped() );
			System.out.println("----------------------------------------------");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	static void getPDFXmpMeta(String fileName){
		try{
		PDDocument doc = PDDocument.load(fileName);
		PDDocumentCatalog catalog = doc.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();
		
		//to read the XML metadata
		InputStream xmlInputStream = metadata.createInputStream();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(xmlInputStream));
		System.out.println("=========== XMP Information ==========");
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("+++++++++++++++++++++++++++++++++++++++");
		//IoUtils.copy(xmlInputStream, System.out);
		
		//or to write new XML metadata
		//InputStream newXMPData = ...;
		//PDMetadata newMetadata = new PDMetadata(doc, newXMLData, false );
		//catalog.setMetadata( newMetadata );
	}
	   /**
     * This will format a date object.
     *
     * @param date The date to format.
     *
     * @return A string representation of the date.
     */
    static private String formatDate( Calendar date )
    {
        String retval = null;
        if( date != null )
        {
            SimpleDateFormat formatter = new SimpleDateFormat();
            retval = formatter.format( date.getTime() );
        }
 
        return retval;
    }

	public static void main(String args[]){
		System.out.println(pdftoText("./test.pdf"));
		String doi = pdfdoi("./test.pdf");
		System.out.println(doi);
		DOItoBibTeXFetcher doibib = new DOItoBibTeXFetcher();
		System.out.println(doibib.getEntryFromDOI(doi));
		getPDFMeta("./test.pdf");
		System.out.println("------------------------");
		getPDFXmpMeta("./test.pdf");
	}
	
	/*
		public String extractTitle() throws IOException {
		int TITLE_MIN_LENGTH = 2;
		String title = null;
		try {
			PDPage page = getPDDocument(getDocument()).getPageTree().getFirstPage();
			if (page.isPage()) {
				try {
					if(!page.cosGetContents().basicIterator().hasNext()) {
						page = page.getNextPage();
					}
									
					TreeMap<PdfTextEntity, StringBuilder> map = tryTextExtraction(page);
					Entry<PdfTextEntity, StringBuilder> entry = map.firstEntry();
					if(entry == null) {
						OCRTextExtractor handler = new OCRTextExtractor(file);
						//tryImageExtraction(page, handler);
						map = handler.getMap();
						entry = map.firstEntry();
						if(entry == null) {
							COSInfoDict info = getDocument().getInfoDict();
							title = info.getTitle();
						}
					}
					else {
						title = entry.getValue().toString().trim();
						while(title.trim().length() < TITLE_MIN_LENGTH || isNumber(title)) {
							entry = map.higherEntry(entry.getKey());
							if(entry == null) {
								break;
							}
							title = entry.getValue().toString().trim();
						}
						if(title.trim().length() < TITLE_MIN_LENGTH || isNumber(title)) {
							title = null;
						}
					}
					//System.out.println(map);
				}
				catch (Exception ex) {
					COSInfoDict info = getDocument().getInfoDict();
					if (info != null) {
						title = info.getTitle();
					}
				}
			}
		}
		finally {
			close();
		}
		if(title != null) {
			try {
				title = filter.filter(title);
			} catch (IOException e) {
			}
		}
		return title;
	}
	*/

}
