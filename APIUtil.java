package com.ibsplc.icargo.apiutility.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/*
 * 
 * APIUtil.java created on 23-Nov-2021
 * 
 * @author A-9627
 *
 * Copyright 2021 IBS Software Services (P) Ltd. All Rights Reserved.
 *
 * This software is the proprietary information of IBS Software Services (P) Ltd.
 * Use is subject to license terms.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.io.Files;
import com.ibsplc.icargo.generic.Path;
import com.ibsplc.icargo.generic.TestData;
import com.ibsplc.iraft.properties.PropertyHandler;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;

public class APIUtil {

    protected static final String fileSeparator = System.getProperty("file.separator");
    protected String apiRequestFiles = PropertyHandler.getPropValue(
	    "src" + fileSeparator + "main" + fileSeparator + "resources" + fileSeparator + "EnvSetup.properties",
	    "soapRequestFiles").replace("\\", fileSeparator).replace("/", fileSeparator);
    Logger logger = Logger.getLogger(APIUtil.class);
    protected String baseURL;
    protected String subFolder;

    /**
     * constructor - returns path of file appended with subfolder. subfolder name
     * could be same as module name
     * 
     * @param subFolder
     */
    public APIUtil(String subFolder, String baseURL) {
	this.apiRequestFiles = apiRequestFiles + subFolder + fileSeparator;
	this.baseURL = baseURL;
	this.subFolder = subFolder;
//	System.setProperty("http.proxyHost", "webproxy.ibsplc.com");
//	System.setProperty("http.proxyPort", "80");
    }
    
    public APIUtil(String subFolder, String baseURL, boolean setProxy,String proxyHost, String port) {
	this.apiRequestFiles = apiRequestFiles + subFolder + fileSeparator;
	this.baseURL = baseURL;
	if(setProxy==true)
	{
		System.setProperty("http.proxyHost", proxyHost);
		System.setProperty("http.proxyPort", port);
	}

    }

