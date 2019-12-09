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
      FTPUtils ftp = new FTPUtils(new FTPClientUtils("218.58.210.228", 10021, "csrd_1", "csrd", true, "/"));
      String[] files = ftp.fileList("/");
      for (String f : files) {
        if (f.endsWith(".tmp")) {
          String fname = f.replace(".tmp", "");
          ftp.renameFile(f, fname);
        }
      }
      ftp.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
