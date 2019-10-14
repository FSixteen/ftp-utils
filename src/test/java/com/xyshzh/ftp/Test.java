package com.xyshzh.ftp;

import com.xyshzh.ftp.client.FTPClientUtils;
import com.xyshzh.ftp.client.FTPUtils;

/**
 * FTP 测试.
 * @author Shengjun Liu
 * @version 2018-10-02
 */
public class Test {

  public static void main(String[] args) {
    try {
      FTPUtils ftp = new FTPUtils(new FTPClientUtils("127.0.0.1", 21, "ftp", "ftp", true, "/"));
      String[] files = ftp.fileList("/");
      for (String f : files) {
        String fname = f.replace(".tmp", "");
        ftp.renameFile(f, fname);
      }
      ftp.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
