package com.xyshzh.ftp.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP 操作.
 * @author Shengjun Liu
 * @version 2018-10-02
 */
public class FTPUtils {
  private Logger log = Logger.getLogger(this.getClass().getName());

  private FTPClientUtils ftpClientUtils;

  private FTPClient ftpClient;

  /**
   * @param ftpClientUtils FTP 客户端工具
   */
  public FTPUtils(FTPClientUtils ftpClientUtils) {
    this.ftpClientUtils = ftpClientUtils;
    this.ftpClient = this.ftpClientUtils.getFtpClient();
  }

  /**
   * @param ftpClientUtils FTP 客户端工具
   * @param ftpClient FTP链接实例
   */
  public FTPUtils(FTPClientUtils ftpClientUtils, FTPClient ftpClient) {
    this.ftpClientUtils = ftpClientUtils;
    this.ftpClient = ftpClient;
  }

  public FTPClient getFtpClient() {
    return ftpClient;
  }

  public void setFtpClient(FTPClient ftpClient) {
    this.ftpClient = ftpClient;
  }

  /**
   * FTP 重链.
   * @throws ConnectException
   * @throws IllegalArgumentException
   * @throws SocketException
   * @throws IOException
   */
  public void reconnect() throws ConnectException, IllegalArgumentException, SocketException, IOException {
    this.ftpClientUtils.reconnect();
    log.info("ftpClientUtils 重新链接完成."); // 提示
    this.ftpClient = this.ftpClientUtils.getFtpClient();
    log.info("ftpClient 重新获取完成."); // 提示
  }

  /**
   * 获取服务器内某一文件的数据流.
   * @param filename 文件[路径及]名称
   * @return
   * @throws IOException 
   */
  public InputStream retrieveFileStream(String filename) throws IOException {
    checkConnect();
    return this.ftpClient.retrieveFileStream(filename);
  }

  /**
   * 获取服务器内某一文件的数据流.
   * @param filename 文件[路径及]名称
   * @return
   * @throws IOException 
   */
  public List<String> retrieve2StringArray(String filename) throws IOException {
    checkConnect();
    InputStream retrieve = this.ftpClient.retrieveFileStream(filename);
    InputStreamReader i = new InputStreamReader(retrieve, "UTF-8");
    BufferedReader bi = new BufferedReader(i);
    String temp = null;
    List<String> list = new ArrayList<>();
    while ((temp = bi.readLine()) != null) {
      list.add(temp);
    }
    bi.close();
    i.close();
    retrieve.close();
    this.ftpClient.completePendingCommand();
    log.info("文件记录总数:  " + list.size() + "  在文件:  " + filename); // 提示
    return list;
  }

  /**
   * 判断文件或目录是否存在.
   * true: 存在, false: 不存在.
   * @param filepath 文件或目录地址.
   * @return
   * @throws IOException
   */
  public boolean isExsits(String filepath) throws IOException {
    if (null == filepath) { return false; }
    checkConnect();
    if ("/".equals(filepath)) { return true; }
    /** 先尝试目录 */
    if (this.ftpClient.changeWorkingDirectory(filepath)) {
      this.ftpClient.changeToParentDirectory();
      return true;
    } else {
      if (filepath.endsWith("/")) { return false; }
    }
    /** 再尝试文件 */
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    this.ftpClient.enterLocalPassiveMode();
    int index = filepath.lastIndexOf("/");
    String path = -1 < index ? filepath.substring(0, index + 1) : "/";
    String name = -1 < index ? filepath.substring(index + 1) : filepath;
    FTPFile[] fs = this.ftpClient.listFiles(path); // 查询目录内容
    if (null == fs || 0 == fs.length) {
      return false;
    } else {
      for (FTPFile f : fs) {
        if (name.equals(f.getName())) { return true; }
      }
    }
    return false;
  }

