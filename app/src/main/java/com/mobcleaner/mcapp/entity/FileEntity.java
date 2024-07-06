package com.mobcleaner.mcapp.entity;

import android.util.Log;

import com.trustlook.sdk.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mulinli on 5/4/18.
 */

public class FileEntity {
    public enum Type{
        FLODER,FILE
    }
    private String filePath;
    private String fileName;
    private String fileSize;
    private Type fileType;
    private boolean checked;
    private List<FileEntity> children;
    private FileEntity parent;

    public FileEntity(String filePath) {
        this.filePath = filePath;
        this.children = new ArrayList<>();
        this.parent = null;
    }


    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileSize() {
        return fileSize;
    }
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
    public Type getFileType() {
        return fileType;
    }
    public void setFileType(Type fileType) {
        this.fileType = fileType;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public List<FileEntity> getChildren() {
        return children;
    }

    public void setChildren(List<FileEntity> children) {
        this.children = children;
    }

    public FileEntity getParent() {
        return parent;
    }

    public void setParent(FileEntity parent) {
        this.parent = parent;
    }

    public List<FileEntity> addChildren() {

        List<FileEntity> children = new ArrayList<>();
        if (children != null && children.size() > 0 ) {
            Log.d(Constants.TAG, "Already added children: " + children.size());
            return children;
        }
        if (filePath == null || filePath.equals("")) {
            return children;
        }

        File fatherFile = new File(filePath);
        File[] files = fatherFile.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                FileEntity entity = new FileEntity(files[i].getPath());
                boolean isDirectory = files[i].isDirectory();
                if (isDirectory == true) {
                    entity.setFileType(Type.FLODER);
//                  entity.setFileName(files[i].getPath());
                } else {
                    entity.setFileType(Type.FILE);
                }
                entity.setFileName(files[i].getName().toString());
                entity.setFilePath(files[i].getAbsolutePath());
                entity.setFileSize(files[i].length() + "");
                entity.setChecked(isChecked());
                children.add(entity);
            }
            this.children = children;
        }
        return children;
    }
}
