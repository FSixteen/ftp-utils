package com.xyshzh.ftp;

import java.io.IOException;
import java.util.List;

import com.xyshzh.ftp.client.FTPClientUtils;
import com.xyshzh.ftp.client.FTPUtils;

/**
 * FTP 测试.
 * @author Shengjun Liu
 * @version 2018-10-02
 */
public class Test {

  public static void main(String[] args) {
    FTPUtils ftp = null;
    while (true) {
      try {
        ftp = new FTPUtils(new FTPClientUtils("192.168.0.66", 21, "ftp", "ftp", true, "/"));
        while (true) {
          // 一些操作......
          String[] fs = ftp.fileList("/");
          if (0 < fs.length) {
            for (String f : fs) {
              List<String> list = ftp.retrieve2StringArray(f);
              for (String string : list) {
                System.out.println(string);
              }
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
        System.out.println("sleep 5s !");
        try {
          Thread.sleep(5 * 1000L); // 睡眠5S后重链.
          if (null != ftp) ftp.reconnect(); // 出现异常,如果已经初始化FTP,说明可能是网络或者FTPSERVER问题,重新链接
          else System.exit(0); // 从未初始化,说明刚运行就失败,直接退出
        } catch (InterruptedException | IllegalArgumentException | IOException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

}
