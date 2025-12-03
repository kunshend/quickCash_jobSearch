package com.example.quickcash.entities;

public class Application {
    private String jobId;

    private String jobName;

    private String applicationId;

    private String email;
    private String message;

    private String status;

    public Application() {}

    public Application(String jobId, String applicationId, String message, String email, String status) {
        this.jobId = jobId;
        this.message = message;
        this.email = email;
        this.applicationId = applicationId;
        this.status = status;
    }

    public String getJobId() {return jobId;}

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail(){return email;}

    public void setEmail(String email){this.email = email;}

    public String getStatus(){return status;}

    public void setStatus(String status){this.status = status;}

    public String getId(){return applicationId;}

    public void setId(String applicationId){this.applicationId = applicationId;}


    public String getJobName(){
        return this.jobName;
    }

    public void setJobName(String jobName){
        this.jobName = jobName;
    }
}
