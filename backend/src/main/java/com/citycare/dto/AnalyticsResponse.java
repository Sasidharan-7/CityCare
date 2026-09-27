package com.citycare.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {
    private long totalComplaints;
    private long pending;
    private long assigned;
    private long inProgress;
    private long resolved;
    private long closed;
    private long critical;
    private long totalUsers;
    private long totalOfficers;
    private Map<String, Long> byCategory;
    private Map<String, Long> byStatus;
    private Map<String, Long> byDepartment;
    private List<ComplaintResponse> complaints;

    public AnalyticsResponse() {}

    public long getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(long totalComplaints) { this.totalComplaints = totalComplaints; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public long getAssigned() { return assigned; }
    public void setAssigned(long assigned) { this.assigned = assigned; }

    public long getInProgress() { return inProgress; }
    public void setInProgress(long inProgress) { this.inProgress = inProgress; }

    public long getResolved() { return resolved; }
    public void setResolved(long resolved) { this.resolved = resolved; }

    public long getClosed() { return closed; }
    public void setClosed(long closed) { this.closed = closed; }

    public long getCritical() { return critical; }
    public void setCritical(long critical) { this.critical = critical; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalOfficers() { return totalOfficers; }
    public void setTotalOfficers(long totalOfficers) { this.totalOfficers = totalOfficers; }

    public Map<String, Long> getByCategory() { return byCategory; }
    public void setByCategory(Map<String, Long> byCategory) { this.byCategory = byCategory; }

    public Map<String, Long> getByStatus() { return byStatus; }
    public void setByStatus(Map<String, Long> byStatus) { this.byStatus = byStatus; }

    public Map<String, Long> getByDepartment() { return byDepartment; }
    public void setByDepartment(Map<String, Long> byDepartment) { this.byDepartment = byDepartment; }

    public List<ComplaintResponse> getComplaints() { return complaints; }
    public void setComplaints(List<ComplaintResponse> complaints) { this.complaints = complaints; }
}
