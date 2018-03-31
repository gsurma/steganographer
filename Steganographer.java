import java.io.*;
import java.util.Scanner;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferByte;

// Simple java script allowing hiding or revealing data in image files using Least Significant Bit algorithm.
public class Steganographer {

	private static int bytesForTextLengthData = 4;
	private static int bitsInByte = 8;

	public static void main(String[] args) {
		if (args.length > 0) {
			if (args.length == 1) {
				if (args[0].equals("--help")) {
					System.out.println("");
					System.out.println("-- STEGANOGRAPHER --");
					System.out.println("");
					System.out.println("Hide or reveal data in images!");
					System.out.println("");
					System.out.println("For encode mode provide two arguments as specified below:");
					System.out.println("java Steganographer <path_to_container_image> <path_to_message_text_file>");
					System.out.println("");
					System.out.println("For decode mode provide only one argument as specified below:");
					System.out.println("java Steganographer <path_to_image_with_hidden_message>");
					System.out.println("");
					return;
				} else {
					decode(args[0]);
					return;
				}
			} else if (args.length == 2) {
				encode(args[0], args[1]);
				return;
			} 
		} 
		System.out.println("Wrong input. Use '--help' option for more information.");
	}


	// Encode

	private static void encode(String imagePath, String textPath) {
		BufferedImage originalImage = getImageFromPath(imagePath);
		BufferedImage imageInUserSpace = getImageInUserSpace(originalImage);
		String text = getTextFromTextFile(textPath);

		byte imageInBytes[] = getBytesFromImage(imageInUserSpace);
		byte textInBytes[] = text.getBytes();
		byte textLengthInBytes[] = getBytesFromInt(textInBytes.length);
		try {
			encodeImage(imageInBytes, textLengthInBytes,  0); 
			encodeImage(imageInBytes, textInBytes, bytesForTextLengthData*bitsInByte);
		}
		catch (Exception exception) {
			System.out.println("Couldn't hide text in image. Error: " + exception);
			return;
		}

		String fileName = imagePath;
		int position = fileName.lastIndexOf(".");
		if (position > 0) {
			fileName = fileName.substring(0, position);
		}
		
		String finalFileName = fileName + "_with_hidden_message.png";
		System.out.println("Successfully encoded text in: " + finalFileName);
		saveImageToPath(imageInUserSpace, new File(finalFileName),"png");
		return;
	}

	private static byte[] encodeImage(byte[] image, byte[] addition, int offset) {
		if (addition.length + offset > image.length) {
			throw new IllegalArgumentException("Image file is not long enough to store provided text");
		}
		for (int i=0; i<addition.length; i++) {
			int additionByte = addition[i];
			for (int bit=bitsInByte-1; bit>=0; --bit, offset++) { 
				int b = (additionByte >>> bit) & 0x1;
				image[offset] = (byte)((image[offset] & 0xFE) | b); 
			}
		}
		return image;
	}


	// Decode

	private static String decode(String imagePath) {
		byte[] decodedHiddenText;
		try {
			BufferedImage imageFromPath = getImageFromPath(imagePath);
			BufferedImage imageInUserSpace = getImageInUserSpace(imageFromPath);
			byte imageInBytes[] = getBytesFromImage(imageInUserSpace);
			decodedHiddenText = decodeImage(imageInBytes);
			String hiddenText = new String(decodedHiddenText);
			String outputFileName = "hidden_text.txt";
			saveTextToPath(hiddenText, new File(outputFileName));
			System.out.println("Successfully extracted text to: " + outputFileName);
			return hiddenText;
		} catch (Exception exception) {
			System.out.println("No hidden message. Error: " + exception);
			return "";
		}
	}

	private static byte[] decodeImage(byte[] image) {
		int length = 0;
		int offset  = bytesForTextLengthData*bitsInByte;

		for (int i=0; i<offset; i++) {
			length = (length << 1) | (image[i] & 0x1);
		}
		
		byte[] result = new byte[length];

		for (int b=0; b<result.length; b++ ) {
			for (int i=0; i<bitsInByte; i++, offset++) {
				result[b] = (byte)((result[b] << 1) | (image[offset] & 0x1));
			}
		}
		return result;
	}


	// File I/O methods

	private static void saveImageToPath(BufferedImage image, File file, String extension) {
		try {
			file.delete(); 
			ImageIO.write(image, extension, file);
		} catch (Exception exception) {
			System.out.println("Image file could not be saved. Error: " + exception);
		}
	}

	private static void saveTextToPath(String text, File file) {
		try {	
			if (file.exists() == false) {
				file.createNewFile( );
			}
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(text);
			bufferedWriter.close();
		} catch (Exception exception) {
			System.out.println("Couldn't write text to file: " + exception);
		}
	}

	private static BufferedImage getImageFromPath(String path) {
		BufferedImage image	= null;
		File file = new File(path);
		try {
			image = ImageIO.read(file);
		} catch (Exception exception) {
			System.out.println("Input image cannot be read. Error: " + exception);
		}
		return image;
	}

	private static String getTextFromTextFile(String textFile) {
		String text = "";
		try {
			Scanner scanner = new Scanner( new File(textFile) );
			text = scanner.useDelimiter("\\A").next();
			scanner.close();
		} catch (Exception exception) {
			System.out.println("Couldn't read text from file. Error: " + exception);
		}
		return text;
	}


	// Helpers

	private static BufferedImage getImageInUserSpace(BufferedImage image) {
		BufferedImage imageInUserSpace  = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = imageInUserSpace.createGraphics();
		graphics.drawRenderedImage(image, null);
		graphics.dispose(); 
		return imageInUserSpace;
	}

	private static byte[] getBytesFromImage(BufferedImage image) {
		WritableRaster raster = image.getRaster();
		DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
		return buffer.getData();
	}

	private static byte[] getBytesFromInt(int integer) {
		return ByteBuffer.allocate(bytesForTextLengthData).putInt(integer).array();
	}
}
