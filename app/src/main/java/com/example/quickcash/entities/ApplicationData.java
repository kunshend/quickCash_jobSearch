package com.example.quickcash.entities;

import com.google.firebase.database.PropertyName;


/**
 * A unified class for handling application data across different activities.
 * This replaces the separate Application classes previously defined in
 * ApplicationList and ViewApplications.
 */
public class ApplicationData {
    private String id;
    private String email;

    @PropertyName("jobId")
    private String jobId;

    @PropertyName("jobName")
    private String jobName;

    private String message;
    private String status;

    private String jobStatus;


    /**
     * Default constructor required for Firebase.
     */
    public ApplicationData() {}

    /**
     * Fully parameterized constructor.
     *
     * @param id The application ID
     * @param email The applicant's email
     * @param jobName The name of the job being applied for
     * @param message The application message/cover letter
     */
    public ApplicationData(String id, String email, String jobName, String message) {
        this.id = id;
        this.email = email;
        this.jobName = jobName;
        this.message = message;
    }

    /**
     * Constructor with just ID and job name for "No applications" placeholder.
     *
     * @param id The placeholder ID
     * @param jobName The job name or empty string
     */
    public ApplicationData(String id, String jobName) {
        this.id = id;
        this.jobId = jobName;
        this.email = "";
        this.message = "";
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @PropertyName("jobId")
    public String getJobId() {
        return jobId;
    }

    @PropertyName("jobId")
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @PropertyName("jobName")
    public String getJobName() {
        return jobName;
    }

    @PropertyName("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    //Status helpers added by Ethan for US-1, iteration 3
    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }
}
