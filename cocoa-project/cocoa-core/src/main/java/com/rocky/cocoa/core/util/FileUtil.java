package com.rocky.cocoa.core.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtil {

  /**
   * unzip a zip file.
   *
   * @param source src zip file
   * @param dest output file
   * @throws IOException throws io exception
   */
  public static void unzip(ZipFile source, File dest) throws IOException {
    Enumeration<?> entries = source.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) entries.nextElement();
      File newFile = new File(dest, entry.getName());
      if (entry.isDirectory()) {
        newFile.mkdirs();
      } else {
        newFile.getParentFile().mkdirs();
        InputStream src = source.getInputStream(entry);
        try {
          OutputStream output =
              new BufferedOutputStream(new FileOutputStream(newFile));
          try {
            IOUtils.copy(src, output);
          } finally {
            output.close();
          }
        } finally {
          src.close();
        }
      }
    }
  }

  private static void zipFile(String path, File input, ZipOutputStream zout)
      throws IOException {
    if (input.isDirectory()) {
      File[] files = input.listFiles();
      if (files != null) {
        for (File f : files) {
          String childPath =
              path + input.getName() + (f.isDirectory() ? "/" : "");
          zipFile(childPath, f, zout);
        }
      }
    } else {
      String childPath =
          path + (path.length() > 0 ? "/" : "") + input.getName();
      ZipEntry entry = new ZipEntry(childPath);
      zout.putNextEntry(entry);
      InputStream fileInputStream =
          new BufferedInputStream(new FileInputStream(input));
      try {
        IOUtils.copy(fileInputStream, zout);
      } finally {
        fileInputStream.close();
      }
    }
  }

  public static void zip(File input, File output) throws IOException {
    FileOutputStream out = new FileOutputStream(output);
    ZipOutputStream zout = new ZipOutputStream(out);
    try {
      zipFile("", input, zout);
    } finally {
      zout.close();
    }
  }

  /**
   * copy file or directory.
   *
   * @param src src file or dir
   * @param distDir dest dir
   * @param distName dest file or dir name
   */
  public static void copyFileOrDirectory(File src, String distDir, String distName)
      throws IOException {
    File dir = new File(distDir);
    dir.mkdirs();
    if (src.isFile()) {
      Files.copy(src.toPath(), new File(distDir + File.separator + distName).toPath(),
          StandardCopyOption.REPLACE_EXISTING);
    } else {
      //String dist = distDir + File.separator + src.getName();
      String dist = distDir + File.separator + distName;
      dir = new File(dist);
      dir.mkdirs();
      File[] files = src.listFiles();
      for (File file : files) {
        if (file.isDirectory()) {
          copyFileOrDirectory(file, dist, file.getName());
        } else {
          Files.copy(file.toPath(), new File(dist + File.separator + file.getName()).toPath(),
              StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  public static void zipFolderContent(File folder, File output)
      throws IOException {
    FileOutputStream out = new FileOutputStream(output);
    ZipOutputStream zout = new ZipOutputStream(out);
    try {
      File[] files = folder.listFiles();
      if (files != null) {
        for (File f : files) {
          zipFile("", f, zout);
        }
      }
    } finally {
      zout.close();
    }
  }

  /**
   * delete file or directory(recursive).
   *
   * @param file file or directory
   * @throws IOException thows io exception
   */
  public static void deleteFileOrDir(File file)
      throws IOException {

    if (file.isDirectory()) {
      for (File innerFile : file.listFiles()) {
        deleteFileOrDir(innerFile);
        innerFile.delete();
      }
    }
    file.delete();

  }

  /**
   * formatFileSize.
   *
   * @param fileS fileS
   * @return string
   */
  public static String formatFileSize(long fileS) {
    DecimalFormat df = new DecimalFormat("#.00");
    String fileSizeString = "";
    String wrongSize = "0 B";
    if (fileS == FileSize.NULL.getValue()) {
      return wrongSize;
    }
    if (fileS < FileSize.BYTE_MAX.getValue()) {
      fileSizeString = df.format((double) fileS) + " B";
    } else if (fileS < FileSize.KB_MAX.getValue()) {
      fileSizeString = df.format((double) fileS / FileSize.BYTE_MAX.getValue()) + " KB";
    } else if (fileS < FileSize.MB_MAX.getValue()) {
      fileSizeString = df.format((double) fileS / FileSize.KB_MAX.getValue()) + " MB";
    } else if (fileS < FileSize.GB_MAX.getValue()) {
      fileSizeString = df.format((double) fileS / FileSize.MB_MAX.getValue()) + " GB";
    } else {
      fileSizeString = df.format((double) fileS / FileSize.GB_MAX.getValue()) + " TB";
    }
    return fileSizeString;
  }


  private enum FileSize {
    NULL(0),
    BYTE_MAX(1024L),
    KB_MAX(1048576L),
    MB_MAX(1073741824L),
    GB_MAX(1073741824 * 1024L);


    private long size;

    FileSize(long size) {
      this.size = size;
    }

    public long getValue() {
      return this.size;
    }
  }
}
