package com.example.quickcash.entities;

import com.google.android.gms.maps.model.LatLng;


public class Job {
    private LatLng locationLL;
    private String id;
    private String name;
    private String email;
    private String description;
    private String category;
    private double latitude;
    private double longitude;
    private int location;
    private String employerId;
    private String status = "open";

    /**
     * Model class for job listings in the QuickCash application.
     * Represents a job with attributes including name, description, category, and geographic location.
     * Used for storing and retrieving job information from Firebase and displaying jobs in the UI.
     *
     * @author Group 11
     * @version 1.0
     */

    public Job(String name, String description, String category, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        // Initialize with default values
        this.email = null;  // Will be set separately after construction
        this.status = "open";
    }

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Job() {
    }

    /**
     * @param jobName     user input, what they're filtering for with search
     * @param jobLocation LatLng object, specifies where the job marker on map is
     * @author Ethan Pancura
     * New constructor can be overloaded when needed (during job creation).
     * This constructor used for search function on job search activity.
     */
    public Job(String jobName, String jobDescription, LatLng jobLocation, String category) {
        this.name = jobName;
        this.description = jobDescription;
        this.category = category;
        this.latitude = jobLocation.latitude;
        this.longitude = jobLocation.longitude;
        this.locationLL = jobLocation;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the job name.
     *
     * @param name The name or title to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the job description.
     *
     * @return The detailed description of the job
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the job description.
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the job category.
     *
     * @return The category the job belongs to
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the job category.
     *
     * @param category The category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the latitude coordinate of the job location.
     *
     * @return The latitude value
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude coordinate of the job location.
     *
     * @param latitude The latitude value to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude coordinate of the job location.
     *
     * @return The longitude value
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude coordinate of the job location.
     *
     * @param longitude The longitude value to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String employerId) {
        this.email = employerId;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //ETHAN
    //Updated the getLocation method which was unused to
    //use LatLng instead of original implementation
    public LatLng getLocation() {
        return locationLL;
    }

    public void setLocation(int location) {
        this.location = location;
    }
}