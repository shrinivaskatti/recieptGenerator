import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.DoubleBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Tab;
import com.itextpdf.layout.element.TabStop;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TabAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

public class ReceiptGenerator {

	public static final String KEYSTORE = "D:/temp/";

	public static final char[] PASSWORD = "password".toCharArray();
	public static int RECEIPT_NUMBER =1;
	public static String dest = "F:/Study/NilayaFoundation/2020-21/";
	public static String strHeader ="Name,PAN,Mobile,Email,VolunteerEmail,2020 Trxn Date,2020,Reciept";
	public static float bodyTextSize = 14;
	public static String masterFileLocation ="F:/Study/NilayaFoundation/2020-21/DonorMasterList.csv";
	public static void main(String[] args)
	{
		try
		{
			File donorOutputFile = new File(dest+"donorOutput.csv");
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(donorOutputFile));
			bWriter.write(strHeader+"\n");
			List<HashMap> donorDataList = readMasterFile();

			for (int i = 0; i < donorDataList.size(); i++) {
				Map donorMap = donorDataList.get(i);
				String name=(String)donorMap.get("name");
				String mobileNumber = (String)donorMap.get("mobilenumber");
				String emailAddress =(String)donorMap.get("emailaddress");
				String pan =(String)donorMap.get("pan");
				String strDonationAmount = (String)donorMap.get("amount");
				String strTransactionDate =(String)donorMap.get("transactionDate");
				String scheme ="Scholorship";
				String volunteeremail = (String)donorMap.get("volunteeremail");
				System.out.println();
				String fileName = generatePdf(name, mobileNumber, emailAddress, pan, strDonationAmount, strTransactionDate,scheme);
				
				String strOutputLine = name+","+pan+","+mobileNumber+","+emailAddress+","+volunteeremail+","+strTransactionDate+
						","+strDonationAmount+","+fileName;
				bWriter.write(strOutputLine+"\n");	
			}
			bWriter.close();
						
			System.out.println("Doc Created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private static List<HashMap> readMasterFile() {
		ArrayList donorDataList = new ArrayList<Map>();
		try {
			
			Map donorData;
			String line ="";
			BufferedReader br = new BufferedReader(new FileReader(masterFileLocation));
			br.readLine();
			while ((line = br.readLine()) != null)   //returns a Boolean value  
			{  
				String[]data =  line.split(",");
				System.out.println("DataLength=> "+data.length);
				donorData = new HashMap<String, String>();
				
				if(data.length == 8) {
					donorData.put("name", data[0]);
					donorData.put("pan", data[1]);
					donorData.put("mobilenumber", data[2]);
					donorData.put("emailaddress", data[3]);
					donorData.put("transactionDate", data[7]);
					donorData.put("amount", data[6]);
					donorData.put("volunteeremail", data[5]);
					donorDataList.add(donorData);
				}else {
					System.out.println("Donor "+data[0]+" is skipped due to insufficient data");
					return null;
				}
				
				
			}
					
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		 
		
		return donorDataList;
	}
	private static String generatePdf(String name, String mobileNumber, String emailAddress, String pan,
			String strDonationAmount, String dateReceived, String scheme) throws FileNotFoundException, IOException, Exception {
		 
		String strFileName=name+".pdf";
		String destSigned = "D:/temp/"+name+"_signed.pdf"; 
		File destFile = new File(dest+strFileName);
//		if(destFile.exists()) {
//			strFileName = name+"_"+RECEIPT_NUMBER+".pdf";
//		}
		PdfWriter writer = new PdfWriter(dest+strFileName);
		PdfDocument pdfDoc = new PdfDocument(writer);
		pdfDoc.addNewPage(); 
		Document document = new Document(pdfDoc); 
		
		configureHeader(document);

		setReceiptDetails(document);

		//addToPersonDetails(document, name, mobileNumber, emailAddress, pan);

		addReceiptBody(document, strDonationAmount, scheme, name, mobileNumber, dateReceived);


		addWatermark(pdfDoc, document);
		
		
		PdfCanvas  canvas = new PdfCanvas(pdfDoc, 1);
		canvas.rectangle(10, 10, 400, 600);
		canvas.saveState();
		document.close();
		return strFileName;
	}

	private static void addWatermark(PdfDocument pdfDoc, Document document) throws Exception {
		
		PdfCanvas over = new PdfCanvas(pdfDoc.getPage(1));
		
		
		
		Paragraph paragraph = new Paragraph("NILAYA FOUNDATION")
				.setFont(PdfFontFactory.createFont(StandardFonts.TIMES_BOLD))
		        .setFontSize(50);
		 PdfExtGState gs1 = new PdfExtGState().setFillOpacity(0.1f);
		PdfPage pdfPage = pdfDoc.getPage(1);
		Rectangle pageSize = pdfPage.getPageSizeWithRotation();
		
		// When "true": in case the page has a rotation, then new content will be automatically rotated in the
		// opposite direction. On the rotated page this would look as if new content ignores page rotation.
		pdfPage.setIgnorePageRotationForContent(true);

		float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
		float y = (pageSize.getTop() + pageSize.getBottom()) / 2;
		System.out.println(x+" -- "+y);
		
		
		
		over.saveState();
		over.setExtGState(gs1);
		document.showTextAligned(paragraph, x, 590, 1, TextAlignment.CENTER, VerticalAlignment.TOP, 0);
		//document.showTextAligned(paragraph, x, y, 1, TextAlignment.CENTER, VerticalAlignment.TOP, -1);
		over.restoreState();
	}

	private static void addDigitalSignature(String src, String dest) throws Exception {

		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(KEYSTORE), PASSWORD);
		String alias = ks.aliases().nextElement();
		PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
		Certificate[] chain = ks.getCertificateChain(alias);

		sign(src, dest, chain, pk,DigestAlgorithms.SHA256, provider.getName(),
				PdfSigner.CryptoStandard.CMS, "Test 1", "India");
	}

	private static void addReceiptBody(Document document, String strDonationAmount, String scheme, String name, String mobileNumber, String receivedDate) throws Exception {

		StringBuilder sbContent = new StringBuilder();
		//sbContent.append("Received with thanks from Shri/Smt <<name>>, <<mobile>> the sum of rupees <<amount>> <<receivedDate>>");
		//sbContent.append(" towards Special Scholorship.");
		sbContent.append("Received with thanks from Shri/Smt ");
		String contentString1 = "Received with thanks from Shri/Smt ";
		String contentString2 = " <<mobile>> the sum of rupees <<amount>> <<receivedDate>> towards Special Scholorship.";
		
		
		Text nameText = new Text(name);
		nameText.setFontSize(bodyTextSize);
		nameText.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
		

		if(mobileNumber.isEmpty()) { 
			contentString2 = contentString2.replace("<<mobile>>",mobileNumber); 
		}else { 
			contentString2 = contentString2.replace("<<mobile>>", "Mobile : "+mobileNumber);
		}

		if(receivedDate.isEmpty()) { 
			contentString2 =	contentString2.replace("<<receivedDate>>", receivedDate);
		}else {
			contentString2 = contentString2.replace("<<receivedDate>>","on "+receivedDate);
		}
		contentString2 = contentString2.replace("<<amount>>", strDonationAmount);


		Text contentText1 = new Text(contentString1);
		contentText1.setFontSize(bodyTextSize);
		contentText1.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));

