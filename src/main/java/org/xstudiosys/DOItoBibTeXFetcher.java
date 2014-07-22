package org.xstudiosys.pdfxmp;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

public class DOItoBibTeXFetcher {
	
    private static final String URL_PATTERN = "http://dx.doi.org/%s"; 
    
    public static void main(String[] args){
       //String query="10.1108/LHT-08-2013-0108";
		 String query="10.1109/ICDE.2009.103";
       DOItoBibTeXFetcher test = new DOItoBibTeXFetcher();

       String entry = test.getEntryFromDOI(query);
       System.out.println(entry); 
    }

	 /* This function is a modified version from JabRef
	  *
	  */
    public String getEntryFromDOI(String doi) {
        String q;
        try {
            q = URLEncoder.encode(doi, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // this should never happen
            e.printStackTrace();
            return null;
        }

        String urlString = String.format(URL_PATTERN, q);

        // Send the request
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

      
        String bibtexString;
        try {
	      conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "text/bibliography; style=bibtex");
             BufferedReader in = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
	    String inputLine;
	    StringBuffer response = new StringBuffer();
	
	    while((inputLine = in.readLine()) != null){
				response.append(inputLine);
	    }
	    in.close();

	    bibtexString = response.toString();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	
	return bibtexString;
	}	


}
