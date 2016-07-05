package com.caitu99.lsp.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class FileNIOCommon {
	
	public static final String UTF_8 = "UTF-8";
	public static final String ISO_8859_1 = "ISO-8859-1";
	public static final String GBK = "GBK";
	
	/**
	 * 读取文件
	 * @param filePath	文件路径
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFileToByte(String filePath) throws IOException{
		File file = new File(filePath);
		FileInputStream in = new FileInputStream(file);
		FileChannel channel = in.getChannel();
		MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		byte[] data = new byte[(int)file.length()];
		int index = 0;
		while(mapped.hasRemaining()){
			data[index++] = mapped.get();
		}

		channel.close();
		in.close();
		return data;
	}
	
	/**
	 * 读取文件
	 * @param filePath	文件路径
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(String filePath) throws IOException{
		return new String(readFileToByte(filePath));
	}
	
	/**
	 * 读取文件
	 * @param filePath	文件路径
	 * @param encoding	解析文件编码
	 * @return
	 * @throws IOException
	 */
	public static String readFileToString(String filePath,String encoding) throws IOException{
		byte[] data = readFileToByte(filePath);
		Charset charset = Charset.forName(encoding);
		CharsetDecoder decoder = charset.newDecoder();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		try {
			return (decoder.decode(buffer).toString());
		} catch (Exception e) {
			System.err.println("字符编码指定错误,无法解析");
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 复制文件内容到另一文件上
	 * @param inputPath		源文件路径
	 * @param outputPath	目标文件路径
	 * @throws IOException
	 */
	public static void writeFileToFile(String inputPath,String outputPath) throws IOException{
		File inputFile = new File(inputPath);
		File outputFile = new File(outputPath);
		FileInputStream in = new FileInputStream(inputFile);
		FileOutputStream out = new FileOutputStream(outputFile);
		FileChannel inChannel = in.getChannel();
		FileChannel outChannel = out.getChannel();
		outChannel.transferFrom(inChannel, 0, inputFile.length());
		
		outChannel.close();
		inChannel.close();
		out.close();
		in.close();
	}
	
	/**
	 * 写入文件
	 * @param path	文件路径
	 * @param content	文件内容 
	 * @param isCoverage	是否覆盖原内容(true为不覆盖)
	 * @throws IOException
	 */
	public static void writeByteToFile(String path,byte[] content,boolean isCoverage) throws IOException{
		File file = new File(path);
		FileOutputStream out = new FileOutputStream(file,isCoverage);
		FileChannel channel = out.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int capacity = buffer.capacity();
		for(int i=0;i<content.length;i++){
			if(capacity == buffer.position()){
				buffer.flip();
				channel.write(buffer);
				buffer.clear();
			}
			buffer.put(content[i]);
		}
		
		buffer.flip();
		channel.write(buffer);
		
		channel.close();
		out.close();
	}
	
	/**
	 * 写入文件
	 * @param path	文件路径
	 * @param content	文件内容 
	 * @param isCoverage	是否覆盖原内容(true为不覆盖)
	 * @throws IOException
	 */
	public static void writeStringToFile(String path,String content,boolean isCoverage) throws IOException{
		writeByteToFile(path,content.getBytes(),isCoverage);
	}
}
