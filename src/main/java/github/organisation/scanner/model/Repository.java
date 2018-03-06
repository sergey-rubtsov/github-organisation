package github.organisation.scanner.model;

import java.util.List;

public class Repository {

    private Integer id;

    private String name;

    private String full_name;

    private String description;

    private String owner;

    private List<Integer> collaborators;

    public Repository(Integer id, String name, String full_name, String description, String owner, List<Integer> collaborators) {
        this.id = id;
        this.name = name;
        this.full_name = full_name;
        this.description = description;
        this.owner = owner;
        this.collaborators = collaborators;
    }

    public Repository(Integer id, String name, String full_name, String description, String owner) {
        this.id = id;
        this.name = name;
        this.full_name = full_name;
        this.description = description;
        this.owner = owner;
    }

    public Repository() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Integer> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(List<Integer> collaborators) {
        this.collaborators = collaborators;
    }
}