////////////////////////////////////////////////////////////////////////// SOAP ///////////////////////////////////////////////////////////////////////////////

    public void modifyXMLAttribute(String fileName, String[] tagName, int[] tagIndex, int[] attributeIndex,
	    String[] newAttributeValue) {
	String file = apiRequestFiles + fileName;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	try (InputStream is = new FileInputStream(file)) {
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(is);
	    int size = tagName.length;
	    for (int i = 0; i < size; i++) {
		NodeList n = doc.getElementsByTagName(tagName[i]);
		n.item(tagIndex[i]).getAttributes().item(attributeIndex[i]).setNodeValue(newAttributeValue[i]);
	    }
	    DOMSource source = new DOMSource(doc);
	    FileWriter writer = new FileWriter(new File(file));
	    StreamResult result = new StreamResult(writer);
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.transform(source, result);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    public Document modifyXMLAttributeAndReturnDoc(String fileName, String[] tagName, int[] tagIndex, int[] attributeIndex,
    	    String[] newAttributeValue) {
    	String file = apiRequestFiles + fileName;
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	Document doc = null;
    	try (InputStream is = new FileInputStream(file)) {
    	    DocumentBuilder db = dbf.newDocumentBuilder();
    	    doc = db.parse(is);
    	    int size = tagName.length;
    	    for (int i = 0; i < size; i++) {
    		NodeList n = doc.getElementsByTagName(tagName[i]);
    		n.item(tagIndex[i]).getAttributes().item(attributeIndex[i]).setNodeValue(newAttributeValue[i]);
    	    }
    	    DOMSource source = new DOMSource(doc);
    	    StringWriter writer = new StringWriter();
    	    StreamResult result = new StreamResult(writer);
    	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    	    Transformer transformer = transformerFactory.newTransformer();
    	    transformer.transform(source, result);
    	    logger.info(writer.toString());
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	return doc;
        }
    
    public String getXMLAttribute(String fileName, String tagName, int tagIndex, int attributeIndex) {
	String file = apiRequestFiles + fileName;
	String attr = null;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	try (InputStream is = new FileInputStream(file)) {
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(is);
	    NodeList n = doc.getElementsByTagName(tagName);
	    attr = n.item(tagIndex).getAttributes().item(attributeIndex).getNodeValue();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return attr;

    }
    
    public String getXMLAttributeFromResponse(Response response, String tagName, int tagIndex, int attributeIndex) {
    	
    	String attr = null;
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	try (InputStream is = new ByteArrayInputStream(response.asString().getBytes(StandardCharsets.UTF_8));) {
    	    DocumentBuilder db = dbf.newDocumentBuilder();
    	    Document doc = db.parse(is);
    	    NodeList n = doc.getElementsByTagName(tagName);
    	    attr = n.item(tagIndex).getAttributes().item(attributeIndex).getNodeValue();
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	return attr;

        }
    
    public void modifyXMLTextContent(String fileName, String[] tagName, int[] tagIndex, String[] newText) {
	String file = apiRequestFiles + fileName;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	try (InputStream is = new FileInputStream(file)) {
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(is);
	    int size = tagName.length;
	    for (int i = 0; i < size; i++) {
		NodeList n = doc.getElementsByTagName(tagName[i]);
		n.item(tagIndex[i]).setTextContent(newText[i]);

	    }
	    DOMSource source = new DOMSource(doc);
	    FileWriter writer = new FileWriter(new File(file));
	    StreamResult result = new StreamResult(writer);
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.transform(source, result);
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
    
    /**
     * Modify xml attributes and returns a document object
     * @author 202605
     * @param fileName
     * @param tagName
     * @param tagIndex
     * @param newText
     * @return
     */
    public Document modifyXMLTextContentAndReturnDoc(String fileName, String[] tagName, int[] tagIndex, String[] newText) {
    	String file = apiRequestFiles + fileName;
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	Document doc=null;
    	try (InputStream is = new FileInputStream(file)) {
    	    DocumentBuilder db = dbf.newDocumentBuilder();
    	    doc = db.parse(is);
    	    int size = tagName.length;
    	    for (int i = 0; i < size; i++) {
    		NodeList n = doc.getElementsByTagName(tagName[i]);
		if (n.getLength() > 0) {
		    if (tagIndex[i] >= 0 && tagIndex[i] < n.getLength()) {
			n.item(tagIndex[i]).setTextContent(newText[i]);
		    } else {
			System.err.println("Index out of bounds for tag:" + tagName[i] + ", Index: " + tagIndex[i]);
		    }
		} else {
		    System.err.println("No elements found for tag: " + tagName[i]);
		}
	    }
    	    DOMSource source = new DOMSource(doc);
    	    StringWriter writer = new StringWriter();
    	    StreamResult result = new StreamResult(writer);
    	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    	    Transformer transformer = transformerFactory.newTransformer();
    	    transformer.transform(source, result);
    	    logger.info(writer.toString());
    	    
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	return doc;
        }
    /**
     * Write a Document object to a file for debugging
     * @author 202605
     * @param doc
     * @param fileName
     */
    public void writeDoctoFile(Document doc,String fileName ) {
    	String file = apiRequestFiles + fileName;
    	try (InputStream is = new FileInputStream(file)) {
    	    DOMSource source = new DOMSource(doc);
    	    FileWriter writer = new FileWriter(new File(file));
    	    StreamResult result = new StreamResult(writer);
    	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    	    Transformer transformer = transformerFactory.newTransformer();
    	    transformer.transform(source, result);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}

        }
    
    public String getXMLTextContent(String fileName, String tagName, int tagIndex) {
	String file = apiRequestFiles + fileName;
	String textContent = null;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	try (InputStream is = new FileInputStream(file)) {
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(is);
	    NodeList n = doc.getElementsByTagName(tagName);
	    textContent = n.item(tagIndex).getTextContent();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return textContent;

    }
    
    /**
     * get xml tag Value from response object
     * 
     * @author 202605
     * @param response
     * @param tagName
     * @param tagIndex
     * @return
     */
    public String getXMLTextContentFromResponse(Response response, String tagName, int tagIndex) {
    	String textContent = null;
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	try (InputStream is = new ByteArrayInputStream(response.asString().getBytes(StandardCharsets.UTF_8));) {
    	    DocumentBuilder db = dbf.newDocumentBuilder();
    	    Document doc = db.parse(is);
    	    NodeList n = doc.getElementsByTagName(tagName);
    	    textContent = n.item(tagIndex).getTextContent();
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	return textContent;

        }

    /**
     * read request body from file and send post request, save the response to a
     * file. works for both soap and rest requests
     * 
     * @param baseURL
     * @param endPoint
     * @param reqFileName
     * @param responseFileName - file to which response to be saved
     * @param headers
     * @throws IOException
     */
    public void sendPOSTRequestAndSaveResponse(String endPoint, String reqFileName, String responseFileName,
	    String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	FileInputStream fs = new FileInputStream(apiRequestFiles +Path.getFs()+ reqFileName);
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).and().body(IOUtils.toString(fs, "UTF-8")).when()
		.post(endPoint).then().statusCode(200).and().log().all().extract().response();
	logger.info(response.getBody().toString());
	String responseAsString = response.asString();
	byte[] responseAsStringByte = responseAsString.getBytes();

	File targetFileForString = new File(apiRequestFiles + responseFileName);
	Files.write(responseAsStringByte, targetFileForString);

    }

    /**
     * read request body from document and send post request, return response. 
     * works for both soap and rest requests
     * 
     * @author 202605
     * @param baseURL
     * @param endPoint
     * @param doc 
     * @param headers
     * @throws IOException
     */
    public Response sendPOSTRequestAndReturnResponse(String endPoint, Document doc,
	    String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	Source xmlSource = new DOMSource(doc);
	Result outputTarget = new StreamResult(outputStream);
	try {
		TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
	} catch (TransformerException | TransformerFactoryConfigurationError e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
//	FileInputStream fs = new FileInputStream(apiRequestFiles +Path.getFs()+ reqFileName);
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).and().body(IOUtils.toString(is, "UTF-8")).when()
		.post(endPoint).then().statusCode(200).and().log().all().extract().response();
	logger.info(response.getBody().toString());
	
	return response;
	
    }

    /**
     * read request body from file and send post request, save the response to a
     * file. works for both soap and rest requests
     * 
     * @param baseURL
     * @param endPoint
     * @param reqFileName
     * @param responseFileName - file to which response to be saved
     * @param headers
     * @throws IOException
     */
    public void sendGETRequestAndSaveResponse(String endPoint, String reqFileName, String responseFileName,
	    String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	FileInputStream fs = new FileInputStream(apiRequestFiles + reqFileName);
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).and().body(IOUtils.toString(fs, "UTF-8")).when()
		.get(endPoint).then().statusCode(200).and().log().all().extract().response();
	logger.info(response.getBody().toString());
	String responseAsString = response.asString();
	byte[] responseAsStringByte = responseAsString.getBytes();

	File targetFileForString = new File(apiRequestFiles + responseFileName);
	Files.write(responseAsStringByte, targetFileForString);

    }

////////////////////////////////////////////////////////////////////////////REST ////////////////////////////////////////////////////////////////////////////	
    /**
     * read from a json file and update any key values and return updated JSONObject
     * @updated: A-10557 to handle array inside array keys.
     * @param fileName
     * @param keyString
     * @param newValue
     * @return
     * @throws Exception
     */
    public JSONObject updateNestedJson(String fileName, String[] keyString, String[] newValue) throws Exception {

	File f = new File(apiRequestFiles + Path.getFs() + fileName);
	InputStream is = new FileInputStream(f);
	String jsonTxt = IOUtils.toString(is, "UTF-8");
	JSONObject obj = new JSONObject(jsonTxt);
	int keyLength = keyString.length;

	for (int k = 0; k < keyLength; k++) {
	    JSONObject jo = obj;
	    if (keyString[k].contains(".")) {
		String[] splitKey = keyString[k].split("\\.");
		int size = splitKey.length;
		try {
		    for (int i = 0; i < size - 1; i++) {
			if (splitKey[i].contains("[")) {
			    String splitArrayKey = splitKey[i].split("\\[")[0];
			    String splitArayInt = splitKey[i].split("\\[")[1].split("\\]")[0];
			    int splitArrayInt = Integer.parseInt(splitArayInt);
			    JSONArray j = jo.getJSONArray(splitArrayKey);
			    jo = (JSONObject) j.get(splitArrayInt);
			} else {
			    jo = jo.getJSONObject(splitKey[i]);
			}
		    }

		    String lastKey = splitKey[size - 1];
		    if (lastKey.contains("[")) {
			String splitArrayKey = lastKey.split("\\[")[0];
			String splitArrayIndex = lastKey.split("\\[")[1].split("\\]")[0];
			int splitArrayInt = Integer.parseInt(splitArrayIndex);
			JSONArray j = jo.getJSONArray(splitArrayKey);

			if (j.get(splitArrayInt) instanceof Integer) {
			    j.put(splitArrayInt, Integer.parseInt(newValue[k]));
			} else if (j.get(splitArrayInt) instanceof Boolean) {
			    j.put(splitArrayInt, Boolean.parseBoolean(newValue[k]));
			} else if (j.get(splitArrayInt) instanceof Double) {
			    j.put(splitArrayInt, Double.parseDouble(newValue[k]));
			} else if (j.get(splitArrayInt) instanceof Float) {
			    j.put(splitArrayInt, Float.parseFloat(newValue[k]));
			} else {
			    j.put(splitArrayInt, newValue[k]);
			}
		    } else {
			if (jo.get(lastKey) instanceof Integer) {
			    jo.put(lastKey, Integer.parseInt(newValue[k]));
			} else if (jo.get(lastKey) instanceof Boolean) {
			    jo.put(lastKey, Boolean.parseBoolean(newValue[k]));
			} else if (jo.get(lastKey) instanceof Double) {
			    jo.put(lastKey, Double.parseDouble(newValue[k]));
			} else if (jo.get(lastKey) instanceof Float) {
			    jo.put(lastKey, Float.parseFloat(newValue[k]));
			} else {
			    jo.put(lastKey, newValue[k]);
			}
		    }
		} catch (Exception e) {
		    System.err.println("Invalid key string passed at keyIndex: "+k);
		}
	    } else {
		try {
		    obj.put(keyString[k], newValue[k]);
		} catch (Exception e) {
		    System.err.println("Invalid key string passed at keyIndex: "+k);
		}
	    }
	}

	logger.info(obj);
	return obj;

    }

    /**
     * get a JSONObject as input and trigger a res assured API post request and
     * return Response
     * 
     * @param endPoint
     * @param json
     * @param headers
     * @return
     * @throws IOException
     */
    public Response sendRESTPOSTRequest(String endPoint, JSONObject json, String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	logger.info(this.baseURL + endPoint);
	logger.info(head);
	Response response = RestAssured.given().headers(head).and().body(json.toString()).when().post(endPoint).then()
		/*.statusCode(200)*/.and().log().all().extract().response();
	logger.info(response.getBody().toString());

	return response;

    }

    /**
     * get a JSONObject as input and trigger an API get request and return Response
     * 
     * @param endPoint
     * @param headers
     * @return
     * @throws IOException
     */
    public Response sendRESTGETRequest(String endPoint, String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).when().get(endPoint).then()
		.statusCode(200).and().log().all().extract().response();
	logger.info(response.getBody().toString());

	return response;

    }

    /**
     * convert a response to JSONObject
     * 
     * @param res
     * @return
     * @throws IOException
     */
    public JSONObject returnJSONObjectFromResponse(Response res) throws IOException {

	JSONObject obj = new JSONObject(res.asString());

	return obj;
    }

    /**
     * get the value against any key in the JSONObject received as an output of an
     * API request and return it as a String
     * 
     * @param obj
     * @param keyString
     * @return
     * @throws Exception
     */
    public String getNodeValueFromJsonResponse(JSONObject obj, String keyString) throws Exception {

	JSONObject jo = obj;
	String result = null;
	if (keyString.contains(".")) {
	    String[] splitKey = keyString.split("\\.");
	    int size = splitKey.length;
	    try {
		for (int i = 0; i < size; i++) {
		    if (i != size - 1 && !splitKey[i].contains("[")) {
			jo = jo.getJSONObject(splitKey[i]);
		    } else if (i != size - 1 && splitKey[i].contains("[")) {
			String splitArrayKey = splitKey[i].split("\\[")[0];
			String splitArayInt = splitKey[i].split("\\[")[1].split("\\]")[0];
			int splitArrayInt = Integer.parseInt(splitArayInt);
			JSONArray j = jo.getJSONArray(splitArrayKey);
			jo = (JSONObject) j.get(splitArrayInt);
		    } else if (i == size - 1 && !splitKey[i].contains("[")) {
			result = jo.get(splitKey[i]).toString();
		    } else if (i == size - 1 && splitKey[i].contains("[")) {
			String splitArrayKey = splitKey[i].split("\\[")[0];
			String splitArayInt = splitKey[i].split("\\[")[1].split("\\]")[0];
			int splitArrayInt = Integer.parseInt(splitArayInt);
			JSONArray j = jo.getJSONArray(splitArrayKey);
			result = j.get(splitArrayInt).toString();
		    }
		}
	    } catch (Exception e) {
		System.err.println("Invalid key string passed");
	    }
	} else {
	    try {
		result = obj.get(keyString).toString();
	    } catch (Exception e) {
		System.err.println("Invalid key string passed");
	    }
	}
	return result;

    }

    /**
     * For returning session id from login request
     * 
     * @param keys
     * @param values
     * @param identitytoken
     * @param className
     * @return
     * @throws Exception
     */
    public String getSessionID(String[] keys, String[] values, String identitytoken, String className)
	    throws Exception {
	JSONObject requestjson = updateNestedJson("hhtloginreq.json", keys, values);
	logger.info(requestjson);
	Response resp = sendRESTPOSTRequest("/baseadmin/mobilityLogin", requestjson, "Content-Type", "application/json",
		"Accept", "application/json", "icargo-identitytoken", identitytoken);
	JSONObject responsejson = returnJSONObjectFromResponse(resp);
	logger.info(responsejson);
	String sessionId = getNodeValueFromJsonResponse(responsejson, "results[0].sessionId");
	logger.info(sessionId);
	TestData.setInterimData(className, "sessionID", sessionId);
	return sessionId;
    }
    /**
     * get a JSONObject as input and trigger an API delete request and return Response
     * 
     * @param endPoint
     * @param headers
     * @return
     * @throws IOException
     */
    public Response sendRESTDELETERequest(String endPoint, String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).when().delete(endPoint).then().log().all().extract().response();
	int statusCode = response.getStatusCode();
	if (statusCode != 200 && statusCode != 204 && statusCode != 201 && statusCode != 203 ) {
	    throw new AssertionError("Unexpected status code: " + statusCode);
	}
	logger.info(response.getBody().toString());

	return response;

    }
    
    /**
     * For returning session id from login request for Neo
     * 
     * @param keys
     * @param values
     * @param companyCode
     * @param className
     * @return
     * @throws Exception
     */
    public String getSessionIDNeo(String[] keys, String[] values,String companyCode, String className)
    	    throws Exception {
    	String endPoint="/auth/"+companyCode.toLowerCase()+"/private/v1/authenticate";
    	JSONObject requestjson = updateNestedJson("neoLogin.json", keys, values);
    	logger.info(requestjson);
    	System.out.println("endpoint URL:"+endPoint);
    	Response resp = sendRESTPOSTRequest(endPoint, requestjson, "Content-Type", "application/json");
    	JSONObject responsejson = returnJSONObjectFromResponse(resp);
    	logger.info(responsejson);
    	String sessionId = getNodeValueFromJsonResponse(responsejson, "body.security.id_token");
    	logger.info(sessionId);
    	TestData.setInterimData(className, "sessionID", sessionId);
    	return sessionId;
        }
    
    /**
     * @author A-10557: To return response json as string for json node level values verifications in test
     * @param baseUrl
     * @param endPoint
     * @param fileName
     * @param keys
     * @param values
     * @param identitytoken
     * @param statusCode
     * @return
     * @throws Exception
     */
    public String APIRequestGetResponseAsString(String endPoint, String fileName, String[] keys, String[] values,
	    String identitytoken, String statusCode) throws Exception {

	APIUtil sp = new APIUtil(this.subFolder, this.baseURL);
	JSONObject requestjson = sp.updateNestedJson(fileName, keys, values);
	logger.info(requestjson);
	Response resp = sp.sendRESTPOSTRequest(endPoint, requestjson, "Content-Type", "application/json", "Accept",
		"application/json", "icargo-identitytoken", identitytoken);
	JSONObject responsejson = sp.returnJSONObjectFromResponse(resp);
	logger.info(responsejson);
	if (statusCode.length() != 0) {
	    String status = sp.getNodeValueFromJsonResponse(responsejson, "status");
	    Assert.assertEquals("Request Processed Successfully", statusCode, status);
	}
	return responsejson.toString();
    }
    
    /**
     * @author A-10557: To send Post Request and save response File with multiple header key values. 
     * @param baseURL
     * @param endPoint
     * @param reqFileName
     * @param responseFileName
     * @param headers
     * @return
     * @throws IOException
     */
    public String sendPOSTRequestFileAndSaveResponse(String endPoint, String reqFileName, String responseFileName,
	    String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	FileInputStream fs = new FileInputStream(apiRequestFiles  + fileSeparator+ reqFileName);
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).and().body(IOUtils.toString(fs, "UTF-8")).when()
		.post(endPoint).then().statusCode(200).and().log().all().extract().response();
	logger.info(response.getBody().toString());
	String responseAsString = response.asString();
	byte[] responseAsStringByte = responseAsString.getBytes();

	File targetFileForString = new File(apiRequestFiles  + fileSeparator+ responseFileName);
	Files.write(responseAsStringByte, targetFileForString);
	return responseAsString;

    }
    
    /**
     * @author A-10557: To return response json as string for json node level values
     *         verifications with different header Key other than
     *         icargo-identitytoken.
     * @param baseUrl
     * @param endPoint
     * @param fileName
     * @param keys
     * @param values
     * @param identitytoken
     * @param statusCode
     * @return
     * @throws Exception
     */
    public String APIRequestGetResponseAsString(String endPoint, String fileName, boolean squareBracketAtBegining, String[] keys,
	    Object[] values, String... headers) throws Exception {
	
	JSONObject requestjson = updateNestedJsonwithJsonObjects(fileName, keys, values);
	logger.info(requestjson.toString());
	
	Response resp = null;
	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}

	RestAssured.baseURI = this.baseURL;
	logger.info("to get base URL and endpont" + this.baseURL + endPoint);	
	Headers head = new Headers(list);
	Response response = null;
	logger.info(head);
	if(!squareBracketAtBegining) {
	    response = RestAssured.given().headers(head).and().body(requestjson.toString()).when().post(endPoint).then()
		    .statusCode(200).and().log().all().extract().response();
	    logger.info(response.getBody().toString());
	} else{
		response = RestAssured.given().headers(head).and().body("[" + requestjson.toString() + "]").when()
			.post(endPoint).then().statusCode(200).and().log().all().extract().response();
		logger.info(response.getBody().toString());
	}
	String res = response.getBody().asString();
	try {

	JSONObject responsejson = returnJSONObjectFromResponse(resp);

	res = responsejson.toString();
	}catch(Exception e) {
	    logger.info("Not a json response");
	}
	return res;
    }
     
    /**
     * @author A-10557
     * updateNestedJsonwithJsonArray - Use to Json file which paramters having  pased as List value as ["A","B"]
     *  for a 
     *     input like - "[\"A\", \"B\"]"
     * @param fileName
     * @param keyString
     * @param newValue
     * @return
     * @throws Exception
     */
    public JSONObject updateNestedJsonwithJsonArray(String fileName, String[] keyString, Object[] newValue)
	    throws Exception {
	File f = new File(apiRequestFiles + Path.getFs() + fileName);
	InputStream is = new FileInputStream(f);
	String jsonTxt = IOUtils.toString(is, "UTF-8");
	JSONObject obj = new JSONObject(jsonTxt);

	for (int k = 0; k < keyString.length; k++) {
	    String[] splitKey = keyString[k].split("\\.");
	    JSONObject jo = obj;

	    for (int i = 0; i < splitKey.length - 1; i++) {
		String key = splitKey[i];

		if (key.contains("[")) {
		    String splitArrayKey = key.split("\\[")[0];
		    int splitArrayIndex = Integer.parseInt(key.split("\\[")[1].split("\\]")[0]);
		    JSONArray j = jo.getJSONArray(splitArrayKey);
		    jo = (JSONObject) j.get(splitArrayIndex);
		} else {
		    jo = jo.getJSONObject(key);
		}
	    }

	    String lastKey = splitKey[splitKey.length - 1];
	    if (newValue[k]==null) {
		logger.info("bound :"+k+" value :  "+newValue[k]+" key:  "+keyString[k]) ;
	    }
	    
	    if (newValue[k] != null && newValue[k].toString().contains("[")) {
	    Object parsedValue = parseValue(newValue[k].toString());
	    
	    if (lastKey.contains("[")) {
		String splitArrayKey = lastKey.split("\\[")[0];
		int splitArrayIndex = Integer.parseInt(lastKey.split("\\[")[1].split("\\]")[0]);
		JSONArray j = jo.getJSONArray(splitArrayKey);
		j.put(splitArrayIndex, parsedValue);
	    } else {
		try {
		    jo.put(lastKey, parsedValue);
		} catch (Exception e) {
		    System.err.println("Invalid key string passed at keyIndex: " + k);
		}
	    }
	}
	else {
	    Object parsedValue = newValue[k];

	    if (lastKey.contains("[")) {
		String splitArrayKey = lastKey.split("\\[")[0];
		int splitArrayIndex = Integer.parseInt(lastKey.split("\\[")[1].split("\\]")[0]);
		JSONArray j = jo.getJSONArray(splitArrayKey);
		if(parsedValue!=null) {
		j.put(splitArrayIndex, parsedValue);
		}
	    } else {
		try {
		    if (parsedValue != null) {
			jo.put(lastKey, parsedValue);
		    }
		} catch (Exception e) {
		    System.err.println("Invalid key string passed at keyIndex: " + k);
		}
	    }
	}
	}
	logger.info(obj);
	return obj;
    }

    private Object parseValue(String value) {
	try {
	    // Try parsing as Integer
	    if(!(value.charAt(0)=='0')) {
	    return Integer.parseInt(value);
	    }
	    else {
		return value;
	    }
	} catch (NumberFormatException e1) {
	    try {
		// Try parsing as Double
		return Double.parseDouble(value);
	    } catch (NumberFormatException e2) {
		// Parse as JSON array
		if (value.contains("[")) {
		    System.out.println(value);
		    value = value.replace("[", "").replace("]", "");
		    return new JSONArray("[" + value + "]");
		} else {
		    return value;
		}
	    }
	}
    }


    /**
     * get a JSONObject as input and trigger a res assured API post request and
     * return Response with status code assertion
     * 
     * @author A-10540
     * @param endPoint
     * @param json
     * @param expectedStatus -Asserted response to be passed as Integer
     * @param headers
     * @return
     * @throws IOException
     */
    public Response sendRESTPOSTRequestWithStatusCode(String endPoint, JSONObject json,int expectedStatus, String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).and().body(json.toString()).when().post(endPoint).then()
		.statusCode(expectedStatus).and().log().all().extract().response();
	logger.info(response.getBody().toString());

	return response;

    }
    
    /**
     * @author A-10557
     * updateNestedJsonwithJsonObjects - To replace the value with exact object.
     * @param fileName
     * @param keyString
     * @param newValue
     * @return
     * @throws Exception
     */
    public JSONObject updateNestedJsonwithJsonObjects(String fileName, String[] keyString, Object[] newValue)
	    throws Exception {
	File f = new File(apiRequestFiles + Path.getFs() + fileName);
	InputStream is = new FileInputStream(f);
	String jsonTxt = IOUtils.toString(is, "UTF-8");
	JSONObject obj = new JSONObject(jsonTxt);

	for (int k = 0; k < keyString.length; k++) {
	    String[] splitKey = keyString[k].split("\\.");
	    JSONObject jo = obj;

	    for (int i = 0; i < splitKey.length - 1; i++) {
		String key = splitKey[i];

		if (key.contains("[")) {
		    String splitArrayKey = key.split("\\[")[0];
		    int splitArrayIndex = Integer.parseInt(key.split("\\[")[1].split("\\]")[0]);
		    JSONArray j = jo.getJSONArray(splitArrayKey);
		    jo = (JSONObject) j.get(splitArrayIndex);
		} else {
		    jo = jo.getJSONObject(key);
		}
	    }

	    String lastKey = splitKey[splitKey.length - 1];
	    Object parsedValue = newValue[k];

	    if (lastKey.contains("[")) {
		String splitArrayKey = lastKey.split("\\[")[0];
		int splitArrayIndex = Integer.parseInt(lastKey.split("\\[")[1].split("\\]")[0]);
		JSONArray j = jo.getJSONArray(splitArrayKey);
		if(parsedValue!=null) {
		j.put(splitArrayIndex, parsedValue);
		}
	    } else {
		try {
		    if (parsedValue != null) {
			jo.put(lastKey, parsedValue);
		    }
		} catch (Exception e) {
		    System.err.println("Invalid key string passed at keyIndex: " + k);
		}
	    }
	}

	logger.info(obj);
	return obj;
    }

    /**
     * get a JSONArray as input and trigger a res assured API post request and
     * return Response with status code assertion
     * 
     * @author A-10540
     * @param endPoint
     * @param json
     * @param expectedStatus -Asserted response to be passed as Integer
     * @param headers
     * @return
     * @throws IOException
     */
    public Response sendRESTPOSTRequestWithStatusCode_jsonArray(String endPoint, JSONArray json, int expectedStatus,
	    String... headers) throws IOException {

	int size = headers.length;

	List<Header> list = new ArrayList<Header>();
	for (int i = 0; i < size; i += 2) {
	    Header h = new Header(headers[i], headers[i + 1]);
	    list.add(h);
	}
	RestAssured.baseURI = this.baseURL;

	Headers head = new Headers(list);
	Response response = RestAssured.given().headers(head).and().body(json.toString()).when().post(endPoint).then()
		.statusCode(expectedStatus).and().log().all().extract().response();
	logger.info(response.getBody().toString());

	return response;

    }

}

