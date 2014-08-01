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
import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.io.*;
import java.util.*;


import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.xstudiosys.pdfxmp.PDFTextParser;
import org.xstudiosys.pdfxmp.XMPUtil;
import org.xstudiosys.pdfxmp.DOItoBibTeXFetcher;

import net.sf.jabref.*;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.export.LatexFieldFormatter;


import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Formatter;

import java.util.Collection;

public class Main{
	private String doi;
	
	
	
	public static void printUsage() {
		System.out.println("Usage: pdfxmp [options] pdf_file\n" +
								 "Options: \n" +
								 " [{-a, --auto-conv}]\n" +
								 " [{-b, --bibtex}]\n" +
								 " [{-c, --conv} bib]\n" +
								 " [{-d, --doi}]\n" +
								 " [{-h, --hash]\n" +
								 " [{-i, --info}]\n" +
								 " [{-x, --xmp}]\n" +
								 " [{-?, --usage}]\n" +							 
								 " ");
		/*
		 * pdfxmp -a pdffile => search for doi, if found, convert into bibtex, save xmp into pdffile
		 * pdfxmp -b pdffile => print bibtex from xmp
		 * pdfxmp -c key bib pdffile => convert bib[key] save as xmp into a pdf
		 * pdfxmp -d pdffile => search for doi
		 * pdfxmp -h pdffile => print pdfhash 
		 * pdfxmp -i pdffile => print pdf info and xmp 
		 * pdfxmp -x pdffile => print xmp 
		 * pdfxmp -? => print usage 	 
		 */
	}

	public static void main(String[] args) {
		new Main(args);
	}
	
	
	public Main(String[] args){
		if (args.length == 0) {
			printUsage();
			System.exit(2);
		}
		
		CmdLineParser parser = new CmdLineParser();
		Option<Boolean> autoOp = parser.addBooleanOption('a', "auto-conv");
		Option<Boolean> bibOp = parser.addBooleanOption('b', "bibtex");
		Option<Boolean> doiOp = parser.addBooleanOption('d', "doi");
		Option<Boolean> hashOp = parser.addBooleanOption('h', "hash");
		Option<Boolean> infoOp = parser.addBooleanOption('i', "info");
		Option<Boolean> xmpOp = parser.addBooleanOption('x', "xmp");
		Option<Boolean> usageOp = parser.addBooleanOption('?', "usage");
		
		Option<String> convOp = parser.addStringOption('c', "conv");
		
		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}
		
		Boolean autoOpValue = parser.getOptionValue(autoOp, Boolean.FALSE);
		Boolean bibOpValue = parser.getOptionValue(bibOp, Boolean.FALSE);
		Boolean doiOpValue = parser.getOptionValue(doiOp, Boolean.FALSE);
		Boolean hashOpValue = parser.getOptionValue(hashOp, Boolean.FALSE);
		Boolean infoOpValue = parser.getOptionValue(infoOp, Boolean.FALSE);
		Boolean xmpOpValue = parser.getOptionValue(xmpOp, Boolean.FALSE);
		Boolean usageOpValue = parser.getOptionValue(usageOp, Boolean.FALSE);
		String convOpValue = parser.getOptionValue(convOp);
		
		String[] otherArgs = parser.getRemainingArgs();
		if (otherArgs.length < 1){
			printUsage();
			System.exit(2);
		}
		
		String pdf_file = otherArgs[0];
		
		// Print Usage Info
		if (usageOpValue){
			printUsage();
			System.exit(0);
		}
		
		// print xmp info
		if (xmpOpValue){
			PDFTextParser.getPDFXmpMeta(pdf_file);
			System.exit(0);
		}
		
		// print pdf info
		if (infoOpValue){
			PDFTextParser.getPDFMeta(pdf_file);
			PDFTextParser.getPDFXmpMeta(pdf_file);
			System.exit(0);
		}
		
		// print hash
		if (hashOpValue){
			System.out.println(toSHA1(pdf_file));
			System.exit(0);
		}
		
		// search for doi and bibtex
		// log4j? to-do list
		if (doiOpValue){
			String doiString = PDFTextParser.pdfdoi(pdf_file);
			if (doiString == null || doiString.trim().equals("")){
				System.out.println("No DOI has found in this PDF file.");
			}else{
				System.out.println("DOI: " + doiString);
				System.out.println("========== Retrieving Bibtex entry for DOI ======== ");
				DOItoBibTeXFetcher test = new DOItoBibTeXFetcher();
				String bibtex = test.getEntryFromDOI(doiString);
				
				if (bibtex == null || bibtex.trim().equals("")){
					System.out.println("Can not find the bibtex entry from DOI.");
				}else{
					System.out.println(bibtex);
				}
			}
			
			System.exit(0);
		}
		
