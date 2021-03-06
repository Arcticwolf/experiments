package org.openengsb.experiments.user;

import java.io.File;

import org.openengsb.experiments.provider.model.Model;
import org.openengsb.experiments.provider.model.ModelId;

@Model
public class TryOutModel {
    private Integer id;
    private String name;
    private File file;

    public Integer getId() {
        return id;
    }

    @ModelId
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
}
