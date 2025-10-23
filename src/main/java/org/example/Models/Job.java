package org.example.Models;

public class Job {
    public String clientName;
    public String clientPhone;
    public String clientNote;

    public String jobName;
    public String truckName;

    // Конструктор з параметрами
    public Job(String clientName, String clientPhone, String clientNote,
               String jobName, String truckName) {
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.clientNote = clientNote;
        this.jobName = jobName;
        this.truckName = truckName;
    }

    public Job(String jobName, String truckName) {
        this.jobName = jobName;
        this.truckName = truckName;
    }
}