		// print bibtex from a pdf's xmp
		if (bibOpValue){
			try{
				List<BibtexEntry> l = XMPUtil.readXMP(new File(pdf_file));
				for (BibtexEntry e : l) {
					StringWriter sw = new StringWriter();
					e.write(sw, new LatexFieldFormatter(),false);
					System.out.println(sw.getBuffer().toString());
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			System.exit(0);
		}
		
		// auto convert
		if (autoOpValue){
			String doiString = PDFTextParser.pdfdoi(pdf_file);
			if (doiString == null || doiString.trim().equals("")){
				System.out.println("No DOI has found in this PDF file.");
			}else{
				System.out.println("DOI: " + doiString);
				System.out.println("========== Retrieving Bibtex entry for DOI ======== ");
				DOItoBibTeXFetcher test = new DOItoBibTeXFetcher();
				String bibtex = test.getEntryFromDOI(doiString);
				
				if (bibtex == null || bibtex.trim().equals("")){
					System.out.println("Can not find the bibtex entry from DOI.");
				}else{
					System.out.println(bibtex);
							
					try{
						BibtexEntry result = BibtexParser.singleFromString(bibtex);
					
						if (result == null) {
							System.err.println("Could not find a valid BibtexEntry ");
						} else {
							XMPUtil.writeXMP(new File(pdf_file), result, null);
							System.out.println("XMP written.");
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			System.exit(0);
		}
		
		// convert
		if (convOpValue != null){
			System.out.println("convOp: " + convOpValue);
			try{
				BufferedReader br = new BufferedReader(new FileReader(convOpValue));
				String line = null;
				String bibtex = null;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				bibtex = sb.toString();
				
				
				BibtexEntry result = BibtexParser.singleFromString(bibtex);
				
				if (result == null) {
					System.err.println("Could not find a valid BibtexEntry ");
				} else {
					XMPUtil.writeXMP(new File(pdf_file), result, null);
					System.out.println("XMP written.");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
		System.out.println("autoOpValue: " + autoOpValue);
      System.out.println("bibOpValue: " + bibOpValue);
      System.out.println("doiOpValue: " + doiOpValue);
      System.out.println("hashOpValue: " + hashOpValue);
		System.out.println("infoOpValue: " + infoOpValue);
		System.out.println("xmpOpValue: " + xmpOpValue);
		System.out.println("usageOpValue: " + usageOpValue);
		
		System.out.println("convOp: " + convOpValue);
		System.out.println("remaining args: ");
        for ( int i = 0; i < otherArgs.length; ++i ) {
            System.out.println(otherArgs[i]);
        }
		System.out.println("=============");
		
		
		
	}
	
	private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
	
	public static String toSHA1(String filename){
		// return the same hash value
		//    git hash-object pdf_file;
		MessageDigest md=null;
		try{
			md = MessageDigest.getInstance("SHA-1");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			FileInputStream in = new FileInputStream(filename);
			BufferedInputStream buffIn = new BufferedInputStream(in);
			DigestInputStream dis = new DigestInputStream(buffIn, md);
			
			File f = new File(filename);
			Long fileSize = f.length();
			
			String blob = "blob " + fileSize.toString() + '\0';
			md.update(blob.getBytes());
						
			// read the file and update the hash calculation
         while (dis.read() != -1);

         // get the hash value as byte array
         byte[] hash = md.digest();

        return byteArray2Hex(hash);
		}catch(Exception e){
			e.printStackTrace();
		}
			
		return "";
	}
	
	public static void writeInfoDictionary(FileInputStream in, 
			String outputFile, byte[] xmp) throws IOException, COSVisitorException {
	
		PDFParser parser = new PDFParser(in);
		parser.parse();
	
		PDDocument document = parser.getPDDocument();
		PDDocumentInformation info = document.getDocumentInformation();
		/*
		for (Entry<String, String> entry : XmpUtils.toInfo(xmp).entrySet()) {
			info.setCustomMetadataValue(entry.getKey(), entry.getValue());
		}
		*/
		document.setDocumentInformation(info);
		document.save(outputFile);
		document.close();
	}
	
	/**
	 * According to the PDF Reference Manual (appendix F) a linearized PDF
	 * must have as its first object after the PDF header an indirect
	 * dictionary containing only direct objects. Among these objects one
	 * must be assigned the key "Linearized", representing the linearized PDF
	 * version number.
	 * 
	 * @return true if the PDF read by reader is a linearized PDF.
	 */
	public static boolean isLinearizedPdf(FileInputStream in) throws IOException {
		boolean isLinear = false;
		
		PDFParser parser = new PDFParser(in);
		parser.parse();
		COSDocument doc = parser.getDocument();
		
		for (Object o : doc.getObjects()) {
			COSObject obj = (COSObject) o;
			if (obj.getObject() instanceof COSDictionary) {
				COSDictionary dict = (COSDictionary) obj.getObject();
				for (Object key : dict.keyList()) {
					COSName name = (COSName) key;
					if ("Linearized".equals(name.getName())) {
						isLinear = true;
						break;
					}
				}
				
				if (isLinear) break;
			}
		}
		
		doc.close();
		
		return isLinear;
	}
	
	private String getXmpForDoi(String doi) {
		MarkBuilder builder = new MarkBuilder();
		
		return " ";
		//builder.getXmpData();
	}
	
	private static String getOutFileName(String pdfFileName) {
		if (pdfFileName.endsWith(".pdf")) {
			return pdfFileName.substring(0, pdfFileName.length() - 4)
					+ "_xmp.pdf";
		} else {
			return pdfFileName + "_xmp.pdf";
		}
	}
	
	private void exitWithError(int code, String error) {
		
		System.err.println();
		System.err.println(error);
		System.exit(code);
	}
}
