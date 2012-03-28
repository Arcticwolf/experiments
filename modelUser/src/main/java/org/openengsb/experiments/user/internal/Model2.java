package org.openengsb.experiments.user.internal;

import org.openengsb.experiments.provider.model.Model;
import org.openengsb.experiments.provider.model.ModelId;

@Model
public class Model2 {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    @ModelId
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
