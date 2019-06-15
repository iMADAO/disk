package com.madao.disk;

import com.madao.disk.bean.User;
import com.madao.disk.bean.UserExample;
import com.madao.disk.mapper.UserMapper;
import com.madao.disk.util.KeyUtil;
import com.madao.disk.util.MD5Encoder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.RemoteIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DiskApplicationTests {

	@Test
	public void contextLoads() throws IOException {
		File file = new File("/datadir") ;	// 定义要压缩的文件
		File zipFile = new File("/datadir.zip") ;	// 定义压缩文件名称
		InputStream input = new FileInputStream(file) ;	// 定义文件的输入流
		ZipOutputStream zipOut = null ;	// 声明压缩流对象
		zipOut = new ZipOutputStream(new FileOutputStream(zipFile)) ;
		zipOut.putNextEntry(new ZipEntry(file.getName())) ;	// 设置ZipEntry对象
		zipOut.setComment("压缩流测试") ;	// 设置注释
		int temp = 0 ;
		while((temp=input.read())!=-1){	// 读取内容
			zipOut.write(temp) ;	// 压缩输出
		}
		input.close() ;	// 关闭输入流
		zipOut.close() ;	// 关闭输出流
	}

	@Test
	public void test(){
		zipMultiFile("/datadir", "/datadir.zip", true);
	}

	public static void zipMultiFile(String filepath ,String zippath, boolean dirFlag) {
		try {
			File file = new File(filepath);// 要被压缩的文件夹
			File zipFile = new File(zippath);
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
			if(file.isDirectory()){
				File[] files = file.listFiles();
				for(File fileSec:files){
					if(dirFlag){
						recursionZip(zipOut, fileSec, file.getName() + File.separator);
					}else{
						recursionZip(zipOut, fileSec, "");
					}
				}
			}
			zipOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void recursionZip(ZipOutputStream zipOut, File file, String baseDir) throws Exception{
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(File fileSec:files){
				recursionZip(zipOut, fileSec, baseDir + file.getName() + File.separator);
			}
		}else{
			byte[] buf = new byte[1024];
			InputStream input = new FileInputStream(file);
			zipOut.putNextEntry(new ZipEntry(baseDir + file.getName()));
			int len;
			while((len = input.read(buf)) != -1){
				zipOut.write(buf, 0, len);
			}
			input.close();
		}
	}


	@Autowired
	private UserMapper userMapper;

	@Test
	public void testGetUser() throws IOException {
//		UserExample userExample = new UserExample();
//		userMapper.selectByPrimaryKey(1L);
//		userMapper.selectByExample(userExample)
//		File file = new File("/mnt/disk1/Downloads/CentOS-7-x86_64-DVD-1810.iso");
//		File file2 = new File("/mnt/disk1/Downloads/kali-linux-2018.4-amd64.iso");
//
//
//		System.out.println(Files.size(file.toPath()));
//		System.out.println(Files.size(file2.toPath()));
//
//		System.out.println(file.lastModified());
////		LocalDate localDate = LocalDate.ofEpochDay(file.lastModified());
//		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd hh:mm:ss");
//		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
//		System.out.println(localDateTime);
//		System.out.println(dateTimeFormatter.format(localDateTime));
//		zipMultiFile("/datadir/1559969722232284730/附件", "/datadir/temp/1559969722232284730/附件.zip", true);
		String result = MD5Encoder.getEncryptedWithSalt("root123", "1559969722232284730");
		System.out.println(result);
	}

	@Value("${HDFS_PATH}")
	private String HDFS_PATH;

	@Test
	public void testHDFSPATH(){
		System.out.println(HDFS_PATH);
	}

	@Test
	public void testPath(){
		Path path = Paths.get("root/aa/bb");
		System.out.println(path.subpath(0, 1).toString());
	}

	@Test
	public void testForEach() throws IOException {
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("/root"));
		directoryStream.forEach(x->{
			System.out.println(x.toString());
		});
	}

	@Test
	public void testHDFS() throws URISyntaxException, IOException, InterruptedException {
		 final String HDFS_PATH = "hdfs://127.0.0.1:8020";
		Configuration configuration = new Configuration();
		FileSystem fileSystem = FileSystem.get(new URI(HDFS_PATH), configuration, "root");
		FileStatus[] fileStatuses = fileSystem.listStatus(new org.apache.hadoop.fs.Path("/disk"));
		for(FileStatus fileStatus: fileStatuses){
			String isDir = fileStatus.isDirectory() ? "Directory" : "file";
			//副本数
			short replication = fileStatus.getReplication();
			long len = fileStatus.getLen();
			String path = fileStatus.getPath().toString();
			System.out.println(isDir + "\t" + replication + "\t" + len + "\t" + path );
		}
	}

}