		Text contentText2 = new Text(contentString2);
		contentText2.setFontSize(bodyTextSize);
		contentText2.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));
		
		Paragraph contentParagraph = new Paragraph();
	//	contentParagraph.add(new Tab());
		contentParagraph.add(contentText1);
		contentParagraph.add(nameText);
		contentParagraph.add(contentText2);
		contentParagraph.setTextAlignment(TextAlignment.JUSTIFIED);

		//PdfFont f1 =  PdfFontFactory.createRegisteredFont(StandardFonts.SYMBOL , PdfEncodings.UNICODE_BIG);		 
		 //PdfFont f1=  PdfFontFactory.getFont("resources/fonts/PlayfairDisplay-Regular.ttf", PdfFontFactory.IDENTITY_H, BaseFont.EMBEDDED, 12);
		//PdfFont font = PdfFontFactory.createFont("D:/temp/arial.ttf");
		//String strRupee = font.decode(new PdfString("\u10B9", PdfEncodings.UTF8));
		/*
		 * String imageFile = "D:/temp/indian_rupee_sign.jpg"; ImageData data =
		 * ImageDataFactory.create(imageFile); Image img = new Image(data);
		 * img.setWidth(15); img.setHeight(15);
		 */
		String strAmount = "  Amount Rs :"+strDonationAmount;
		Text amountText = new Text(strAmount);
		amountText.setTextAlignment(TextAlignment.JUSTIFIED_ALL);
		
		Paragraph amountParagraph = new Paragraph(amountText);
		amountParagraph.setBorder(new SolidBorder(1));
		amountParagraph.setTextAlignment(TextAlignment.LEFT);
		amountParagraph.setVerticalAlignment(VerticalAlignment.MIDDLE);
		amountParagraph.setPaddingLeft(5);
		amountParagraph.setWidth(120);
		amountParagraph.setHeight(40);
		amountParagraph.setFontSize(12);
		
		String thanksString = "Thank you so much for your generous contribution and sustained support to Nilaya Foundation cause.";
		Text thanksText = new Text(thanksString);
		thanksText.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));
		thanksText.setFontSize(11);
		Paragraph thanksParagraph = new Paragraph();
		thanksParagraph.setTextAlignment(TextAlignment.CENTER);
		thanksParagraph.add(thanksText);
		
		String trustPan ="Pan of Trust : AAAAP6998H \n";
		Text panText = new Text(trustPan);


		String exemption = "Exemption U/S 80G of I.T. Act 1961 vide 118/629/CIT-HBL/2008-09 ";
		Text exemptionText = new Text(exemption);

		Paragraph exemParagraph = new Paragraph();
		exemParagraph.setFontSize(9);
		exemParagraph.setTextAlignment(TextAlignment.CENTER);

		exemParagraph.add(exemptionText);
		exemParagraph.add(panText);

		String regards ="Regards,\nNilaya Foundation.";
		Text regardsText = new Text(regards);

		String declaration ="This is computer generated receipt do not require signature.";

		document.add(new Paragraph());
		document.add(new Paragraph());
		document.add(contentParagraph);
		document.add(new Paragraph());
		document.add(amountParagraph);
		document.add(new Paragraph());
		document.add(thanksParagraph);
		document.add(new Paragraph());
		document.add(exemParagraph);
		//document.add(new Paragraph(regardsText).setFontSize(12));
		document.add(new Paragraph(declaration).setFontSize(8).setTextAlignment(TextAlignment.CENTER));
	}

	private static void addToPersonDetails(Document document, String name, String mobileNumber, String emailAddress, String strPAN) throws Exception {

		Text personName = new Text(name+"\n");
		personName.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));

		String mobileNoLabel= "Mobile : "+mobileNumber+"\n";
		Text mobileNumberText = new Text(mobileNoLabel);

		String emailLabel = "E-Mail : "+emailAddress+"\n";
		Text emailText = new Text(emailLabel);

		String panLabel = "PAN : "+strPAN+"\n";
		Text panText = new Text(panLabel);

		Paragraph donorDetails = new Paragraph();
		donorDetails.setTextAlignment(TextAlignment.LEFT);

		donorDetails.add(personName);
		donorDetails.add(panText);
		donorDetails.add(mobileNumberText);
		donorDetails.add(emailText);

		document.add(donorDetails);

	}

	public static void sign(String src, String dest, Certificate[] chain, PrivateKey pk, String digestAlgorithm,
			String provider, PdfSigner.CryptoStandard signatureType, String reason, String location)
					throws GeneralSecurityException, IOException {
		PdfReader reader = new PdfReader(src);
		PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());

		// Create the signature appearance
		Rectangle rect = new Rectangle(36, 648, 200, 100);
		PdfSignatureAppearance appearance = signer.getSignatureAppearance();
		appearance
		.setReason(reason)
		.setLocation(location)

		// Specify if the appearance before field is signed will be used
		// as a background for the signed field. The "false" value is the default value.
		.setReuseAppearance(false)
		.setPageRect(rect)
		.setPageNumber(1);
		signer.setFieldName("sig");

		IExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);
		IExternalDigest digest = new BouncyCastleDigest();

		// Sign the document using the detached mode, CMS or CAdES equivalent.
		signer.signDetached(digest, pks, chain, null, null, null, 0, signatureType);
	}

	private static void setReceiptDetails(Document document) throws IOException {
		Text receiptHeader = new Text("RECEIPT \n");
		receiptHeader.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));
		receiptHeader.setFontSize(18);
		receiptHeader.setFontColor(ColorConstants.BLUE);

		Paragraph receiptParagraph = new Paragraph();
		receiptParagraph.add(receiptHeader);
		receiptParagraph.setTextAlignment(TextAlignment.CENTER);

		String strReceiptNumber = getReceiptNumber();
		Text receiptNumber = new Text(strReceiptNumber);
		receiptNumber.setTextAlignment(TextAlignment.LEFT);
		Paragraph receiptNumberParagraph = new Paragraph();

		receiptNumberParagraph.setTextAlignment(TextAlignment.LEFT);

		String date = getCurrentDate();
		Text dateText = new Text(date);
		dateText.setTextAlignment(TextAlignment.RIGHT);
		Paragraph dateTextParagraph = new Paragraph();

		//dateTextParagraph.setTextAlignment(TextAlignment.RIGHT);

		receiptNumberParagraph.add(receiptNumber);
		receiptNumberParagraph.add(new Tab());

		receiptNumberParagraph.addTabStops(new TabStop(1000, TabAlignment.RIGHT));
		receiptNumberParagraph.add(dateText);

		Text toText = new Text("To,");
		toText.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));
		Paragraph toParagraph = new Paragraph(toText);

		document.add(receiptParagraph);
		document.add(receiptNumberParagraph);
		//document.add(toParagraph);
	}

	private static String getCurrentDate() {

		String returnString="Date: ";
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date today = new Date();

		try {

			returnString=returnString+formatter.format(today);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnString;
	}

	private static String getReceiptNumber() {


		Calendar cal =  Calendar.getInstance();
		String year = String.valueOf(cal.get(Calendar.YEAR));
		String strReceiptNo = String.format("%03d", RECEIPT_NUMBER);
		RECEIPT_NUMBER++;
		return "Receipt No :"+year+"/"+strReceiptNo;
		
	}

	private static void configureHeader(Document document) throws IOException {

		Paragraph header = new Paragraph();
		header.setTextAlignment(TextAlignment.CENTER);
		header.setBorderBottom(new SolidBorder(2)); 

		Text headerText = new Text("NILAYA FOUNDATION\n");
		headerText.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));
		headerText.setFontSize(30);
		//headerText.setFontColor(ColorConstants.BLUE);


		//header.setBackgroundColor(ColorConstants.ORANGE);


		String p = "(Wing of \"Prahlad Vidyarthi Welfare Association\")\n";

		Text description = new Text(p);
		description.setFontSize(10);
		description.setFont(PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD));

		String address =  "Registered under KSR Act 1960, Registration No."
				+ "376/2005 No. 1077, Shri Ram, 4th Cross, 9th Main, Srinivas Nagar, BSK "
				+ "1st Stage, Bengaluru - 560 050.\n Mobile : 94480 64123, 99805 41495 "
				+ "Web: www.nilayafoundation.org Email: nilaya.foundation@gmail.com";
		Text addressText = new Text(address);
		addressText.setFontSize(10);
		addressText.setTextAlignment(TextAlignment.JUSTIFIED);


		header.add(headerText);
		header.add(description);
		header.add(addressText);


		document.add(header);
		//document.add(descParagraph);
	}

	public static String getHtmlContent() throws Exception {

		File file = new File("D:/temp/bill.html");
		BufferedReader bReader = new BufferedReader(new FileReader(file));
		System.out.println("File is there "+bReader.readLine());
		String sLine = null;
		StringBuilder strHTML=new StringBuilder();
		while((sLine=bReader.readLine())!=null) {

			strHTML.append(sLine+"\n");
		}
		return strHTML.toString();
	}
}
