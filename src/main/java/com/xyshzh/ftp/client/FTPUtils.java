package com.xyshzh.ftp.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

  private FTPClient ftpClient;

  /**
   * @param ftpClient FTP链接实例
   */
  public FTPUtils(FTPClient ftpClient) {
    this.ftpClient = ftpClient;
  }

  public FTPClient getFtpClient() {
    return ftpClient;
  }

  public void setFtpClient(FTPClient ftpClient) {
    this.ftpClient = ftpClient;
  }

  /**
   * 获取服务器内某一文件的数据流.
   * @param filename 文件[路径及]名称
   * @return
   */
  public InputStream retrieveFileStream(String filename) {
    try {
      checkConnect();
      return this.ftpClient.retrieveFileStream(filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 获取服务器目录内目录内容.
   * @param pathname 文件目录
   * @return
   */
  public String[] directoryList(String pathname) {
    try {
      checkConnect();
      this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
      FTPFile[] fs = this.ftpClient.listFiles(pathname); // 查询目录内容
      ArrayList<String> flist = new ArrayList<>();
      for (FTPFile f : fs) {
        if (f.isDirectory()) flist.add(pathname + (pathname.endsWith("/") ? "" : "/") + f.getName());
      }
      return flist.toArray(new String[flist.size()]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new String[] {};
  }

  /**
   * 获取服务器目录内文件内容.
   * @param pathname 文件目录
   * @return
   */
  public String[] fileList(String pathname) {
    try {
      checkConnect();
      this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
      this.ftpClient.enterLocalPassiveMode();
      FTPFile[] fs = this.ftpClient.listFiles(pathname); // 查询目录内容
      ArrayList<String> flist = new ArrayList<>();
      for (FTPFile f : fs) {
        if (f.isFile()) flist.add(pathname + (pathname.endsWith("/") ? "" : "/") + f.getName());
      }
      return flist.toArray(new String[flist.size()]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new String[] {};
  }

  /**
   * 下载服务器根数据到本地.</br>
   * @param filename 服务器文件绝对或相对路径
   * @param localfilename 本地保存文件绝对或相对路径
   * @return
   */
  public boolean downloadFile(String filename, String localfilename) {
    try {
      checkConnect();
      this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型为二进制文件,便于传输
      this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
      OutputStream is = new FileOutputStream(new File(localfilename)); // 创建输出流
      boolean status = this.ftpClient.retrieveFile(filename, is); // 开始下载
      is.close(); // 关闭输出流
      log.info((status ? "文件下载完成:  " : "文件下载失败:  ") + filename); // 提示
      return status;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 上传数据到服务器根目录.</br>
   * @param filename 上传文件名称
   * @param localfilename 本地文件绝对或相对路径
   * @return
   */
  public boolean uploadFile(String filename, String localfilename) {
    try {
      return uploadFile("/", filename, new FileInputStream(new File(localfilename)), false);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return false;
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
   */
  public boolean uploadFile(String pathname, String filename, InputStream inputStream, boolean mkdir) {
    try {
      checkConnect();
      this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型为二进制文件,便于传输
      this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
      if (!this.ftpClient.changeWorkingDirectory(pathname) && mkdir) makeDirectory(pathname, mkdir); // 如果上传文件目录不存在,则根据mkdir执行创建文件目录
      if (this.ftpClient.changeWorkingDirectory(pathname)) {
        boolean status = this.ftpClient.storeFile(filename, inputStream); // 上传数据
        inputStream.close(); // 关闭数据流
        log.info((status ? "文件上传完成:  " : "文件上传失败:  ") + (pathname + (pathname.endsWith("/") ? "" : "/") + filename)); // 提示
        return status;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 在服务器创建文件目录.</br>
   * 如果被创建文件目录已存在,则创建失败.</br>
   * 如果被创建文件目录为多级目录,最后一级前的目录尚未创建,则创建失败.此时相当于{@code mkdir pathname}</br>
   * 此方法相当于{@link #makeDirectory(String pathname, boolean cascade) makeDirectory(String pathname, false)}</br>
   * @param pathname 文件目录
   * @return
   */
  public boolean makeDirectory(String pathname) {
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
   */
  public boolean makeDirectory(String pathname, boolean cascade) {
    try {
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
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 删除服务器文件目录.</br>
   * 如果被删除文件目录非空目录,则删除失败.</br>
   * 此方法相当于{@link #deleteDirectory(String pathname, boolean cascade) deletePath(String pathname, false)}</br>
   * @param pathname 文件目录
   * @return
   */
  public boolean deleteDirectory(String pathname) {
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
   */
  public boolean deleteDirectory(String pathname, boolean cascade) {
    try {
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
      if (this.ftpClient.removeDirectory(pathname)) {
        log.info("已删除目录:  " + pathname);
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 删除服务器文件.
   * @param filename 文件[路径及]名称
   * @return
   */
  public boolean deleteFile(String filename) {
    return deleteFile(null, filename);
  }

  /**
   * 删除服务器文件.
   * @param pathname 文件目录
   * @param filename 文件名称
   * @return
   */
  public boolean deleteFile(String pathname, String filename) {
    try {
      checkConnect();
      this.ftpClient.changeToParentDirectory(); // 切换到父目录,类似于'cd /'
      if (null != pathname && this.ftpClient.changeWorkingDirectory(pathname)) return false; // 如果有指定目录则先切换目录,切换失败,退出方法
      boolean status = this.ftpClient.deleteFile(filename); // 删除文件
      log.info((status ? "已删除文件:  " : "文件删除失败:  ") + ((null != pathname ? pathname : "") + filename).replace("//", "/").replace("//", "/"));
      return status;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 检查FTP登录正常状态.
   * @throws RuntimeException
   */
  public void checkConnect() throws RuntimeException {
    int replyCode = this.ftpClient.getReplyCode();
    if (!FTPReply.isPositiveCompletion(replyCode)) { throw new RuntimeException("FTP服务器登录失败,错误代码:" + replyCode); }
  }

}
