import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

public class HexToBinConverter {
	
	private final static String[] hexSymbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
	public final static int BITS_PER_HEX_DIGIT = 4;
	
	public static String toHexFromByte(final byte b) {
		byte leftSymbol = (byte)((b >>> BITS_PER_HEX_DIGIT) & 0X0f);
		byte rightSymbol = (byte)(b & 0x0f);
		
		return (hexSymbols[leftSymbol] + hexSymbols[rightSymbol]);
	}
	
	
	public static String toHexFromBytes(final byte[] bytes) {
		if(bytes == null || bytes.length == 0) {
			return ("");
		}
		
		StringBuilder hexBuffer = new StringBuilder(bytes.length * 2);
		
		for(int i =0 ; i < bytes.length; i++) {
			hexBuffer.append(toHexFromByte(bytes[i])); 
		}
		
		return (hexBuffer.toString()); 
	}
	
	public static int findFileCount(String path) {
		int count = 0;
		int flag = 0;
		while(flag == 0) {
			String filePath = path.split("\\.")[0] + "_" + count + "."+path.split("\\.")[1];
			File file = new File(filePath);
			if(file.exists()) {
				count++;
			} else {
				flag =1;
			}
			
		}
		return count;
	}
	
	//매개변수 경로에 있는 파일을 load 하는 함수
	public static String readBase64File(String path) throws IOException {
		String result = "";
		int count = findFileCount(path);
		
		if(count == 0) {
			System.out.println("읽으려는 파일이 없습니다");
			return "";
		}
		List<String> resultList = new ArrayList<String>(); 
		System.out.println("총 "+count+" 개의 파일을 로드했습니다");
		int flag=0;
		for(int i=0;i<=count;i++) {
			File file = new File(path.split("\\.")[0] + "_" + i + "."+path.split("\\.")[1]);
			if(file.exists()) {
				System.out.println((i+1)+"/"+count+" 번째 파일을 읽는중...");
				BufferedReader br = new BufferedReader(new FileReader(file));
				String sLine = null;
				
				while((sLine = br.readLine()) != null) {
					byte[] decoded = DatatypeConverter.parseBase64Binary(sLine);
					resultList.add(toHexFromBytes(decoded));
				}
			}
		}
		System.out.println(resultList.size()+"개의 데이터가 로드되었습니다");
		StringBuilder str = new StringBuilder();
		for(int i=0;i<resultList.size();i++) {
			System.out.println((i+1)+"/"+resultList.size()+"개 Hex데이터 로드 완료");
			str.append(resultList.get(i));
		}
		return str.toString();
	}
	
	public static void readFile2Base64Data(String path, String outputFilePath, int readPartSize, int writePartSize) throws IOException {
		FileInputStream fis = new FileInputStream(new File(path));
		
		String base64Txt = "";
		byte[] bytes = new byte[readPartSize];
		int value = 0;
		int step = 0;
		long beforeTime = System.currentTimeMillis();
		
		do {
			value=fis.read(bytes);
			base64Txt += DatatypeConverter.printBase64Binary(bytes)+"\n";
			if(base64Txt.length() > writePartSize) {
				FileOutputStream hex = new FileOutputStream(outputFilePath.split("\\.")[0]+"_"+step+"."+outputFilePath.split("\\.")[1]);
				hex.write(base64Txt.getBytes());
				hex.flush();
				hex.close();
				step++;
				base64Txt = "";
				System.out.println((step)+"번째 파일 저장 완료");
			}
		} while(value != -1);
		
		FileOutputStream hex2 = new FileOutputStream(outputFilePath.split("\\.")[0]+"_"+step+"."+outputFilePath.split("\\.")[1]);
		hex2.write(base64Txt.getBytes());
		hex2.flush();
		hex2.close();
		System.out.println("총 "+step+"개의 파일을 저장했습니다.");
		long afterTime = System.currentTimeMillis();
		long useTime = (afterTime - beforeTime) / 1000;
		System.out.println(writePartSize+" 개 조각으로 나눌때 걸린 시간 :"+useTime);
	}
	
