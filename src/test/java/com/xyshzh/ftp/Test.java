package com.xyshzh.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
      FTPUtils ftp = new FTPUtils(new FTPClientUtils("192.168.0.66", 21, "ftp", "ftp", true, "/").getFtpClient());
      while (true) {
        String[] fs = ftp.fileList("/");
        if (0 < fs.length) {
          for (String f : fs) {
            InputStream retrieve = ftp.retrieveFileStream(f);
            InputStreamReader i = new InputStreamReader(retrieve, "UTF-8");
            BufferedReader bi = new BufferedReader(i);
            System.out.println(bi.readLine());
            bi.close();
            i.close();
            retrieve.close();
            ftp.getFtpClient().completePendingCommand();
            ftp.deleteFile(f);
            System.out.println("----");
          }
        } else {
          System.out.println("sleep 5s !");
          try {
            Thread.sleep(5 * 1000L);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    } catch (IllegalArgumentException | IOException e) {
      e.printStackTrace();
    }
  }

}
