org.apache.commons.net.ftp 的基本操作
------

一. 基本操作

1. FTP 实例化

```JAVA
FTPClient ftpClient = new FTPClient();
```

2. FTP 创建链接

```JAVA
// 代码中, 参数名称需省略, 只保留参数值.
ftpClient.connect(ip = "127.0.0.1", port = 21);
```

3. FTP 用户登录

```JAVA
// 代码中, 参数名称需省略, 只保留参数值.
ftpClient.login(username = "ftp_username", password = "ftp_password");
```

4. 切换到FTP根目录

```JAVA
// 切换到父目录,类似于'cd /'
boolean status = ftpClient.changeToParentDirectory();
return status ? "切换成功" : "切换失败";
```

5. 切换到FTP指定目录

```JAVA
// 代码中, 参数名称需省略, 只保留参数值.
boolean status = ftpClient.changeWorkingDirectory(pathname = "/test");
return status ? "切换成功" : "切换失败";
```

6. 获取指定目录内文件内容

```JAVA
// 查询目录内容(包括文件及文件夹)
FTPFile[] fs = ftpClient.listFiles(pathname = "/test");
// 只获取文件
ArrayList<String> flist = new ArrayList<>();
for (FTPFile f : fs) {
  if (f.isFile()) flist.add(pathname + "/" + f.getName());
}
// 只获取目录
ArrayList<String> dlist = new ArrayList<>();
for (FTPFile f : fs) {
  if (f.isDirectory()) dlist.add(pathname + "/" + f.getName());
}
```

7. 删除文件

```JAVA
// 删除文件, 代码中, 参数名称需省略, 只保留参数值.
boolean status = ftpClient.deleteFile(filename = "/test/1.txt");
return status ? "删除成功" : "删除失败";
```

8. 删除目录

```JAVA
// 删除目录, 代码中, 参数名称需省略, 只保留参数值.
// 当文件夹内有内容时, 删除失败.
boolean status = ftpClient.removeDirectory(pathname = "/test");
return status ? "删除成功" : "删除失败";
```

9. 创建目录

```JAVA
// 创建目录, 代码中, 参数名称需省略, 只保留参数值.
// 当创建多级不存在的文件夹时, 创建失败, 需要一级一级的创建. 可递归创建. 参考代码public boolean makeDirectory(String pathname, boolean cascade) throws IOException.
boolean status = ftpClient.makeDirectory(pathname = "/test");
return status ? "创建成功" : "创建失败";
```

10. 上传文件

```JAVA
// 设置文件类型为二进制文件,便于传输
ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
// 上传数据, 代码中, 参数名称需省略, 只保留参数值.
boolean status = ftpClient.storeFile(filename = "/test/12.txt", inputStream = inputStream);
return status ? "上传成功" : "上传失败";
```

11. 下载文件

```JAVA
// 设置文件类型为二进制文件,便于传输
ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
// 下载数据, 代码中, 参数名称需省略, 只保留参数值.
boolean status = ftpClient.retrieveFile(filename = "/test/12.txt", outputStream = outputStream);
return status ? "下载成功" : "下载失败";
```

12. 获取服务器文件数据流

```JAVA
// 获取服务器文件数据流, 代码中, 参数名称需省略, 只保留参数值.
InputStream is = ftpClient.retrieveFileStream(filename = "/test/12.txt");
// 做一些小事情
// 关闭服务器文件数据流(这两步必不可少且必须按此顺序)
is.close();
ftpClient.completePendingCommand();
```

二. 常见问题

1. listFiles 一直为空.

当FTP SERVER在LINUX下时, 可能会出现ftpClient.listFiles(pathname = "/test");一直为空(不是null). 此时在初始化ftpClient时, 自定义一下UnixFTPEntryParser即可, 代码如下:

```JAVA
// 修改Unix配置,FTP架设在Windows上,可忽略
ftpClient.configure(new FTPClientConfig("com.xyshzh.ftp.client.UnixFTPEntryParser"));
```

查看 [com.xyshzh.ftp.client.UnixFTPEntryParser](src/main/java/com/xyshzh/ftp/client/UnixFTPEntryParser.java) .

2. 获取服务器文件数据流retrieveFileStream之后再操作FTP状态一直是150. 此时关闭retrieveFileStream数据流, 执行一次completePendingCommand即可. 代码如下:
```JAVA
// 获取服务器文件数据流, 代码中, 参数名称需省略, 只保留参数值.
InputStream is = ftpClient.retrieveFileStream(filename = "/test/12.txt");
// 做一些小事情
// 关闭服务器文件数据流(这两步必不可少且必须按此顺序)
is.close();
ftpClient.completePendingCommand();
```

