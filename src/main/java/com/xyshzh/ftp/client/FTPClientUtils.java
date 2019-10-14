package com.xyshzh.ftp.client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;

/**
 * FTP 客户端工具.
 * @author Shengjun Liu
 * @version 2018-10-02
 */
public class FTPClientUtils {

  /**服务器IP*/
  private String ip;
  /**服务器端口*/
  private int port;
  /**服务器登录用户名*/
  private String username;
  /**服务器登录用户密码*/
  private String password;
  /**服务器链接采用被动模式[默认false]*/
  private boolean passiveMode = false;
  /**服务器登录后需要切换的目录*/
  private String basePath;
  /**FTP客户端实例*/
  private FTPClient ftpClient;

  /**
   * @param ip 服务器IP
   * @param username 服务器登录用户名
   * @param password 服务器登录用户密码
   * @throws IllegalArgumentException
   * @throws ConnectException
   * @throws SocketException 
   * @throws IOException 
   */
  public FTPClientUtils(String ip, String username, String password) throws IllegalArgumentException, ConnectException, SocketException, IOException {
    this(ip, 21, username, password, false, "/");
  }

  /**
   * @param ip 服务器IP
   * @param port 服务器端口
   * @param username 服务器登录用户名
   * @param password 服务器登录用户密码
   * @throws IllegalArgumentException
   * @throws ConnectException
   * @throws SocketException 
   * @throws IOException 
   */
  public FTPClientUtils(String ip, int port, String username, String password) throws IllegalArgumentException, ConnectException, SocketException, IOException {
    this(ip, port, username, password, false, "/");
  }

  /**
   * @param ip 服务器IP
   * @param port 服务器端口
   * @param username 服务器登录用户名
   * @param password 服务器登录用户密码
   * @param passiveMode 服务器链接采用被动模式[默认false]
   * @param basePath 服务器登录后需要切换的目录
   * @throws IllegalArgumentException
   * @throws ConnectException
   * @throws SocketException 
   * @throws IOException 
   */
  public FTPClientUtils(String ip, int port, String username, String password, boolean passiveMode, String basePath)
      throws IllegalArgumentException, ConnectException, SocketException, IOException {
    if (null == ip || 0 == ip.trim().length()) { throw new IllegalArgumentException("服务器IP指定异常."); }
    if (0 > port || 65535 < port) { throw new IllegalArgumentException("服务器端口指定异常."); }
    if (null == username || 0 == username.trim().length()) { throw new IllegalArgumentException("服务器用户名指定异常."); }
    if (null == password || 0 == password.trim().length()) { throw new IllegalArgumentException("服务器用户密码指定异常."); }
    if (null != basePath && 0 == basePath.trim().length()) { throw new IllegalArgumentException("服务器登录后需要切换的目录指定异常."); }
    this.ip = ip;
    this.port = port;
    this.username = username;
    this.password = password;
    this.passiveMode = passiveMode;
    this.basePath = basePath;
    this.reconnect();
  }

  /**
   * FTP 重链.
   * @throws IllegalArgumentException
   * @throws ConnectException
   * @throws SocketException 
   * @throws IOException 
   */
  public void reconnect() throws IllegalArgumentException, ConnectException, SocketException, IOException {
    if (null != this.ftpClient) {
      close();
    }
    if (null == this.ftpClient) {
      // 创建链接实例
      this.ftpClient = new FTPClient();
      // 链接
      ftpClient.connect(this.ip, this.port);
      // 登录
      ftpClient.login(this.username, this.password);
      // 切换目录
      if (null != this.basePath) ftpClient.changeWorkingDirectory(basePath);
      // 修改模式
      if (this.passiveMode) ftpClient.enterLocalPassiveMode();
      // 修改Unix配置,FTP架设在Windows上,可忽略
      ftpClient.configure(new FTPClientConfig(com.xyshzh.ftp.parser.UnixFTPEntryParser.class.getName()));
    }
  }

  /**
   * 获取登录IP.
   * @return
   */
  public String getIp() {
    return ip;
  }

  /**
   * 获取登录端口.
   * @return
   */
  public int getPort() {
    return port;
  }

  /**
   * 获取登录用户名.
   * @return
   */
  public String getUsername() {
    return username;
  }

  /**
   * 获取登录用户密码.
   * @return
   */
  public String getPassword() {
    return password;
  }

  /**
   * 获取被动模式状态.
   * @return
   */
  public boolean isPassiveMode() {
    return passiveMode;
  }

  /**
   * 获取登录后需要切换的目录.
   * @return
   */
  public String getBasePath() {
    return basePath;
  }

  /**
   * 获取FTP客户端实例.
   * @return
   */
  public FTPClient getFtpClient() {
    return ftpClient;
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