	public static void makeHexToFile(String encodeTxt, String outputFilePath) throws IOException {
		if((encodeTxt.length() % 2) != 0) 
			throw new IllegalArgumentException("파일이 손상되었습니다");
		
		final byte result[] = new byte[encodeTxt.length()/2];
		final char enc[] = encodeTxt.toCharArray();
		System.out.print("\n데이터를 묶는중입니다.");
		for (int x=0;x<enc.length;x+=2) {
			StringBuilder curr = new StringBuilder(2);
			curr.append(enc[x]).append(enc[x+1]);
			result[x/2] = (byte)Integer.parseInt(curr.toString(), 16);
			if(x%50000000 == 0) {
				System.out.print(".");
			}
		}
		FileOutputStream fos = new FileOutputStream(outputFilePath); 
		fos.write(result);
		fos.flush();
		fos.close();
		System.out.println("\n데이터 묶기 완료!");
	}
	
	public static void jobEnded() {
		System.out.println("작업이 종료되었습니다");
	}
	public static void jobStarted() {
		System.out.println("작업이 시작되었습니다.");
	}
	
	public static void main(String[] args) {
		try {
			String readFile = "";
			String outputFile ="";
			String middleFile ="";
			String flag = args[0];
			String read ="";
			int readPartSize = 300000;
			int writePartSize = 1000000;
			if(args.length == 3) { //encode 또는 decode
				if("encode".equalsIgnoreCase(flag) || "decode".equalsIgnoreCase(flag)) {
					readFile = args[1];
					outputFile = args[2];
				} 
			} else if (args.length == 4) { //test 일때
				if("test".equalsIgnoreCase(flag)) {
					readFile = args[1];
					middleFile = args[2];
					outputFile = args[3];
				} 
			} else if (args.length == 5) { //사이즈까지 전달할때
				if("encode".equalsIgnoreCase(flag)) {
					readFile = args[1];
					outputFile = args[2];
					readPartSize = Integer.parseInt(args[3]);
					writePartSize = Integer.parseInt(args[4]);
				} 
			} else if (args.length == 6) { //test에 사이즈까지 전달할때
				if("test".equalsIgnoreCase(flag)) {
					readFile = args[1];
					middleFile = args[2];
					outputFile = args[3];
					readPartSize = Integer.parseInt(args[4]);
					writePartSize = Integer.parseInt(args[5]);
				}
			} else {
			
			}
			
			if("test".equalsIgnoreCase(flag)) {
				jobStarted();
				readFile2Base64Data(readFile, middleFile, readPartSize, writePartSize);
				read = readBase64File(middleFile);
				makeHexToFile(read, outputFile);
				jobEnded();
			} else if("encode".equalsIgnoreCase(flag)) {
				jobStarted();
				readFile2Base64Data(readFile, outputFile, readPartSize, writePartSize);
				jobEnded();
				
			} else if("decode".equalsIgnoreCase(flag)) {
				jobStarted();
				read = readBase64File(readFile);
				makeHexToFile(read, outputFile);				
				jobEnded();
			} else {
				System.out.println("==================== 사용법 =======================");
				System.out.println("test [파일경로] [인코딩데이터저장경로] [아웃풋파일경로]");
				System.out.println("encode [파일경로] [인코딩데이터저장경로]");
				System.out.println("decode [인코딩데이터저장경로] [아웃풋파일경로]");
				System.out.println("test [파일경로] [인코딩데이터저장경로] [아웃풋파일경로] [읽기1개조각수] [쓰기1개조각수]");
				System.out.println("encode [파일경로] [인코딩데이터저장경로] [읽기1개조각수] [쓰기1개조각수]");
				System.out.println("decode [인코딩데이터저장경로] [아웃풋파일경로] [읽기1개조각수] [쓰기1개조각수]");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
