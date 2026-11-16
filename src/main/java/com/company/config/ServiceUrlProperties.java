package com.company.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.services")
public class ServiceUrlProperties {

    private String employeeBaseUrl;
    private String departmentBaseUrl;
    private int departmentConnectTimeoutMs = 2000;
    private int departmentReadTimeoutMs = 3000;

    public String getEmployeeBaseUrl() {
        return employeeBaseUrl;
    }

    public void setEmployeeBaseUrl(String employeeBaseUrl) {
        this.employeeBaseUrl = employeeBaseUrl;
    }

    public String getDepartmentBaseUrl() {
        return departmentBaseUrl;
    }

    public void setDepartmentBaseUrl(String departmentBaseUrl) {
        this.departmentBaseUrl = departmentBaseUrl;
    }

    public int getDepartmentConnectTimeoutMs() {
        return departmentConnectTimeoutMs;
    }

    public void setDepartmentConnectTimeoutMs(int departmentConnectTimeoutMs) {
        this.departmentConnectTimeoutMs = departmentConnectTimeoutMs;
    }

    public int getDepartmentReadTimeoutMs() {
        return departmentReadTimeoutMs;
    }

    public void setDepartmentReadTimeoutMs(int departmentReadTimeoutMs) {
        this.departmentReadTimeoutMs = departmentReadTimeoutMs;
    }
}
