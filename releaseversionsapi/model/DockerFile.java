package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import java.util.LinkedList;
import java.util.List;

/**
 * For mapping the file dockerfile content under validation
 */
public class DockerFile
{
    private List<String> dockerfileContent = new LinkedList<>();

    /**
     * Get the dockerfile content
     *
     * @return dockerfile content
     */
    public List<String> getDockerfileContent()
    {
        List<String> returnList = new LinkedList<>();
        returnList.addAll(dockerfileContent);

        return returnList;
    }

    /**
     * Set the dockerfile content
     *
     * @param dockerfileContent List of lines of the dockerfile
     */
    public void setDockerfileContent(List<String> dockerfileContent)
    {
        this.dockerfileContent = new LinkedList<>();
        this.dockerfileContent.addAll(dockerfileContent);
    }

    /**
     * Add a new line of the dockerfile content
     *
     * @param newLine New line of the dockerfile content
     */
    public void addDockerfileLine(String newLine)
    {
        this.dockerfileContent.add(newLine);
    }

    /**
     * @param validatorInputs Values of the parameters under validation
     * @return Dockerfile field values for NOVA, Batch and Thin2 service
     */
    public List<String> getDockerfileTemplateForNovaBatchAndThin2(ValidatorInputs validatorInputs)
    {
        List<String> list = new LinkedList<>();
        list.add("FROM nova/nova-java-service:__BASE_IMAGE_VERSION__");
        list.add("ADD /" + validatorInputs.getPom().getGroupId() + "-" + validatorInputs.getPom().getArtifactId() + ".jar /");
        list.add("RUN sh -c 'touch /" + validatorInputs.getPom().getGroupId() + "-" + validatorInputs.getPom().getArtifactId() + ".jar'");
        list.add("ENTRYPOINT [\"/bin/sh\", \"-c\", \"echo HelloNOVAApp\"]");
        return list;
    }

    /**
     * @return Dockerfile field values for NODE service
     */
    public List<String> getDockerfileTemplateForNode()
    {
        List<String> list = new LinkedList<>();
        list.add("(FROM nova\\/nova-node-service[6-8]+:__BASE_IMAGE_VERSION__)");
        list.add("ADD \\/app \\/app");
        list.add("RUN sh -c 'touch \\/app\\/index.js'");
        list.add("ENTRYPOINT \\[\"\\/bin\\/sh\", \"-c\", \"echo HelloNodeApp\"\\]");
        return list;
    }
}