  public boolean renameFile(String srcFname, String targetFname) throws IOException {
    if (null == srcFname || null == targetFname) { return false; }
    checkConnect();
    try {
      return this.ftpClient.rename(srcFname, targetFname);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 获取服务器目录内目录内容.
   * @param pathname 文件目录
   * @return
   * @throws IOException 
   */
  public String[] directoryList(String pathname) throws IOException {
    checkConnect();
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    FTPFile[] fs = this.ftpClient.listFiles(pathname); // 查询目录内容
    ArrayList<String> dlist = new ArrayList<>();
    for (FTPFile f : fs) {
      if (f.isDirectory()) dlist.add(pathname + (pathname.endsWith("/") ? "" : "/") + f.getName());
    }
    log.info("目录总数:  " + dlist.size() + "  在目录:  " + pathname); // 提示
    return dlist.toArray(new String[dlist.size()]);
  }

  /**
   * 获取服务器目录内文件内容.
   * @param pathname 文件目录
   * @return
   * @throws IOException 
   */
  public String[] fileList(String pathname) throws IOException {
    checkConnect();
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    this.ftpClient.enterLocalPassiveMode();
    FTPFile[] fs = this.ftpClient.listFiles(pathname); // 查询目录内容
    ArrayList<String> flist = new ArrayList<>();
    for (FTPFile f : fs) {
      if (f.isFile()) flist.add(pathname + (pathname.endsWith("/") ? "" : "/") + f.getName());
    }
    log.info("文件总数:  " + flist.size() + "  在目录:  " + pathname); // 提示
    return flist.toArray(new String[flist.size()]);
  }

  /**
   * 下载服务器根数据到本地.</br>
   * @param filename 服务器文件绝对或相对路径
   * @param localfilename 本地保存文件绝对或相对路径
   * @return
   * @throws IOException 
   */
  public boolean downloadFile(String filename, String localfilename) throws IOException {
    checkConnect();
    this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型为二进制文件,便于传输
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    OutputStream is = new FileOutputStream(new File(localfilename)); // 创建输出流
    boolean status = this.ftpClient.retrieveFile(filename, is); // 开始下载
    is.close(); // 关闭输出流
    log.info((status ? "文件下载完成:  " : "文件下载失败:  ") + filename); // 提示
    return status;
  }

  /**
   * 上传数据到服务器根目录.</br>
   * @param filename 上传文件名称
   * @param localfilename 本地文件绝对或相对路径
   * @return
   * @throws FileNotFoundException 
   * @throws IOException 
   */
  public boolean uploadFile(String filename, String localfilename) throws FileNotFoundException, IOException {
    return uploadFile("/", filename, new FileInputStream(new File(localfilename)), false);
  }

  /**
   * 上传数据到服务器.</br>
   * 第一步: 上传数据前首先需要切换上传文件目录,切换失败会检测是否自动创建目录.</br>
   * 第二部: 重试切换上传文件目录,如果切换成功,则上传数据,否则退出方法.</br>
   * @param pathname 上传文件目录
   * @param filename 上传文件名称
   * @param inputStream 数据流
   * @param mkdir 自动创建目录
   * @return
   * @throws IOException 
   */
  public boolean uploadFile(String pathname, String filename, InputStream inputStream, boolean mkdir) throws IOException {
    checkConnect();
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    if (!this.ftpClient.changeWorkingDirectory(pathname) && mkdir) makeDirectory(pathname, mkdir); // 如果上传文件目录不存在,则根据mkdir执行创建文件目录
    if (this.ftpClient.changeWorkingDirectory(pathname)) {
      this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型为二进制文件,便于传输
      boolean status = this.ftpClient.storeFile(filename, inputStream); // 上传数据
      inputStream.close(); // 关闭数据流
      log.info((status ? "文件上传完成:  " : "文件上传失败:  ") + (pathname + (pathname.endsWith("/") ? "" : "/") + filename)); // 提示
      return status;
    } else {
      log.info("上传文件目录不存在:  " + (pathname + (pathname.endsWith("/") ? "" : "/") + filename)); // 提示
      return false;
    }
  }

  /**
   * 下载服务器根数据到本地.</br>
   * @param remotePath 服务器绝对路径
   * @param localPath 本地绝对路径
   * @param deleteRemoteData 删除服务器历史文件
   * @return
   * @throws IOException 
   */
  public boolean downloadDirectory(String remotePath, String localPath, boolean deleteRemoteData) throws IOException {
    if (!remotePath.endsWith("/")) {
      remotePath = remotePath + "/";
    }
    if (!localPath.endsWith("/")) {
      localPath = localPath + "/";
    }
    checkConnect();
    this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型为二进制文件,便于传输
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    /** 先尝试目录 */
    if (this.ftpClient.changeWorkingDirectory(remotePath)) {
      FTPFile[] files = this.ftpClient.listFiles(remotePath);
      log.info("请求文件内容总数 :: " + (null != files ? files.length : 0));
      if (null != files && 0 < files.length) {
        log.info("开始处理文件内容 ...... ");
        for (FTPFile file : files) {
          if (file.isFile()) {
            String rfn = remotePath + file.getName();
            String lfn = localPath + file.getName();
            log.info("文件处理中 :: " + rfn + "  -->  " + lfn);
            OutputStream is = new FileOutputStream(new File(lfn + ".temp")); // 创建输出流
            boolean status = this.ftpClient.retrieveFile(rfn, is); // 开始下载
            is.close(); // 关闭输出流
            new File(lfn + ".temp").renameTo(new File(lfn));
            log.info("文件下载结果 :: " + rfn + "  -->  " + lfn + "  ::  " + status);
            if (status && deleteRemoteData) { // 删除远程文件
              boolean deleted = this.ftpClient.deleteFile(remotePath + file.getName());
              log.info("文件删除结果 :: " + rfn + "  ::  " + deleted);
            }
          } else {
            if (!".".equals(file.getName()) && !"./".equals(file.getName()) && !"..".equals(file.getName()) && !"../".equals(file.getName())) {
              String rfn = remotePath + file.getName() + "/";
              String lfn = localPath + file.getName() + "/";
              log.info("目录处理中 :: " + lfn);
              File f = new File(localPath + file.getName() + "/");
              if (f.exists() || f.mkdirs()) {
                boolean status = this.downloadDirectory(rfn, lfn, deleteRemoteData);
                if (status && deleteRemoteData) { // 删除远程文件
                  this.ftpClient.removeDirectory(remotePath + file.getName());
                }
              }
            }
          }
        }
      }
    } else {
      return false;
    }
    return true;
  }

  /**
   * 在服务器创建文件目录.</br>
   * 如果被创建文件目录已存在,则创建失败.</br>
   * 如果被创建文件目录为多级目录,最后一级前的目录尚未创建,则创建失败.此时相当于{@code mkdir pathname}</br>
   * 此方法相当于{@link #makeDirectory(String pathname, boolean cascade) makeDirectory(String pathname, false)}</br>
   * @param pathname 文件目录
   * @return
   * @throws IOException 
   */
  public boolean makeDirectory(String pathname) throws IOException {
    return makeDirectory(pathname, false);
  }

  /**
   * 在服务器创建文件目录.</br>
   * 如果被创建文件目录已存在,则创建失败.</br>
   * 当cascade=false时,如果被创建文件目录为多级目录,最后一级前的目录尚未创建,则创建失败.此时相当于{@link #makeDirectory(String pathname) makeDirectory(String pathname)}</br>
   * 当cascade=true时,如果被创建文件目录为多级目录,最后一级前的目录尚未创建,则遍历目录进项创建.此时相当于{@code mkdir -p pathname}</br>
   * @param pathname 文件目录
   * @param cascade 级联创建
   * @return
   * @throws IOException 
   */
  public boolean makeDirectory(String pathname, boolean cascade) throws IOException {
    checkConnect();
    if (this.ftpClient.changeWorkingDirectory(pathname)) { // 切换目录成功说明目录已存在
      log.info("目录已存在:  " + pathname);
      return false;
    }
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    for (String dir : pathname.split("/")) {
      if ("".equals(dir)) continue; // '/123/456'执行split后,第一个是''
      if (!ftpClient.changeWorkingDirectory(dir)) { // 切换目录失败说明目录不存在,目录不存在则创建进入,存在则直接进入
        if (ftpClient.makeDirectory(dir) && ftpClient.changeWorkingDirectory(dir)) { // 创建目录且进入
          String[] rt = ftpClient.doCommandAsStrings("pwd", null); // 获取当前目录位置
          // -- 1 --
          // java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"(.*?)\"").matcher(rt[0]);
          // if (m.find()) log.info("已创建目录:  " + m.group(0).replace("\"", "")); // 提示当前创建目录位置
          // -- 2 --
          log.info("已创建目录:  " + rt[0].replace("\"", "")); // 提示当前创建目录位置
          // -- 1 | 2 保留其一即可 --
        } else {
          return false; // 创建过程中出现问题,退出
        }
      } else {
        log.info("目录已存在:  " + dir);
      }
    }
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    return true;
  }

  /**
   * 删除服务器文件目录.</br>
   * 如果被删除文件目录非空目录,则删除失败.</br>
   * 此方法相当于{@link #deleteDirectory(String pathname, boolean cascade) deletePath(String pathname, false)}</br>
   * @param pathname 文件目录
   * @return
   * @throws IOException 
   */
  public boolean deleteDirectory(String pathname) throws IOException {
    return deleteDirectory(pathname, false);
  }

  /**
   * 删除服务器文件目录.</br>
   * pathname末尾可能包含反斜线,例如:/123/或/123皆可.</br>
   * 当cascade=false时,如果被删除文件目录非空目录,则删除失败.此时相当于{@link #deleteDirectory(String pathname) deletePath(String pathname)}</br>
   * 当cascade=true时,如果被删除文件目录非空目录,则遍历删除文件目录内所有内容,再删除目录.</br>
   * @param pathname 文件目录
   * @param cascade 级联删除
   * @return
   * @throws IOException 
   */
  public boolean deleteDirectory(String pathname, boolean cascade) throws IOException {
    checkConnect();
    if (cascade) { // 如果级联删除,进入if代码块
      this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
      this.ftpClient.changeWorkingDirectory(pathname); // 切换目录
      FTPFile[] fs = this.ftpClient.listFiles(pathname); // 获取目录内容文件内容
      for (FTPFile f : fs) { // 迭代文件内容
        if (f.isFile()) { // 是文件直接删除
          if (this.ftpClient.deleteFile(f.getName())) { // 删除文件
            log.info("已删除文件:  " + pathname + (pathname.endsWith("/") ? "" : "/") + f.getName()); // 提示
          }
        } else if (f.isDirectory()) { // 是目录,则迭代继续删除
          this.deleteDirectory(pathname + (pathname.endsWith("/") ? "" : "/") + f.getName(), cascade);
        } else {
          throw new IllegalArgumentException("未知数据类型.");
        }
      }
    }
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    boolean status = this.ftpClient.removeDirectory(pathname);
    log.info((status ? "已删除目录:  " : "目录删除失败:  ") + pathname);
    return status;
  }

  /**
   * 删除服务器文件.
   * @param filename 文件[路径及]名称
   * @return
   * @throws IOException 
   */
  public boolean deleteFile(String filename) throws IOException {
    return deleteFile(null, filename);
  }

  /**
   * 删除服务器文件.
   * @param pathname 文件目录
   * @param filename 文件名称
   * @return
   * @throws IOException 
   */
  public boolean deleteFile(String pathname, String filename) throws IOException {
    if (null == filename) { return false; }
    checkConnect();
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    if (null != pathname && !this.ftpClient.changeWorkingDirectory(pathname)) return false; // 如果有指定目录则先切换目录,切换失败,退出方法
    boolean status = this.ftpClient.deleteFile(filename); // 删除文件
    log.info((status ? "已删除文件:  " : "文件删除失败:  ") + filename + (null == pathname ? "" : (" 在目录 " + pathname + " 中 ")));
    this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
    return status;
  }

  /**
   * 检查FTP登录正常状态.
   * @throws RuntimeException
   * @throws IOException 
   */
  public void checkConnect() throws RuntimeException, IOException {
    int replyCode = this.ftpClient.pwd();
    if (!FTPReply.isPositiveCompletion(replyCode)) { throw new RuntimeException("FTP服务器登录失败,错误代码:" + replyCode); }
  }

  /**
   * 关闭FTP链接.
   */
  public void close() {
    try {
      if (null != this.ftpClient && this.ftpClient.isConnected()) {
        // 退出登录
        this.ftpClient.logout();
        // 断开链接
        this.ftpClient.disconnect();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // 置空
      this.ftpClient = null;
    }
  }

}
